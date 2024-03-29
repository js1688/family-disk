package com.jflove.gateway.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/14 11:13
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("用户与空间关系")
public class UserSpaceRelVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 163374038874200806L;
    @ApiModelProperty(value="空间的创建用户id")
    private long createUserId;

    @ApiModelProperty(value="空间id")
    private long spaceId;

    @ApiModelProperty(value="用户id")
    private long userId;

    @ApiModelProperty(value="空间权限编码")
    private String role;

    @ApiModelProperty(value="关系状态(USE=正在使用,APPROVAL=待审批,NOTUSED=未使用)")
    private String state;

    @ApiModelProperty(value="空间主题")
    private String title;

    @ApiModelProperty(value="用户名称")
    private String userName;
}
