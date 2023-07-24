package com.jflove.scheduling.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

/**
 * @author: tanjun
 * @date: 2023/4/23 11:32 AM
 * @desc: 清除临时文件
 */
@Service
@Log4j2
@EnableAsync
public class ClearTempFileService{
    @Value("${file.storage.path.temp}")
    private String tempPath;//分片文件临时存放
    public static final String  tempFileSuffix = ".temp";

    @Async("myTaskExecutor")
    public void run() {
        try {
            File[] fs = new File(tempPath).listFiles(e -> e.getName().endsWith(tempFileSuffix));
            for (File f : fs) {
                BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                FileTime time = attrs.lastModifiedTime();
                DateTime now = DateUtil.beginOfDay(new Date());
                if (now.getTime() > time.toMillis()) {//今天的开始时间戳大于了文件时间戳,则代表文件是需要被清理的过期临时文件
                    f.delete();
                }
            }
        }catch (Exception e){
            log.error("清除临时文件时发生异常",e);
        }
    }
}
