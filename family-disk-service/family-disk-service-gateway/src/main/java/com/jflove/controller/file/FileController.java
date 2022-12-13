package com.jflove.controller.file;

import com.jflove.config.HttpConstantConfig;
import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.vo.ResponseHeadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author tanjun
 * @date 2022/12/13 11:07
 * @describe 文件管理
 */
@RestController
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequestMapping("/file")
@Api(tags = "文件管理")
@Log4j2
public class FileController {
    @DubboReference
    private IFileService fileService;
    @Autowired
    private HttpServletRequest autowiredRequest;

//    @ApiOperation(value = "上传文件,分片多线程上传")
//    @PostMapping("/addFileSharding")
//    public ResponseHeadVO<String> addFile(@ApiParam("文件流") @RequestPart MultipartFile file){
//        //todo 每次上传的都是一个文件的分片,临时文件,最终多个分片合并,这个还没有想好怎么实现
//        return new ResponseHeadVO<>(false,"还未实现");
//    }

    @ApiOperation(value = "上传文件(完整文件,非分片)")
    @PostMapping("/addFile")
    public ResponseHeadVO<String> addFile(@ApiParam("文件流") @RequestPart("f") MultipartFile f,
                                          @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘)") @RequestParam("s") String s){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        if(useUserId == null || useSpaceId == null){
            throw new SecurityException("错误的请求");
        }
        //将文件分片后发送过去
        long totalSize = f.getSize();//文件总大小
        int shardingConfigSize = 1024 * 1024 * 1;//分片大小配置(MB)
        int shardingNum = (int) (totalSize / shardingConfigSize);//本次分片个数
        FileTransmissionDTO dto = new FileTransmissionDTO();
        dto.setName(f.getOriginalFilename());
        dto.setTotalSize(totalSize);
        dto.setCode(UUID.randomUUID().toString());
        dto.setShardingNum(shardingNum);
        dto.setSource(FileSourceENUM.valueOf(s));
        dto.setType(dto.getName().substring(dto.getName().lastIndexOf(".")));
        dto.setSpaceId(useSpaceId);
        dto.setCreateUserId(useUserId);
        //按分片读取数据,再将数据发送出去
        StreamObserver<FileTransmissionDTO> request = fileService.addFile(null);
        try {
            for (int i = 0; i < shardingNum - 1; i++) {
                byte [] b = new byte[shardingConfigSize];
                f.getInputStream().read(b);
                dto.setShardingSort(i);
                dto.setShardingStream(b);
                request.onNext(dto);
            }
            //发送最后一片
            byte [] b = new byte[(int)totalSize % shardingConfigSize];
            dto.setShardingSort(shardingNum);
            dto.setShardingStream(b);
            request.onNext(dto);
            request.onCompleted();
        }catch (Exception e){
            log.error("文件上传发生异常",e);
            return new ResponseHeadVO<>(false,"文件上传失败");
        }
        return new ResponseHeadVO<String>(true,dto.getCode(),"文件上传成功,稍后主动根据文件编码查询文件是否写入磁盘成功");
    }
}
