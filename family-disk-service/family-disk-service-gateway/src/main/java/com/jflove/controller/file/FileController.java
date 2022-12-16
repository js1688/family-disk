package com.jflove.controller.file;

import cn.hutool.json.JSONUtil;
import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.file.DelFileParamVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;

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
    @ApiOperation(value = "上传文件(完整文件,非分片)")
    @PostMapping("/addFile")
    public ResponseHeadVO<String> addFile(@ApiParam("文件流") @RequestPart("f") MultipartFile f,
                                          @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘)") @RequestParam("s") String s){
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
        long shardingConfigSize = DataSize.of((long)(maxFileSize.toMegabytes() * 0.01), DataUnit.MEGABYTES).toBytes();//如果被分片,每一片大小最多是最大允许上传的1%
        int shardingNum = shardingConfigSize <= 0 ? 0 : (int) (totalSize / shardingConfigSize);//本次分片个数
        FileTransmissionDTO dto = new FileTransmissionDTO();
        dto.setName(f.getOriginalFilename());
        dto.setTotalSize(totalSize);
        dto.setShardingNum(shardingNum);
        dto.setSource(FileSourceENUM.valueOf(s));
        dto.setType(dto.getName().substring(dto.getName().lastIndexOf(".")));
        dto.setSpaceId(useSpaceId);
        dto.setCreateUserId(useUserId);
        //按分片读取数据,再将数据发送出去
        StreamObserver<FileTransmissionDTO> request = fileService.addFile(new StreamObserver<Boolean>() {
            @Override
            public void onNext(Boolean data) {

            }

            @Override
            public void onError(Throwable throwable) {
                //利用websocket推送文件上传结果
                String user = String.format("%s-%s-%s", useUserId,useSpaceId);
                messagingTemplate.convertAndSendToUser(user, "/add/file", JSONUtil.toJsonStr(new ResponseHeadVO<>(false,"文件写盘失败")));
            }

            @Override
            public void onCompleted() {
                //利用websocket推送文件上传结果
                String user = String.format("%s-%s-%s", useUserId,useSpaceId);
                messagingTemplate.convertAndSendToUser(user, "/add/file", JSONUtil.toJsonStr(new ResponseHeadVO<>(true,"文件写盘成功")));
            }
        });
        try {
            String md5 = DigestUtils.md5DigestAsHex(f.getInputStream());
            dto.setFileMd5(md5);
            //判断该文件对于当前用户已存在了
            ResponseHeadDTO<Boolean> ex = fileService.isExist(dto.getFileMd5(),dto.getSpaceId(),dto.getSource());
            if(ex.getData()){
                return new ResponseHeadVO<>(false,"文件上传失败,该文件已存在空间中,也许是文件名不一样");
            }
            byte [] total = f.getInputStream().readAllBytes();
            int off = 0;
            for (int i = 0; i < shardingNum; i++) {
                byte [] b = Arrays.copyOfRange(total,off,off + (int)shardingConfigSize);
                dto.setShardingSort(i);
                dto.setShardingStream(b);
                request.onNext(dto);
                off += b.length;
            }
            //发送最后一片
            byte [] b = Arrays.copyOfRange(total,off,off + (shardingConfigSize <= 0 ? (int)totalSize : (int)(totalSize % shardingConfigSize)));
            dto.setShardingSort(shardingNum);
            dto.setShardingStream(b);
            request.onNext(dto);
        }catch (Exception e){
            log.error("文件上传发生异常",e);
            return new ResponseHeadVO<>(false,"文件上传失败");
        }
        return new ResponseHeadVO<String>(true,dto.getFileMd5(),"文件已上传到服务器,正在写入到磁盘,稍后通知写入结果,请勿重复上传.");
    }
}
