package com.jflove.file.service.impl;

import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.service.IFileReadAndWrit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

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
@Component("FileReadAndWrit0")
@Log4j2
public class LocalFileReadAndWritImpl implements IFileReadAndWrit {

    @Override
    public boolean writ(FileTransmissionDTO data, FileDiskConfigPO selectd,String  tempFileSuffix,String tempPath) {
        RandomAccessFile raf = null;
        try {
            //文件传输完毕,开始执行临时分片合并
            String path = String.format("%s/%s%s", selectd.getPath(), data.getFileMd5(), data.getType());
            Files.deleteIfExists(Path.of(path));//先删除历史数据
            raf = new RandomAccessFile(new File(path), "rw");
            for (int i = 0; i <= data.getShardingNum(); i++) {
                byte [] f = Files.readAllBytes(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(i), tempFileSuffix)));
                raf.write(f);//支持追加写入
            }
        }catch (IOException e){
            log.error("文件合并异常",e);
        }finally {
            if(raf != null){
                try {
                    raf.close();
                }catch (IOException e){}
            }
        }
        return false;
    }
}
