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
 * @date 2022/12/14 11:11
 * @describe 用户创建空间参数
 */
@Getter
@Setter
@ToString
@ApiModel("用户创建空间参数")
public class CreateSpaceParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3153847628747489080L;

    @ApiModelProperty(value="空间主题")
    @NotBlank(message = "空间主题不能为空")
    private String title;
}
