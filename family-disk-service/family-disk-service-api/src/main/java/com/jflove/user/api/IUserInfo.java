package com.jflove.user.api;


import com.jflove.user.dto.UserInfoDTO;

/**
 * @author tanjun
 * @date 2022/12/7 11:54
 * @describe
 */
public interface IUserInfo {

    /**
     * 按邮箱+密码查询用户信息
     * @param email
     * @param password
     * @return
     */
    UserInfoDTO emailPasswordLogin(String email,String password);

    /**
     * 根据邮箱获取账号信息
     * @param email
     * @return
     */
    UserInfoDTO getUserInfoByEmail(String email);
}
