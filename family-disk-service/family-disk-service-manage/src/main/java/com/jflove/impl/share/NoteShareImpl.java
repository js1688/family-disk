package com.jflove.impl.share;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.mapper.notebook.NotebookNoteMapper;
import com.jflove.mapper.share.ShareLinkMapper;
import com.jflove.po.notebook.NotebookNotePO;
import com.jflove.po.share.ShareLinkPO;
import com.jflove.share.api.INoteShare;
import com.jflove.share.em.ShareBodyTypeENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Optional;

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
    public ResponseHeadDTO<String> getBody(String uuid, String password) {
        ShareLinkPO po = shareLinkMapper.selectOne(new LambdaQueryWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getUuid,uuid)
                .eq(ShareLinkPO::getPassword, Optional.ofNullable(password).orElse(""))
                .eq(ShareLinkPO::getBodyType, ShareBodyTypeENUM.NOTE.getCode())
        );
        if(po == null){
            if(StringUtils.hasLength(password)){
                return new ResponseHeadDTO<>(false,"分享已失效或密码错误,请刷新网页后重试.");
            }
            return new ResponseHeadDTO<>(false,"分享已失效.");
        }
        NotebookNotePO nnp = notebookNoteMapper.selectById(po.getBodyId());
        if(nnp == null){
            return new ResponseHeadDTO<>(false,"分享的内容已经被删除了");
        }
        return new ResponseHeadDTO<String>(nnp.getText());
    }
}
