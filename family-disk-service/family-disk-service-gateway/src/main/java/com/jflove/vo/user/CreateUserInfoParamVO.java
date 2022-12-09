package com.jflove.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/9 16:24
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("注册账号参数")
public class CreateUserInfoParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2265529258646355355L;

    @ApiModelProperty(value="邮箱")
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @ApiModelProperty(value="密码")
    @NotBlank(message = "密码不能为空")
    private String password;
    @ApiModelProperty(value="用户名称")
    @NotBlank(message = "用户名不能为空")
    private String name;
    @ApiModelProperty(value="验证码")
    @NotBlank(message = "验证码不能为空")
    private String captcha;
}
