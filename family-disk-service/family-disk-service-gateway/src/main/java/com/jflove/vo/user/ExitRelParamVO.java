package com.jflove.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/3/28 10:30 AM
 * @desc:
 */
@Getter
@Setter
@ToString
@ApiModel("用户退出空间参数")
public class ExitRelParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -6656369342893305121L;

    @ApiModelProperty(value="退出的空间ID")
    @NotNull(message = "退出的空间ID不能为空")
    private Long spaceId;
}
