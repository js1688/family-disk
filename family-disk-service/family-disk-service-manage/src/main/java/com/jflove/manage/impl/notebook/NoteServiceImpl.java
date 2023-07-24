package com.jflove.manage.impl.notebook;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.po.notebook.NotebookNotePO;
import com.jflove.notebook.api.INoteService;
import com.jflove.notebook.dto.NotebookNoteDTO;
import com.jflove.mapper.notebook.NotebookNoteMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/2/6 5:00 PM
 * @desc:
 */
@DubboService
@Log4j2
public class NoteServiceImpl implements INoteService {
    @Autowired
    private NotebookNoteMapper notebookNoteMapper;

    @Override
    public ResponseHeadDTO<NotebookNoteDTO> getList(long spaceId, String keyword, long tag) {
        List<NotebookNotePO> listPO = notebookNoteMapper.selectList(new LambdaQueryWrapper<NotebookNotePO>()
                .eq(NotebookNotePO::getSpaceId,spaceId)
                .eq(tag != 0,NotebookNotePO::getTag,tag)
                .like(StringUtils.hasLength(keyword),NotebookNotePO::getKeyword,keyword)
                .orderByDesc(NotebookNotePO::getCreateTime)
                .select(NotebookNotePO::getId,
                        NotebookNotePO::getCreateTime,
                        NotebookNotePO::getUpdateTime,
                        NotebookNotePO::getKeyword,
                        NotebookNotePO::getSpaceId,
                        NotebookNotePO::getTag
                )
        );
        List<NotebookNoteDTO> dtoList = new ArrayList<>(listPO.size());
        listPO.forEach(v->{
            NotebookNoteDTO listDTO = new NotebookNoteDTO();
            BeanUtils.copyProperties(v,listDTO);
            dtoList.add(listDTO);
        });
        return new ResponseHeadDTO<>(true,dtoList,"查询成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO<Long> saveNote(NotebookNoteDTO dto) {
        NotebookNotePO po = new NotebookNotePO();
        BeanUtils.copyProperties(dto,po);
        int len = po.getText().length();
        String keyword = po.getText().substring(0, len > 20 ? 20 : len);
        po.setKeyword(keyword);
        if(po.getId() == 0) {
            notebookNoteMapper.insert(po);
        }else{//修改
            if(!notebookNoteMapper.exists(new LambdaQueryWrapper<NotebookNotePO>()
                    .eq(NotebookNotePO::getSpaceId,dto.getSpaceId())
                    .eq(NotebookNotePO::getId,dto.getId())
            )){
                return new ResponseHeadDTO(false,"笔记不存在");
            }
            po.setUpdateTime(null);
            po.setCreateTime(null);
            notebookNoteMapper.updateById(po);
        }
        return new ResponseHeadDTO<>(true,po.getId(),"笔记保存成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO del(long spaceId, long id) {
        if(!notebookNoteMapper.exists(new LambdaQueryWrapper<NotebookNotePO>()
                .eq(NotebookNotePO::getSpaceId,spaceId)
                .eq(NotebookNotePO::getId,id)
        )){
            return new ResponseHeadDTO<>(false,"","笔记不存在");
        }
        notebookNoteMapper.deleteById(id);
        return new ResponseHeadDTO<>(true,"","删除成功");
    }

    @Override
    public ResponseHeadDTO<String> getText(long spaceId, long id) {
        NotebookNotePO po = notebookNoteMapper.selectById(id);
        if(po == null){
            return new ResponseHeadDTO<>(false,"","笔记不存在");
        }
        return new ResponseHeadDTO<>(true,po.getText(),"查询成功");
    }

    @Override
    public ResponseHeadDTO<String> getHtml(long spaceId, long id) {
        return null;
    }
}
