package com.jflove.share.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jflove.share.ShareLinkPO;
import com.jflove.share.mapper.ShareLinkMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * @author: tanjun
 * @date: 2023/2/10 5:12 PM
 * @desc: 清理过期的分享
 */
@Log4j2
@Component
@EnableScheduling
public class ShareClearService {

    @Autowired
    private ShareLinkMapper shareLinkMapper;

    @Scheduled(cron = "0 0 1 * * ?")
    public void clearOverdue() {
        //直接删除掉已经到期的分享链接
        long xz = System.currentTimeMillis() / 1000;
        shareLinkMapper.delete(new LambdaUpdateWrapper<ShareLinkPO>()
                .lt(ShareLinkPO::getInvalidTime, xz)//执行删除的时间小于了现在的时间,则代表可以删除了
        );
    }
}
