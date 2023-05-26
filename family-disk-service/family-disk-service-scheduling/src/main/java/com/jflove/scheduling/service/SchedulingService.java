package com.jflove.scheduling.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: tanjun
 * @date: 2023/4/23 10:42 AM
 * @desc: 定时任务
 */
@Component
@EnableScheduling
public class SchedulingService {
    @Autowired
    private ClearTempFileService clearTempService;

    @Autowired
    private ClearExpireFileService clearExpireFileService;

    @Autowired
    private CorrectUseSpaceService correctUseSpaceService;

    @Autowired
    private LocalFileBakService localFileBakService;

    /**
     * 凌晨3点执行,
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void clearTemp() {
        clearTempService.run();//清除临时文件
        clearExpireFileService.run();//清除过期无引用文件
        correctUseSpaceService.run();//纠正用户空间使用量
    }

    /**
     * 文件备份
     * 只有本地存储方式的磁盘才有会被执行备份
     * 如果是使用那种文件存储系统,它们自己会更好的解决备份问题
     * 一小时执行一次
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void fileBak(){
        localFileBakService.run();//本地存储盘文件备份
    }
}
