package com.jflove.scheduling.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jflove.mapper.share.ShareLinkMapper;
import com.jflove.po.share.ShareLinkPO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 * @author: tanjun
 * @date: 2023/2/10 5:12 PM
 * @desc: 清理过期的分享
 */
@Log4j2
@Service
public class ShareClearService implements Runnable{
    @Autowired
    private ShareLinkMapper shareLinkMapper;
    @Override
    @Async
    public void run() {
        try {
            //直接删除掉已经到期的分享链接
            long xz = System.currentTimeMillis() / 1000;
            shareLinkMapper.delete(new LambdaUpdateWrapper<ShareLinkPO>()
                    .lt(ShareLinkPO::getInvalidTime, xz)//执行删除的时间小于了现在的时间,则代表可以删除了
            );
        }catch (Throwable e){
            log.error("清理过期的分享链接发生异常",e);
        }
    }
}
