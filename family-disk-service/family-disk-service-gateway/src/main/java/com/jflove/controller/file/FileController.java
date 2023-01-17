package com.jflove.controller.file;

import com.jflove.ResponseHeadDTO;
import com.jflove.admin.api.IFileAdministration;
import com.jflove.config.ByteResourceHttpRequestHandlerConfig;
import com.jflove.config.HttpConstantConfig;
import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileReadReqDTO;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.dto.FileTransmissionRepDTO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.tool.JJwtTool;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.file.DelFileParamVO;
import com.jflove.vo.file.GetFileParamVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
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
    private JJwtTool jJwtTool;

    @DubboReference
    private IUserInfo userInfo;

    @DubboReference
    private IUserSpace userSpace;

    @DubboReference
    private IFileAdministration fileAdministration;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    @Autowired
    private ByteResourceHttpRequestHandlerConfig resourceHttpRequestHandle;

    //todo 前端播放器不支持播放mov等视频资源,或者播放有的mp4黑屏问题,考虑是否后端转码输出 可以试试 FFMPEG 解决转码问题 Chrome只支持标准的H.264方式编码
    @ApiOperation(value = "媒体资源边播边下")
    @GetMapping("/media/play/{source}/{fileMd5}/{useSpaceId}/{token}")
    public void mediaPlay(HttpServletRequest request,HttpServletResponse response,
                              @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,JOURNAL=日记)") @PathVariable("source") String source,
                              @ApiParam("文件md5值") @PathVariable("fileMd5") String fileMd5,
                              @ApiParam("正在使用的空间") @PathVariable("useSpaceId") Long useSpaceId,
                              @ApiParam("token") @PathVariable("token") String token
    )throws IOException, ServletException {
        Assert.notNull(fileMd5,"错误的请求:文件md5不能为空");
        Assert.notNull(source,"错误的请求:文件来源不能为空");
        Assert.notNull(token,"错误的请求:token不能为空");
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Jws<Claims> jws = jJwtTool.parseJwt(token);
        Claims claims = jws.getBody();
        //token验证通过,返回用户信息
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.getUserInfoByEmail(claims.getId());
        Assert.notNull(dto.getData(),dto.getMessage());
        UserSpaceRoleENUM useSpacerRole = dto.getData().getSpaces().stream().filter(
                e->String.valueOf(e.getSpaceId()).equals(String.valueOf(useSpaceId))
        ).toList().get(0).getRole();
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,fileMd5);
        request.setAttribute(ByteResourceHttpRequestHandlerConfig.SOURCE,source);
        request.setAttribute(ByteResourceHttpRequestHandlerConfig.SPACE_ID,useSpaceId);
        resourceHttpRequestHandle.handleRequest(request, response);
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
        ResponseHeadDTO<Boolean> dto = fileAdministration.delFile(param.getFileMd5(),useSpaceId,FileSourceENUM.valueOf(param.getSource()));
        ResponseHeadVO<Boolean> vo = new ResponseHeadVO<>();
        BeanUtils.copyProperties(dto,vo);
        return vo;
    }

    @ApiOperation(value = "下载文件(分片下载方式)")
    @PostMapping("/slice/getFile")
    public void sliceGetFile(
            HttpServletRequest request,HttpServletResponse response,
            @RequestBody @Valid GetFileParamVO param) throws Exception{
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,param.getFileMd5());
        request.setAttribute(ByteResourceHttpRequestHandlerConfig.SOURCE,param.getSource());
        request.setAttribute(ByteResourceHttpRequestHandlerConfig.SPACE_ID,useSpaceId);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,HttpHeaders.CONTENT_RANGE);
        resourceHttpRequestHandle.handleRequest(request, response);
    }

    @ApiOperation(value = "下载文件(完整文件,非分片)")
    @PostMapping("/getFile")
    public void getFile(HttpServletResponse response,@RequestBody @Valid GetFileParamVO param) throws Exception{
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try(ServletOutputStream baos = response.getOutputStream()){
            AtomicBoolean ab = new AtomicBoolean(false);
            StreamObserver<FileReadReqDTO> request = fileService.getFile(new StreamObserver<FileTransmissionDTO>() {
                @Override
                public void onNext(FileTransmissionDTO data) {
                    try{
                        baos.write(data.getShardingStream());
                    }catch (IOException e){
                        onError(e);
                    }
                    if(data.getShardingSort() == data.getShardingNum()){//是最后一片
                        ab.set(true);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    ab.set(true);
                    log.error("文件读写服务异常",throwable);
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
            request.onCompleted();//重要,当不需要再请求了,必须要调这个函数通知对方,对方也调用这个函数通知请求方,如果不这么做,会导致不会释放,最终内存崩溃
        }
    }

    @ApiOperation(value = "上传文件(大文件,分片)")
    @PostMapping("/slice/addFile")
    public ResponseHeadVO<String> sliceAddFile(@ApiParam("文件流") @RequestPart("f") MultipartFile f,
                                          @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,JOURNAL=日记)") @RequestParam("s") String s,
                                          @ApiParam("文件多媒体类型") @RequestParam("m") String m,
                                               @ApiParam("文件分片数量") @RequestParam("n") Integer n,
                                               @ApiParam("开始位置") @RequestParam("start") Integer start,
                                               @ApiParam("结束位置") @RequestParam("end") Integer end,
                                               @ApiParam("总大小") @RequestParam("totalLength") Long totalLength,
                                               @ApiParam("文件真实名称") @RequestParam("originalFileName") String originalFileName,
                                               @ApiParam("前端计算的文件md5") @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        Long useSpaceId = (Long) autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Long useUserId = (Long) autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM) autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId, "错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useUserId, "错误的请求:用户ID不能为空");
        Assert.notNull(useSpacerRole, "错误的请求:正在使用的空间权限不能为空");
        if (useSpacerRole != UserSpaceRoleENUM.WRITE) {
            throw new SecurityException("用户对该空间没有写入权限");
        }
        FileTransmissionDTO dto = new FileTransmissionDTO();
        dto.setRangeStart(start.longValue());
        dto.setRangeEnd(end.longValue());
        dto.setTotalSize(totalLength);
        dto.setFileMd5(fileMd5);
        dto.setReadLength(f.getSize());
        dto.setShardingNum(n);
        byte[] total = f.getBytes();
        f.getInputStream().close();//读取完字节后,将流关闭掉
        dto.setShardingStream(total);
        dto.setType(originalFileName.lastIndexOf(".") != -1 ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "");
        dto.setSpaceId(useSpaceId);
        dto.setSource(FileSourceENUM.valueOf(s));
        int sort = Integer.parseInt(f.getOriginalFilename().lastIndexOf("-") != -1 ? f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf("-") + 1) : "0");
        dto.setShardingSort(sort);
        dto.setCreateUserId(useUserId);
        dto.setMediaType(m);
        dto.setName(originalFileName);

        //尝试是否可以从垃圾箱恢复
        if(fileAdministration.dustbinRecovery(dto.getFileMd5(),dto.getSpaceId(),dto.getSource()).isResult()){
            return new ResponseHeadVO<>(true,dto.getFileMd5(),"已经从垃圾箱恢复文件,不需要重复上传.");
        }
        //是否还存的下
        DataSize ds = DataSize.ofBytes(dto.getTotalSize());
        ResponseHeadDTO use = userSpace.useSpaceByte(dto.getSpaceId(),ds.toMegabytes(),true,false);
        if(!use.isResult()){
            return new ResponseHeadVO(false,dto.getFileMd5(),"用户存储空间不足");
        }

        //尝试是否可以直接引用其它人上传的文件
        if(fileAdministration.checkDuplicate(dto.getName(),dto.getType(),dto.getMediaType(),dto.getFileMd5(),dto.getSpaceId(),dto.getSource(),dto.getTotalSize(),useUserId).isResult()){
            return new ResponseHeadVO<>(true,dto.getFileMd5(),"触发秒传成功");
        }

        AtomicBoolean ab = new AtomicBoolean(false);
        ResponseHeadVO<String> ret = new ResponseHeadVO<String>();
        StreamObserver<FileTransmissionDTO> request = fileService.writeByte(new StreamObserver<FileTransmissionRepDTO>() {
            @Override
            public void onNext(FileTransmissionRepDTO data) {
                ret.setResult(data.isResult());
                ret.setMessage(data.getMessage());
                ret.setData(data.getFileMd5());
                ab.set(true);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("文件读写服务异常",throwable);
                ret.setResult(false);
                ret.setMessage("分片发生异常,请重试");
                ret.setData("retry");
                ab.set(true);
            }

            @Override
            public void onCompleted() {
            }
        });
        request.onNext(dto);
        while (true){
            TimeUnit.SECONDS.sleep(1);
            if(ab.get()){
                break;
            }
        }
        request.onCompleted();//重要,当不需要再请求了,必须要调这个函数通知对方,对方也调用这个函数通知请求方,如果不这么做,会导致不会释放,最终内存崩溃
        return ret;
    }

    @ApiOperation(value = "上传文件(完整文件,非分片)")
    @PostMapping("/addFile")
    public ResponseHeadVO<String> addFile(@ApiParam("文件流") @RequestPart("f") MultipartFile f,
                                          @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,JOURNAL=日记)") @RequestParam("s") String s,
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
        byte[] total = f.getBytes();
        //读取完字节后关闭流
        f.getInputStream().close();
        String md5 = DigestUtils.md5DigestAsHex(total);
        dto.setFileMd5(md5);
        //尝试是否可以从垃圾箱恢复
        if(fileAdministration.dustbinRecovery(dto.getFileMd5(),dto.getSpaceId(),dto.getSource()).isResult()){
            return new ResponseHeadVO<>(true,dto.getFileMd5(),"已经从垃圾箱恢复文件,不需要重复上传.");
        }
        //是否还存的下
        DataSize ds = DataSize.ofBytes(dto.getTotalSize());
        ResponseHeadDTO use = userSpace.useSpaceByte(dto.getSpaceId(),ds.toMegabytes(),true,false);
        if(!use.isResult()){
            return new ResponseHeadVO(false,dto.getFileMd5(),"用户存储空间不足");
        }

        //尝试是否可以直接引用其它人上传的文件
        if(fileAdministration.checkDuplicate(dto.getName(),dto.getType(),dto.getMediaType(),dto.getFileMd5(),dto.getSpaceId(),dto.getSource(),dto.getTotalSize(),useUserId).isResult()){
            return new ResponseHeadVO<>(true,dto.getFileMd5(),"触发秒传成功");
        }

        //判断该文件对于当前用户已存在了
        ResponseHeadDTO<Boolean> ex = fileAdministration.isExist(dto.getFileMd5(),dto.getSpaceId(),dto.getSource());
        if(ex.getData()){//文件已经存在这个空间了,直接返回成功,不需要写盘了
            return new ResponseHeadVO<>(true,dto.getFileMd5(),"该文件已存在空间中,可以直接引用");
        }

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
                log.error("文件读写服务异常",throwable);
                ret.setResult(false);
                ret.setMessage("上传失败");
                ab.set(true);
            }

            @Override
            public void onCompleted() {
            }
        });
        try {
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
        }catch (Exception e){
            log.error("文件上传发生异常",e);
            return new ResponseHeadVO<>(false,"文件上传失败");
        }
        while (true){
            TimeUnit.SECONDS.sleep(1);
            if(ab.get()){
                break;
            }
        }
        request.onCompleted();//重要,当不需要再请求了,必须要调这个函数通知对方,对方也调用这个函数通知请求方,如果不这么做,会导致不会释放,最终内存崩溃
        return ret;
    }
}
