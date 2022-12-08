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
 * @date 2022/12/8 15:42
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("邮箱+密码登录参数")
public class EmailPasswordLoginParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1141369560127585469L;

    @ApiModelProperty(value="邮箱")
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @ApiModelProperty(value="密码")
    @NotBlank(message = "密码不能为空")
    private String password;
}
