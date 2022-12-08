package com.jflove.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jflove.user.UserInfoPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tanjun
 * @date 2022/12/7 15:58
 * @describe
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfoPO> {
}
