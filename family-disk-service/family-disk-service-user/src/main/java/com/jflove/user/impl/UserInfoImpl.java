package com.jflove.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.user.UserInfoPO;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.mapper.UserInfoMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author tanjun
 * @date 2022/12/7 12:04
 * @describe
 */
@DubboService
@Log4j2
public class UserInfoImpl implements IUserInfo {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfoDTO getUserInfoByEmail(String email) {
        UserInfoPO po = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoPO>()
                .eq(UserInfoPO::getEmail,email)
        );
        if(po == null){
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(po,dto);
        return dto;
    }

    @Override
    public UserInfoDTO emailPasswordLogin(String email,String password) {
        UserInfoPO po = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoPO>()
                .eq(UserInfoPO::getEmail,email)
                .eq(UserInfoPO::getPassword,password)
        );
        if(po == null){
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(po,dto);
        return dto;
    }


}
