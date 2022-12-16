package com.jflove.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.user.UserCaptchaPO;
import com.jflove.user.UserInfoPO;
import com.jflove.user.UserSpaceRelPO;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.mapper.UserCaptchaMapper;
import com.jflove.user.mapper.UserInfoMapper;
import com.jflove.user.mapper.UserSpaceRelMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private UserCaptchaMapper userCaptchaMapper;

    @Autowired
    private UserSpaceRelMapper userSpaceRelMapper;

    @Override
    public ResponseHeadDTO<UserInfoDTO> getUserInfoByEmail(String email) {
        UserInfoPO po = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoPO>()
                .eq(UserInfoPO::getEmail,email)
        );
        if(po == null){
            return new ResponseHeadDTO<UserInfoDTO>(false,"用户不存在");
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(po,dto);
        List<UserSpaceRelPO> spacesPO = userSpaceRelMapper.selectList(new LambdaQueryWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getUserId,po.getId())
        );
        List<UserSpaceRelDTO> spacesDTOs = new ArrayList<>(spacesPO.size());
        spacesPO.forEach(v->{
            UserSpaceRelDTO spacesDto = new UserSpaceRelDTO();
            BeanUtils.copyProperties(v,spacesDto);
            spacesDTOs.add(spacesDto);
        });

        dto.setSpaces(spacesDTOs);
        return new ResponseHeadDTO<UserInfoDTO>(dto);
    }

    @Override
    public ResponseHeadDTO<UserInfoDTO> emailPasswordLogin(String email,String password) {
        UserInfoPO po = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoPO>()
                .eq(UserInfoPO::getEmail,email)
                .eq(UserInfoPO::getPassword,password)
        );
        if(po == null){
            return new ResponseHeadDTO<UserInfoDTO>(false,"账号不存在或密码不正确");
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(po,dto);
        return new ResponseHeadDTO<UserInfoDTO>(dto);
    }

    @Override
    @Transactional
    public ResponseHeadDTO<UserInfoDTO> createUserInfo(String email, String password,String name, String captcha) {
        //查询账号是否已被注册
        if(userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfoPO>()
                .eq(UserInfoPO::getEmail,email)
        ) > 0){
            return new ResponseHeadDTO<UserInfoDTO>(false,"邮箱已被注册");
        }
        //验证码校验
        long thisTime = System.currentTimeMillis() / 1000;//只需要精确到秒
        if(userCaptchaMapper.selectCount(new LambdaQueryWrapper<UserCaptchaPO>()
                .eq(UserCaptchaPO::getEmail,email)
                .eq(UserCaptchaPO::getCaptcha,captcha)
                .gt(UserCaptchaPO::getCaptchaExpireTime,thisTime)
        ) == 0){
            return new ResponseHeadDTO<UserInfoDTO>(false,"验证码无效,请重新获取验证码.");
        }
        //注册账号
        UserInfoPO uip = new UserInfoPO();
        uip.setName(name);
        uip.setEmail(email);
        uip.setPassword(password);
        userInfoMapper.insert(uip);
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(uip,dto);
        return new ResponseHeadDTO<>(true,dto,"账号注册成功");
    }
}
