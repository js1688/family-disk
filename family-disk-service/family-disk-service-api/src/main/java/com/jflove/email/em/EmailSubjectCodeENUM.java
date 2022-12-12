package com.jflove.email.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/9 11:25
 * @describe 邮件主题类型
 */
@Getter
public enum EmailSubjectCodeENUM {
    CAPTCHA("CAPTCHA","家庭网盘注册验证码");
    private String code;
    private String name;

    EmailSubjectCodeENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
