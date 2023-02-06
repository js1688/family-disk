package com.jflove.notebook.impl;

import com.jflove.ResponseHeadDTO;
import com.jflove.notebook.api.INoteService;
import com.jflove.notebook.dto.NotebookNoteDTO;
import com.jflove.notebook.mapper.NotebookNoteMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

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
        return null;
    }

    @Override
    public ResponseHeadDTO<Long> add(NotebookNoteDTO dto) {
        return null;
    }

    @Override
    public ResponseHeadDTO del(long spaceId, long id) {
        return null;
    }
}
