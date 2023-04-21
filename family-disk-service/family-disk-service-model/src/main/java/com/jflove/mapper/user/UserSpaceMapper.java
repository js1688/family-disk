package com.jflove.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jflove.po.user.UserSpacePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tanjun
 * @date 2022/12/14 10:33
 * @describe 用户空间
 */
@Mapper
public interface UserSpaceMapper extends BaseMapper<UserSpacePO> {
}
