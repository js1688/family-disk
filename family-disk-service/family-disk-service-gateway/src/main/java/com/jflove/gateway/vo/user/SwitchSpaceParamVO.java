package com.jflove.gateway.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/14 11:11
 * @describe 切换空间参数
 */
@Getter
@Setter
@ToString
@ApiModel("切换空间参数")
public class SwitchSpaceParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1257235503425949291L;

    @ApiModelProperty(value="目标空间id")
    @NotNull(message = "目标空间id不能为空")
    private Long targetSpaceId;
}
