package com.jflove.mapper.journal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jflove.po.journal.JournalListPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: tanjun
 * @date: 2023/1/17 9:34 AM
 * @desc:
 */
@Mapper
public interface JournalListMapper extends BaseMapper<JournalListPO> {
}
