package com.jflove.manage.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.mapper.user.UserCaptchaMapper;
import com.jflove.mapper.user.UserInfoMapper;
import com.jflove.mapper.user.UserSpaceRelMapper;
import com.jflove.po.user.UserCaptchaPO;
import com.jflove.po.user.UserInfoPO;
import com.jflove.po.user.UserSpaceRelPO;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserRelStateENUM;
import com.jflove.user.em.UserRoleENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private IUserSpace userSpace;

    @Value("${user.open.register:true}")
    private boolean openRegister;

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
                .in(UserSpaceRelPO::getState,UserRelStateENUM.USE.getCode(),UserRelStateENUM.NOTUSED.getCode())//只查正在使用或者未使用的关系,过滤掉审批中的
        );
        List<UserSpaceRelDTO> spacesDTOs = new ArrayList<>(spacesPO.size());
        spacesPO.forEach(v->{
            UserSpaceRelDTO spacesDto = new UserSpaceRelDTO();
            BeanUtils.copyProperties(v, spacesDto);
            spacesDto.setRole(UserSpaceRoleENUM.valueOf(v.getRole()));
            spacesDto.setState(UserRelStateENUM.valueOf(v.getState()));
            spacesDTOs.add(spacesDto);
        });
        dto.setRole(UserRoleENUM.valueOf(po.getRole()));
        dto.setSpaces(spacesDTOs);
        return new ResponseHeadDTO<>(dto);
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
        dto.setRole(UserRoleENUM.valueOf(po.getRole()));
        return new ResponseHeadDTO<UserInfoDTO>(dto);
    }

    @Override
    @Transactional
    public ResponseHeadDTO<UserInfoDTO> createUserInfo(String email, String password,String name, String captcha) {
        if(!openRegister){
            return new ResponseHeadDTO<UserInfoDTO>(false,"注册失败,这是私人环境,不允许公众用户注册.");
        }
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
        uip.setRole(UserRoleENUM.COMMON.getCode());
        //如果没有管理员账号,则默认设置成管理员账号,管理员账号只允许一个
        if(userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfoPO>()
                .eq(UserInfoPO::getRole,UserRoleENUM.ADMIN.getCode())
        ) == 0){
            uip.setRole(UserRoleENUM.ADMIN.getCode());
        }
        userInfoMapper.insert(uip);
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(uip,dto);
        dto.setRole(UserRoleENUM.valueOf(uip.getRole()));
        //自动创建空间,如果创建不成功可手动创建
        userSpace.createSpace(uip.getId(),String.format("%s的空间",uip.getName()));
        return new ResponseHeadDTO<>(true,dto,"账号注册成功");
    }
}
