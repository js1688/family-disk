package com.jflove.user.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 16:00
 * @describe 账户权限
 */
@Getter
public enum UserRoleENUM {

    ADMIN("ADMIN","管理员"),
    COMMON("COMMON","普通用户");
    private String code;
    private String name;

    UserRoleENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
