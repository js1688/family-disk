package com.jflove.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tanjun
 * @date Wed Dec 07 12:03:19 CST 2022
 * @describe 用户信息
 */
@Getter
@Setter
@ToString
public class UserInfoDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 999401249279970447L;
    /**
     * 主键
     */
    private long id;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户名称
     */
    private String name;
    /**
     * 密码
     */
    private String password;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;
}
