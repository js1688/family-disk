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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

/**
 * @author tanjun
 * @date 2022/12/13 10:53
 * @describe
 */
@DubboService
@Log4j2
public class FileStreamServiceImpl implements IFileStreamService {

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
    @Transactional
    public ResponseHeadDTO<StreamWriteResultDTO> writeByte(StreamWriteParamDTO dto){
        try {
            //查找文件存储在哪个磁盘中
            FileInfoPO po = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                    .eq(FileInfoPO::getFileMd5,dto.getFileMd5())
                    .eq(FileInfoPO::getSpaceId,dto.getSpaceId())
                    .eq(FileInfoPO::getSource,dto.getSource().getCode())
            );
            FileDiskConfigPO selectd = null;
            if(po == null){//立即选择一个存储位置
                //查找出文件可以存放到哪个磁盘上
                selectd = fileDiskConfigMapper.selectOne(new LambdaQueryWrapper<FileDiskConfigPO>()
                        .orderByDesc(FileDiskConfigPO::getUsableSize)//按可用空间降序,取最大的存储
                        .last(" limit 1")
                );
                //判断是否还存的下,如果长度是long 最大值就不判断了,因为它是一个临时的未知值
                if(dto.getTotalSize() != Long.MAX_VALUE){
                    DataSize ds = DataSize.ofBytes(dto.getTotalSize());
                    if (ds.toGigabytes() > selectd.getUsableSize() - tempSpace.toGigabytes()) {
                        return new ResponseHeadDTO<>(false,String.format("文件写盘失败,服务器存储空间已不足%sGB.",tempSpace.toGigabytes()));
                    }
                }
                //将文件信息先写入到表中,临时存储,用于记录这个文件最开始就被选择了写入盘
                po = new FileInfoPO();
                po.setSource(dto.getSource().getCode());
                po.setSpaceId(dto.getSpaceId());
                po.setFileMd5(dto.getFileMd5());
                po.setBefore(1);//标记成上传之前,意味着它只是预占
                po.setDiskId(selectd.getId());
                po.setName(dto.getOriginalFileName());
                po.setSize(dto.getTotalSize());
                po.setMediaType(dto.getMediaType());
                po.setType(dto.getType());
                po.setCreateUserId(dto.getCreateUserId());
                fileInfoMapper.insert(po);
            }else{
                //查找磁盘
                selectd = fileDiskConfigMapper.selectById(po.getDiskId());
                if(selectd == null){
                    return new ResponseHeadDTO<>(false,"找不到文件存放的磁盘信息");
                }
            }

            IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(), IFileReadAndWrit.class);
            ResponseHeadDTO<StreamWriteResultDTO> result = fileReadAndWrit.writByte(dto, selectd);//将分片文件执行追加写盘
            if(result.isResult() && result.getData() != null){
                //文件全部写入了,将文件信息补全
                po.setUpdateTime(null);
                po.setBefore(0);//标记改成上传之后
                if(!StringUtils.hasLength(po.getMediaType())){//有可能前面的分片没有传媒体类型,更新一下,提高兼容性
                    po.setMediaType(result.getData().getMediaType());
                }
                if(po.getSize() == 0){//有可能前面分片没有传总长度,更新一下,提高兼容性
                    po.setSize(result.getData().getTotalSize());
                }
                fileInfoMapper.updateById(po);
            }
            return result;
        }catch (Exception e){
            log.error("分片写盘异常",e);
            return new ResponseHeadDTO<>(false,"分片写盘失败");
        }
    }
}
