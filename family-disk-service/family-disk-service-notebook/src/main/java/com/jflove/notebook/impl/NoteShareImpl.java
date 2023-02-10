package com.jflove.notebook.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.notebook.NotebookNotePO;
import com.jflove.notebook.mapper.NotebookNoteMapper;
import com.jflove.notebook.mapper.ShareLinkMapper;
import com.jflove.share.ShareLinkPO;
import com.jflove.share.api.INoteShare;
import com.jflove.share.dto.ShareLinkDTO;
import com.jflove.share.em.ShareBodyTypeENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public ResponseHeadDTO<String> create(String password, long bodyId, long spaceId, String invalidTime) {
        if(!notebookNoteMapper.exists(new LambdaQueryWrapper<NotebookNotePO>()
                .eq(NotebookNotePO::getId,bodyId)
                .eq(NotebookNotePO::getSpaceId,spaceId)
        )){
            return new ResponseHeadDTO<>(false,"笔记不存在");
        }
        String uuid = UUID.randomUUID().toString();
        long dqsj = DateUtil.parse(invalidTime, DatePattern.NORM_DATETIME_PATTERN).getTime() / 1000;
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

    @Override
    public ResponseHeadDTO<String> getBody(String uuid, String password) {
        ShareLinkPO po = shareLinkMapper.selectOne(new LambdaQueryWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getUuid,uuid)
                .eq(ShareLinkPO::getPassword, Optional.ofNullable(password).orElse(""))
        );
        if(po == null){
            if(StringUtils.hasLength(password)){
                return new ResponseHeadDTO<>(false,"分享已失效或密码错误,请刷新网页后重试.");
            }
            return new ResponseHeadDTO<>(false,"分享已失效或不存在.");
        }
        NotebookNotePO nnp = notebookNoteMapper.selectById(po.getBodyId());
        if(nnp == null){
            return new ResponseHeadDTO<>(false,"分享的内容已经被删除了");
        }
        return new ResponseHeadDTO<String>(nnp.getText());
    }

    @Override
    public ResponseHeadDTO delLink(long id, long spaceId) {
        if(!shareLinkMapper.exists(new LambdaQueryWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getSpaceId,spaceId)
                .eq(ShareLinkPO::getId,id)
        )){
            return new ResponseHeadDTO<>(false,"","链接不存在");
        }
        shareLinkMapper.deleteById(id);
        return new ResponseHeadDTO<>(true,"","删除成功");
    }

    @Override
    public ResponseHeadDTO<ShareLinkDTO> getLinkList(long spaceId) {
        List<ShareLinkPO> pos =  shareLinkMapper.selectList(new LambdaQueryWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getSpaceId,spaceId)
        );
        List<ShareLinkDTO> dtoList = new ArrayList<>(pos.size());
        pos.forEach(v->{
            ShareLinkDTO listDTO = new ShareLinkDTO();
            BeanUtils.copyProperties(v,listDTO);
            listDTO.setBodyType(ShareBodyTypeENUM.valueOf(v.getBodyType()));
            listDTO.setKeyword("分享的内容已经被删除");
            switch (listDTO.getBodyType()){
                case NOTE:
                    NotebookNotePO notePO = notebookNoteMapper.selectOne(new LambdaQueryWrapper<NotebookNotePO>()
                            .eq(NotebookNotePO::getId,v.getBodyId())
                            .select(NotebookNotePO::getKeyword)
                    );
                    if(notePO != null){
                        listDTO.setKeyword(notePO.getKeyword());
                    }
            }
            dtoList.add(listDTO);
        });
        return new ResponseHeadDTO<>(true,dtoList,"查询成功");
    }
}
