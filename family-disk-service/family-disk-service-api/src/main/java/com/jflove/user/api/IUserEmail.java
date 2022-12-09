package com.jflove.user.api;

import com.jflove.ResponseHeadDTO;

/**
 * @author tanjun
 * @date 2022/12/9 14:10
 * @describe 用户邮件发送
 */
public interface IUserEmail {


    /**
     * 注册验证码
     * @param email
     * @return
     */
    ResponseHeadDTO<String> sendRegisterEmailCaptcha(String email);
}
