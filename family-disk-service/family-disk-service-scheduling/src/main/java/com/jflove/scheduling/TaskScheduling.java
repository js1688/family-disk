package com.jflove.scheduling;

import com.jflove.scheduling.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
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
@EnableAsync
public class TaskScheduling {

    @Autowired
    private ClearExpireFileService clearExpireFileService;

    @Autowired
    private CorrectUseSpaceService correctUseSpaceService;
    @Autowired
    private ShareClearService shareClearService;
    @Autowired
    private OfflineDownloadService offlineDownloadService;

    /**
     * 一分钟执行一次
     */
    @Scheduled(fixedDelay = 1000 * 60)
    public void clearLink(){
        shareClearService.run();//清除分享链接
    }

    /**
     * 10秒钟执行一次
     */
    @Scheduled(fixedDelay = 10000)
    public void offlineDownload(){
        offlineDownloadService.run();//离线下载,尝试将下载完成的任务转存
    }

    /**
     * 凌晨3点执行,
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void clearTemp() {
        clearExpireFileService.run();//清除过期无引用文件
        correctUseSpaceService.run();//纠正用户空间使用量
    }
}
