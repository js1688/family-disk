package com.jflove.user.api;


import com.jflove.ResponseHeadDTO;
import com.jflove.user.dto.UserInfoDTO;

/**
 * @author tanjun
 * @date 2022/12/7 11:54
 * @describe 用户管理
 */
public interface IUserInfo {

    /**
     * 按邮箱+密码查询用户信息
     * @param email
     * @param password
     * @return
     */
    ResponseHeadDTO<UserInfoDTO> emailPasswordLogin(String email, String password);

    /**
     * 根据邮箱获取账号信息
     * @param email
     * @return
     */
    ResponseHeadDTO<UserInfoDTO> getUserInfoByEmail(String email);

    /**
     * 创建账号
     * @param email
     * @param password
     * @param name
     * @param captcha 验证码
     * @return
     */
    ResponseHeadDTO<UserInfoDTO> createUserInfo(String email,String password,String name,String captcha);
}
