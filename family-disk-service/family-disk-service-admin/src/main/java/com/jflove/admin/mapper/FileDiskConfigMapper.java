package com.jflove.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jflove.file.FileDiskConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tanjun
 * @date 2022/12/12 10:55
 * @describe 可存储文件的磁盘配置
 */
@Mapper
public interface FileDiskConfigMapper extends BaseMapper<FileDiskConfigPO> {
}
