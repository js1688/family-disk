package com.jflove.gateway.config;

import org.springframework.http.HttpHeaders;

/**
 * @author tanjun
 * @date 2022/12/13 17:50
 * @describe 网管层常用字段
 */
public class HttpConstantConfig extends HttpHeaders {

    /**
     * 正在使用的空间id
     */
    public static final String USE_SPACE_ID = "USE_SPACE_ID";

    /**
     * 对正在使用的空间,权限级别
     */
    public static final String USE_SPACE_ROLE = "USE_SPACE_ROLE";

    /**
     * 正在使用的用户id
     */
    public static final String USE_USER_ID = "USE_USER_ID";

    /**
     * 正在使用的用户邮箱
     */
    public static final String USE_USER_EMAIL = "USE_USER_EMAIL";
}
