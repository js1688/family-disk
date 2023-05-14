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
    public ResponseHeadDTO<String> writByte(StreamWriteParamDTO dto,FileDiskConfigPO selectd) {
        String path = String.format("%s/%s%s", selectd.getPath(), dto.getFileMd5(), dto.getType());
        try(RandomAccessFile raf = new RandomAccessFile(new File(path), "rw")){
            long l = raf.length();
            raf.seek(l);
            raf.write(dto.getStream());//支持追加写入
            //判断如果文件已经全部写入,则更新一下磁盘大小
            if((l + dto.getStream().length) >= dto.getTotalSize()) {
                //刷新磁盘可使用空间
                File file = new File(selectd.getPath());
                DataSize total = DataSize.ofBytes(file.getTotalSpace());
                DataSize usableSpace = DataSize.ofBytes(file.getUsableSpace());//直接从磁盘中读取剩余可用空间,更加准确
                selectd.setMaxSize(total.toGigabytes());
                selectd.setUsableSize(usableSpace.toGigabytes());
                selectd.setUpdateTime(null);
                fileDiskConfigMapper.updateById(selectd);
                //完整文件写盘成功,会在data中带上md5值,这个可以区分是不是完整文件写盘结束
                return new ResponseHeadDTO(true,dto.getFileMd5(),"文件分片全部写盘成功");
            }
            return new ResponseHeadDTO(true,"文件分片写盘成功");
        }catch (IOException e){
            log.error("文件分片写盘异常",e);
            return new ResponseHeadDTO<>(false,"文件分片写盘异常");
        }
    }
}
