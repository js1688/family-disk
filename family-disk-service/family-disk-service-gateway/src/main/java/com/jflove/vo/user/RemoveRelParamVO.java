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
 * @date: 2023/2/8 4:57 PM
 * @desc:
 */
@Getter
@Setter
@ToString
@ApiModel("移除用户与空间关系参数")
public class RemoveRelParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3271187432414975835L;

    @ApiModelProperty(value="移除的用户id")
    @NotNull(message = "移除的用户id不能为空")
    private Long removeUserId;
}
