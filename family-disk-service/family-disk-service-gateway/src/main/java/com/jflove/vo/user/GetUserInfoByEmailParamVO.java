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
 * @date 2022/12/8 17:16
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("邮箱获取账号信息")
public class GetUserInfoByEmailParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8155108394676406494L;
    @ApiModelProperty(value="邮箱")
    @NotBlank(message = "邮箱不能为空")
    private String email;
}
