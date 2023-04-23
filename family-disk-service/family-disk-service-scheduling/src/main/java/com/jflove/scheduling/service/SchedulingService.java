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

    /**
     * 凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void clearTemp() {
        clearTempService.run();//清除临时文件
        clearExpireFileService.run();//清除过期无引用文件
        correctUseSpaceService.run();//纠正用户空间使用量
    }
}
