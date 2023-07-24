package com.jflove.gateway.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/2/8 5:09 PM
 * @desc:
 */
@Getter
@Setter
@ToString
@ApiModel("设置用于与空间权限参数")
public class SetRelRoleParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8465875803176211042L;

    @ApiModelProperty(value="设置用户id")
    @NotNull(message = "设置用户id不能为空")
    private Long targetUserId;

    @ApiModelProperty(value="空间权限编码")
    @NotBlank(message = "空间权限编码不能为空")
    private String role;
}
