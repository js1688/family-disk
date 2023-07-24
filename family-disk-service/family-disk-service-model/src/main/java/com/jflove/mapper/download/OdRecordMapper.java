package com.jflove.mapper.download;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jflove.po.download.OdRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: tanjun
 * @date: 2023/7/19 11:54 AM
 * @desc: 离线下载
 */
@Mapper
public interface OdRecordMapper extends BaseMapper<OdRecordPO> {
}
