package com.jflove.journal.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.journal.dto.JournalListDTO;

/**
 * @author: tanjun
 * @date: 2023/1/17 9:45 AM
 * @desc: 日记
 */
public interface IJournalList {

    /**
     * 查询日记列表
     * @param spaceId 所属空间id
     * @param keyword 筛选关键字
     * @return
     */
    ResponseHeadDTO<JournalListDTO> getList(long spaceId, String keyword);

    /**
     * 添加日记
     * @param dto
     * @return
     */
    ResponseHeadDTO<Long> add(JournalListDTO dto);

    /**
     * 删除日记
     * @param spaceId 所属空间id
     * @param id 日记id
     * @return
     */
    ResponseHeadDTO del(long spaceId,long id);
}
