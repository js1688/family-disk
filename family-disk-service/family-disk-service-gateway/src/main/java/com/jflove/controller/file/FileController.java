package com.jflove.controller.file;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileReadReqDTO;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.dto.FileTransmissionRepDTO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.file.DelFileParamVO;
import com.jflove.vo.file.GetFileParamVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author tanjun
 * @date 2022/12/13 11:07
 * @describe 文件管理
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
@Log4j2
public class FileController {
    @DubboReference
    private IFileService fileService;
    @Autowired
    private HttpServletRequest autowiredRequest;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

//    @ApiOperation(value = "上传文件,分片多线程上传")
//    @PostMapping("/addFileSharding")
//    public ResponseHeadVO<String> addFile(@ApiParam("文件流") @RequestPart MultipartFile file){
//        //todo 每次上传的都是一个文件的分片,临时文件,最终多个分片合并,这个还没有想好怎么实现
//        return new ResponseHeadVO<>(false,"还未实现");
//    }

    @ApiOperation(value = "文件下载(分片下载,适合大文件)")
    @GetMapping("/slice/download/{fileMd5}")
    public void sliceDownload(HttpServletRequest request,HttpServletResponse response,
                                                  @ApiParam("文件md5值") @PathVariable("fileMd5") String fileMd5
    ){
        Assert.notNull(fileMd5,"错误的请求:文件md5不能为空");
        //获取从那个字节开始读取文件9
        String rangeString = request.getHeader(HttpHeaders.RANGE);
        if (!StringUtils.hasLength(rangeString)) {
            new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
        response.setContentType("video/mp4");
        long rangeStart = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
        DataSize ds = DataSize.of(1,DataUnit.MEGABYTES);
        File file = new File("F:\\formal\\39faacabc42b1075df8b189ad40bad7f.mp4");
        response.setHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %s-%s/%s",rangeStart,rangeStart + ds.toBytes(),file.length()));
        response.setHeader(HttpHeaders.CONTENT_LENGTH,String.valueOf(ds.toBytes()));
        try(RandomAccessFile raf = new RandomAccessFile(new File("F:\\formal\\39faacabc42b1075df8b189ad40bad7f.mp4" ), "r");OutputStream outputStream = response.getOutputStream();) {
            byte [] b = new byte[(int)ds.toBytes()];
            raf.seek(rangeStart);
            int len = raf.read(b);
            //获取响应的输出流
            outputStream.write(b,0,len);
        }catch (IOException e){
            log.error("读取文件异常",e);
        }
    }

    @ApiOperation(value = "删除文件")
    @PostMapping("/delFile")
    public ResponseHeadVO<Boolean> delFile(@RequestBody @Valid DelFileParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有删除权限");
        }
        ResponseHeadDTO<Boolean> dto = fileService.delFile(param.getFileMd5(),useSpaceId,FileSourceENUM.valueOf(param.getSource()));
        ResponseHeadVO<Boolean> vo = new ResponseHeadVO<>();
        BeanUtils.copyProperties(dto,vo);
        return vo;
    }

