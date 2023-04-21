package com.jflove.stream.service.impl;

import com.jflove.ResponseHeadDTO;
import com.jflove.mapper.admin.FileDiskConfigMapper;
import com.jflove.po.file.FileDiskConfigPO;
import com.jflove.stream.dto.StreamReadParamDTO;
import com.jflove.stream.dto.StreamReadResultDTO;
import com.jflove.stream.dto.StreamWriteParamDTO;
import com.jflove.stream.service.IFileReadAndWrit;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author tanjun
 * @date 2022/12/14 18:34
 * @describe 本地磁盘读写实现类
 */
@Component(IFileReadAndWrit.BEAN_PREFIX + "LOCAL")
@Log4j2
public class LocalFileReadAndWritImpl implements IFileReadAndWrit {

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    @Value("${dubbo.protocol.payload}")
    private long payload;//dubbo传输大小

    @Override
    public ResponseHeadDTO<StreamReadResultDTO> readByte(StreamReadParamDTO dto, FileDiskConfigPO selectd) {
        String path = String.format("%s/%s%s", selectd.getPath(), dto.getFileMd5(), dto.getType());
        StreamReadResultDTO ret = new StreamReadResultDTO();
        ret.setParam(dto);
        try(RandomAccessFile raf = new RandomAccessFile(new File(path), "r")) {
            //自动修正读取位置
            if(dto.getRangeStart() > raf.length()) {
                return new ResponseHeadDTO<>(false,"读取位置超出了文件大小");
            }else if(dto.getReadLength() > payload){//如果读取长度大于了dubbo传输大小,则设置成3mb
                dto.setReadLength((int)DataSize.ofMegabytes(3).toBytes());
            }else if(dto.getRangeStart() == 0 && dto.getRangeStart() + dto.getReadLength() > raf.length()){
                dto.setReadLength((int)raf.length());
            }else if(dto.getRangeStart() + dto.getReadLength() > raf.length()){
                dto.setReadLength((int)(raf.length()-dto.getRangeStart()));
            }
            ret.setTotalSize(raf.length());
            raf.seek(dto.getRangeStart());
            byte [] b = new byte[(int)dto.getReadLength()];
            int len = raf.read(b);
            ret.setReadLength(len);
            ret.setStream(b);
            return new ResponseHeadDTO<>(ret);
        }catch (IOException e){
            log.error("读取文件异常",e);
            return new ResponseHeadDTO<>(false,"读取文件异常");
        }
    }

    @Override
    public ResponseHeadDTO<String> writByte(StreamWriteParamDTO dto,FileDiskConfigPO selectd, String  tempFileSuffix, String tempPath) {
        String path = String.format("%s/%s%s", selectd.getPath(), dto.getFileMd5(), dto.getType());
        try{
            Files.deleteIfExists(Path.of(path));//先删除历史数据
        }catch (IOException e){}
        try(RandomAccessFile raf = new RandomAccessFile(new File(path), "rw")){
            //文件传输完毕,开始执行临时分片合并
            for (int i = 0; i <= dto.getShardingNum(); i++) {
                byte [] f = Files.readAllBytes(Path.of(String.format("%s/%s-%s%s", tempPath, dto.getFileMd5(), i, tempFileSuffix)));
                raf.write(f);//支持追加写入
            }
            //刷新磁盘可使用空间
            File file = new File(selectd.getPath());
            DataSize total = DataSize.ofBytes(file.getTotalSpace());
            DataSize usableSpace = DataSize.ofBytes(file.getUsableSpace());//直接从磁盘中读取剩余可用空间,更加准确
            selectd.setMaxSize(total.toGigabytes());
            selectd.setUsableSize(usableSpace.toGigabytes());
            selectd.setUpdateTime(null);
            fileDiskConfigMapper.updateById(selectd);
            return new ResponseHeadDTO(true,dto.getFileMd5(),"所有分片合并成完整文件写入到磁盘成功");
        }catch (IOException e){
            log.error("文件分片合并写盘异常",e);
            return new ResponseHeadDTO<>(false,"文件分片合并写盘异常");
        }
    }
}
