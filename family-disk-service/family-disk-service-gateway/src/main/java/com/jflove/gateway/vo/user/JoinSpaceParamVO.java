package com.jflove.gateway.vo.user;

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
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("申请加入空间参数")
public class JoinSpaceParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 8391960066262640634L;

    @ApiModelProperty(value="目标空间编码")
    @NotBlank(message = "目标空间编码不能为空")
    private String targetSpaceCode;
}
