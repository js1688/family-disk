package com.jflove.user.dto;

import com.jflove.user.em.UserSpaceRoleENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/13 17:26
 * @describe
 */
@Getter
@Setter
@ToString
public class UserSpaceRelDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3731937495255507273L;

    /**
     * 空间的创建用户id
     */
    private long createUserId;
    /**
     * 空间id
     */
    private long spaceId;
    /**
     * 用户id
     */
    private long userId;
    /**
     * 空间权限
     */
    private UserSpaceRoleENUM role;
}
