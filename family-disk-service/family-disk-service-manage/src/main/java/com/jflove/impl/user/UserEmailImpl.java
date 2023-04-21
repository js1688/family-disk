package com.jflove.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.email.api.IEmailService;
import com.jflove.email.dto.EmailDetailsDTO;
import com.jflove.email.em.EmailSubjectCodeENUM;
import com.jflove.mapper.user.UserCaptchaMapper;
import com.jflove.mapper.user.UserInfoMapper;
import com.jflove.po.user.UserCaptchaPO;
import com.jflove.po.user.UserInfoPO;
import com.jflove.user.api.IUserEmail;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author tanjun
 * @date 2022/12/9 14:11
 * @describe
 */
@DubboService
@Log4j2
public class UserEmailImpl implements IUserEmail {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserCaptchaMapper userCaptchaMapper;

    @Autowired
    private IEmailService emailService;

    @Override
    @Transactional
    public ResponseHeadDTO<String> sendRegisterEmailCaptcha(String email) {
        EmailDetailsDTO dto = new EmailDetailsDTO();
        //查询账号是否已被注册
        if(userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfoPO>()
                .eq(UserInfoPO::getEmail,email)
        ) > 0){
            return new ResponseHeadDTO<String>(false,"邮箱已被注册");
        }
        long thisTime = System.currentTimeMillis() / 1000;//只需要精确到秒
        //查询生成的验证码是否还未到期
        UserCaptchaPO oldCaptcha = userCaptchaMapper.selectOne(new LambdaQueryWrapper<UserCaptchaPO>()
                .eq(UserCaptchaPO::getEmail,email)
                .gt(UserCaptchaPO::getCaptchaExpireTime,thisTime)
                .select(UserCaptchaPO::getCaptcha,UserCaptchaPO::getId)
        );
        String captcha = null;
        int exp = 5;//五分钟到期
        long expTime = thisTime + (5 * 60);//本次的验证码到期时间戳
        if(oldCaptcha != null){
            captcha = oldCaptcha.getCaptcha();
            //重新刷新有效期
            oldCaptcha.setCaptchaExpireTime(expTime);
            oldCaptcha.setUpdateTime(null);
            userCaptchaMapper.updateById(oldCaptcha);
        }else{
            userCaptchaMapper.delete(new LambdaUpdateWrapper<UserCaptchaPO>().eq(UserCaptchaPO::getEmail,email));//删除掉以前的验证码
            String random = String.valueOf(Math.random());
            captcha = random.substring(random.length() - 6,random.length());
            //将新生成的验证码存储起来
            UserCaptchaPO newCaptcha = new UserCaptchaPO();
            newCaptcha.setEmail(email);
            newCaptcha.setCaptcha(captcha);
            newCaptcha.setCaptchaExpireTime(expTime);
            userCaptchaMapper.insert(newCaptcha);
        }
        dto.setSubject(EmailSubjectCodeENUM.CAPTCHA);
        dto.setMsgBody(String.format("您好本次注册验证码[%s],用于注册家庭网盘用户,%s分钟后失效",captcha,String.valueOf(exp)));
        dto.setRecipient(email);
        return emailService.sendSimpleMail(dto);
    }
}
