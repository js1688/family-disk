package com.jflove.notebook.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.notebook.dto.NotebookNoteDTO;

/**
 * @author: tanjun
 * @date: 2023/2/6 4:49 PM
 * @desc: 笔记本
 */
public interface INoteService {

    /**
     * 获取笔记列表
     * @param spaceId
     * @param keyword
     * @param tag
     * @return
     */
    ResponseHeadDTO<NotebookNoteDTO> getList(long spaceId, String keyword,long tag);

    /**
     * 获取记事本text
     * @param spaceId
     * @param id
     * @return
     */
    ResponseHeadDTO<String> getText(long spaceId,long id);

    /**
     * 获取记事本html
     * @param spaceId
     * @param id
     * @return
     */
    ResponseHeadDTO<String> getHtml(long spaceId,long id);

    /**
     * 添加笔记
     * @param dto
     * @return
     */
    ResponseHeadDTO<Long> saveNote(NotebookNoteDTO dto);

    /**
     * 删除笔记
     * @param spaceId 所属空间id
     * @param id 笔记id
     * @return
     */
    ResponseHeadDTO del(long spaceId,long id);
}
