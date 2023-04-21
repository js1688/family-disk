package com.jflove.stream.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.mapper.admin.FileDiskConfigMapper;
import com.jflove.mapper.file.FileInfoMapper;
import com.jflove.po.file.FileDiskConfigPO;
import com.jflove.po.file.FileInfoPO;
import com.jflove.stream.api.IFileStreamService;
import com.jflove.stream.dto.StreamReadParamDTO;
import com.jflove.stream.dto.StreamReadResultDTO;
import com.jflove.stream.dto.StreamWriteParamDTO;
import com.jflove.stream.dto.StreamWriteResultDTO;
import com.jflove.stream.service.IFileReadAndWrit;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author tanjun
 * @date 2022/12/13 10:53
 * @describe
 */
@DubboService
@Log4j2
public class FileStreamServiceImpl implements IFileStreamService {

    @Value("${file.storage.path.temp}")
    private String tempPath;//分片文件临时存放

    public static final String  tempFileSuffix = ".temp";

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${file.storage.space}")
    private DataSize tempSpace;//临时空间占用大小

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    public ResponseHeadDTO<StreamReadResultDTO> readByte(StreamReadParamDTO dto){
        try {
            //查找文件存储在哪个磁盘中
            FileInfoPO po = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                    .eq(FileInfoPO::getFileMd5,dto.getFileMd5())
                    .eq(FileInfoPO::getSpaceId,dto.getSpaceId())
                    .eq(FileInfoPO::getSource,dto.getSource().getCode())
            );

            if(po == null){
                return new ResponseHeadDTO<>(false,"找不到文件信息");
            }
            dto.setType(po.getType());
            dto.setMediaType(po.getMediaType());
            //查找磁盘
            FileDiskConfigPO selectd = fileDiskConfigMapper.selectById(po.getDiskId());
            if(selectd == null){
                return new ResponseHeadDTO<>(false,"找不到文件存放的磁盘信息");
            }
            IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(), IFileReadAndWrit.class);
            return fileReadAndWrit.readByte(dto,selectd);
        }catch (Exception e){
            return new ResponseHeadDTO<>(false,"读取文件流失败");
        }
    }

    @Override
    public ResponseHeadDTO<StreamWriteResultDTO> writeByte(StreamWriteParamDTO dto){
        try {
            //将分片文件写入临时目录
            Path p = Path.of(String.format("%s/%s-%s%s", tempPath,dto.getFileMd5(), dto.getShardingSort(), tempFileSuffix));
            if(Files.notExists(p)) {
                Files.write(p, dto.getStream());
            }
            //判断所有分片是否都写入到临时目录了
            boolean ok = true;
            for (int i = 0; i < dto.getShardingNum(); i++) {
                if(Files.notExists(Path.of(String.format("%s/%s-%s%s", tempPath,dto.getFileMd5(), i, tempFileSuffix)))){
                    ok = false;
                    break;
                }
            }
            if (ok){//开始合并
                DataSize ds = DataSize.ofBytes(dto.getTotalSize());
                //查找出文件可以存放到哪个磁盘上
                FileDiskConfigPO selectd = fileDiskConfigMapper.selectOne(new LambdaQueryWrapper<FileDiskConfigPO>()
                        .orderByDesc(FileDiskConfigPO::getUsableSize)//按可用空间降序,取最大的存储
                        .last(" limit 1")
                );
                //判断是否还存的下
                if (ds.toGigabytes() > selectd.getUsableSize() - tempSpace.toGigabytes()) {
                    return new ResponseHeadDTO<>(false,String.format("文件写盘失败,服务器存储空间已不足%sGB.",tempSpace.toGigabytes()));
                }

                IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(), IFileReadAndWrit.class);
                ResponseHeadDTO<String> result = fileReadAndWrit.writByte(dto, selectd,tempFileSuffix, tempPath);//写盘
                if (result.isResult()) {//写盘成功,清除临时文件
                    //清除临时文件
                    for (int i = 0; i <= dto.getShardingNum(); i++) {
                        try {
                            Files.deleteIfExists(Path.of(String.format("%s/%s-%s%s", tempPath, dto.getFileMd5(), i, tempFileSuffix)));
                        } catch (IOException e) {
                        }
                    }
                    StreamWriteResultDTO rd = new StreamWriteResultDTO();
                    rd.setFileMd5(result.getData());
                    rd.setDiskId(selectd.getId());
                    //完整文件写盘成功,会在data中带上md5值,这个可以区分是不是完整文件写盘结束
                    return new ResponseHeadDTO<>(true,rd,result.getMessage());
                }
                return new ResponseHeadDTO<>(result.isResult(),result.getMessage());
            }else{
                return new ResponseHeadDTO(true,"分片写入成功");
            }
        }catch (IOException e){
            log.error("分片写盘异常",e);
            return new ResponseHeadDTO<>(false,"分片写盘失败");
        }
    }
}
