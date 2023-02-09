package com.jflove.notebook.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.notebook.NotebookNotePO;
import com.jflove.notebook.mapper.NotebookNoteMapper;
import com.jflove.notebook.mapper.ShareLinkMapper;
import com.jflove.share.ShareLinkPO;
import com.jflove.share.api.INoteShare;
import com.jflove.share.em.ShareBodyTypeENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

/**
 * @author: tanjun
 * @date: 2023/2/9 5:54 PM
 * @desc:
 */
@DubboService
@Log4j2
public class NoteShareImpl implements INoteShare {
    @Autowired
    private ShareLinkMapper shareLinkMapper;

    @Autowired
    private NotebookNoteMapper notebookNoteMapper;

    @Override
    public ResponseHeadDTO<String> create(String password, long bodyId, long spaceId, Date invalidTime) {
        if(!notebookNoteMapper.exists(new LambdaQueryWrapper<NotebookNotePO>()
                .eq(NotebookNotePO::getId,bodyId)
                .eq(NotebookNotePO::getSpaceId,spaceId)
        )){
            return new ResponseHeadDTO<>(false,"笔记不存在");
        }
        String uuid = UUID.randomUUID().toString();
        long dqsj = invalidTime.getTime() / 1000;
        ShareLinkPO po = new ShareLinkPO();
        po.setBodyType(ShareBodyTypeENUM.NOTE.getCode());
        po.setBodyId(bodyId);
        po.setPassword(password);
        po.setSpaceId(spaceId);
        po.setInvalidTime(dqsj);
        po.setUuid(uuid);
        shareLinkMapper.insert(po);
        String link = String.format("lock=%s&uuid=%s", StringUtils.hasLength(password),uuid);
        return new ResponseHeadDTO<>(true,link,"创建分享成功,地址已复制");
    }
}
