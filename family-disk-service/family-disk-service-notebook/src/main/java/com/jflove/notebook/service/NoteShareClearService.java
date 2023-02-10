package com.jflove.notebook.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jflove.notebook.mapper.ShareLinkMapper;
import com.jflove.share.ShareLinkPO;
import com.jflove.share.em.ShareBodyTypeENUM;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * @author: tanjun
 * @date: 2023/2/10 5:12 PM
 * @desc: 清理过期的笔记分享
 */
@Log4j2
@Component
@EnableScheduling
public class NoteShareClearService {

    @Autowired
    private ShareLinkMapper shareLinkMapper;

    @Scheduled(cron = "0 0 1 * * ?")
    public void clearOverdue() {
        //直接删除掉已经到期的分享链接
        long xz = System.currentTimeMillis() / 1000;
        shareLinkMapper.delete(new LambdaUpdateWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getBodyType, ShareBodyTypeENUM.NOTE.getCode())//只清理笔记这一种数据
                .lt(ShareLinkPO::getInvalidTime, xz)//执行删除的时间小于了现在的时间,则代表可以删除了
        );
    }
}
