package com.jflove.user.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 16:00
 * @describe 用户对空间的权限
 */
@Getter
public enum UserSpaceRoleENUM {

    READ("READ","只读"),
    WRITE("WRITE","读写");
    private String code;
    private String name;

    UserSpaceRoleENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