    @ApiOperation(value = "下载文件(完整文件,非分片)")
    @PostMapping("/getFile")
    public ResponseEntity<Resource> getFile(@RequestBody @Valid GetFileParamVO param) throws Exception{
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            AtomicBoolean ab = new AtomicBoolean(false);
            StreamObserver<FileReadReqDTO> request = fileService.getFile(new StreamObserver<FileTransmissionDTO>() {
                @Override
                public void onNext(FileTransmissionDTO data) {
                    if(data.getShardingSort() == data.getShardingNum()){//是最后一片
                        ab.set(true);
                    }
                    baos.writeBytes(data.getShardingStream());
                }

                @Override
                public void onError(Throwable throwable) {
                    ab.set(true);
                }

                @Override
                public void onCompleted() {

                }
            });
            request.onNext(new FileReadReqDTO(param.getFileMd5(),FileSourceENUM.valueOf(param.getSource()),useSpaceId));
            while (true){
                TimeUnit.SECONDS.sleep(1);
                if(ab.get()){
                    break;
                }
            }
            Resource file = new ByteArrayResource(baos.toByteArray());
            return new ResponseEntity<>(file, headers, HttpStatus.OK);
        }catch (Exception e){
            throw e;
        }
    }

    @ApiOperation(value = "上传文件(完整文件,非分片)")
    @PostMapping("/addFile")
    public ResponseHeadVO<String> addFile(@ApiParam("文件流") @RequestPart("f") MultipartFile f,
                                          @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,DIARY=日记)") @RequestParam("s") String s,
                                          @ApiParam("文件多媒体类型") @RequestParam("m") String m
    ) throws Exception{
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useUserId,"错误的请求:用户ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有写入权限");
        }
        //将文件分片后发送过去
        long totalSize = f.getSize();//文件总大小
        long shardingConfigSize = DataSize.of((long)(maxFileSize.toMegabytes() * 0.1), DataUnit.MEGABYTES).toBytes();//如果被分片,每一片大小最多是最大允许上传的10%
        int shardingNum = shardingConfigSize <= 0 ? 0 : (int) (totalSize / shardingConfigSize);//本次分片个数
        FileTransmissionDTO dto = new FileTransmissionDTO();
        dto.setName(f.getOriginalFilename());
        dto.setTotalSize(totalSize);
        dto.setMediaType(m);
        dto.setShardingNum(shardingNum);
        dto.setSource(FileSourceENUM.valueOf(s));
        dto.setType(dto.getName().lastIndexOf(".") != -1 ? dto.getName().substring(dto.getName().lastIndexOf(".")) : "");
        dto.setSpaceId(useSpaceId);
        dto.setCreateUserId(useUserId);
        AtomicBoolean ab = new AtomicBoolean(false);
        ResponseHeadVO<String> ret = new ResponseHeadVO<String>();
        //按分片读取数据,再将数据发送出去
        StreamObserver<FileTransmissionDTO> request = fileService.addFile(new StreamObserver<FileTransmissionRepDTO>() {
            @Override
            public void onNext(FileTransmissionRepDTO data) {
                ret.setResult(data.isResult());
                ret.setData(data.getFileMd5());
                ret.setMessage(data.getMessage());
                ab.set(true);
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
            }
        });
        try {
            String md5 = DigestUtils.md5DigestAsHex(f.getInputStream());
            dto.setFileMd5(md5);
            //判断该文件对于当前用户已存在了
            ResponseHeadDTO<Boolean> ex = fileService.isExist(dto.getFileMd5(),dto.getSpaceId(),dto.getSource());
            if(ex.getData()){//文件已经存在这个空间了,直接返回成功,不需要写盘了
                ret.setResult(true);
                ret.setData(dto.getFileMd5());
                ret.setMessage("该文件已存在空间中,可以直接引用.");
                ab.set(true);
            }else {
                byte[] total = f.getInputStream().readAllBytes();
                int off = 0;
                for (int i = 0; i < shardingNum; i++) {
                    byte[] b = Arrays.copyOfRange(total, off, off + (int) shardingConfigSize);
                    dto.setShardingSort(i);
                    dto.setShardingStream(b);
                    request.onNext(dto);
                    off += b.length;
                }
                //发送最后一片
                byte[] b = Arrays.copyOfRange(total, off, off + (shardingConfigSize <= 0 ? (int) totalSize : (int) (totalSize % shardingConfigSize)));
                dto.setShardingSort(shardingNum);
                dto.setShardingStream(b);
                request.onNext(dto);
            }
        }catch (Exception e){
            log.error("文件上传发生异常",e);
            return new ResponseHeadVO<>(false,"文件上传失败");
        }finally {
            f.getInputStream().close();
        }
        while (true){
            TimeUnit.SECONDS.sleep(1);
            if(ab.get()){
                break;
            }
        }
        return ret;
    }
}
