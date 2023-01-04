package com.jflove.file.service.impl;

import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.service.IFileReadAndWrit;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author tanjun
 * @date 2022/12/14 18:34
 * @describe 0 本地磁盘读写实现类
 */
@Component(IFileReadAndWrit.BEAN_PREFIX + "LOCAL")
@Log4j2
public class LocalFileReadAndWritImpl implements IFileReadAndWrit {

    @Override
    public void read(FileTransmissionDTO dto, FileDiskConfigPO selectd, StreamObserver<FileTransmissionDTO> response) {
        String path = String.format("%s/%s%s", selectd.getPath(), dto.getFileMd5(), dto.getType());
        try(RandomAccessFile raf = new RandomAccessFile(new File(path), "r")) {
            dto.setTotalSize(raf.length());
            long shardingConfigSize = DataSize.of(3, DataUnit.MEGABYTES).toBytes();//如果被分片,每片最多是3mb
            int shardingNum = shardingConfigSize <= 0 ? 0 : (int) (dto.getTotalSize() / shardingConfigSize);//本次分片个数
            dto.setShardingNum(shardingNum);
            for (int i = 0; i < shardingNum; i++) {
                byte [] b = new byte[(int) shardingConfigSize];
                raf.read(b);
                dto.setShardingSort(i);
                dto.setShardingStream(b);
                response.onNext(dto);
            }
            //发送最后一片
            byte[] b = new byte[shardingConfigSize <= 0 ? (int) dto.getTotalSize() : (int) (dto.getTotalSize() % shardingConfigSize)];
            raf.read(b);
            dto.setShardingSort(shardingNum);
            dto.setShardingStream(b);
            response.onNext(dto);
        }catch (IOException e){
            log.error("读取文件异常",e);
            response.onError(new RuntimeException("文件读取错误"));
        }
    }

    @Override
    public boolean writ(FileTransmissionDTO data, FileDiskConfigPO selectd,String  tempFileSuffix,String tempPath) {
        String path = String.format("%s/%s%s", selectd.getPath(), data.getFileMd5(), data.getType());
        try{
            Files.deleteIfExists(Path.of(path));//先删除历史数据
        }catch (IOException e){}
        try(RandomAccessFile raf = new RandomAccessFile(new File(path), "rw")){
            //文件传输完毕,开始执行临时分片合并
            for (int i = 0; i <= data.getShardingNum(); i++) {
                byte [] f = Files.readAllBytes(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(i), tempFileSuffix)));
                raf.write(f);//支持追加写入
            }
        }catch (IOException e){
            log.error("文件合并异常",e);
        }
        return false;
    }
}
