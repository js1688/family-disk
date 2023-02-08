package com.jflove.user.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 16:00
 * @describe 账户与空间关系状态
 */
@Getter
public enum UserRelStateENUM {

    USE("USE","正在使用"),
    APPROVAL("APPROVAL","待审批"),
    NOTUSED("NOTUSED","未使用");
    private String code;
    private String name;

    UserRelStateENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
