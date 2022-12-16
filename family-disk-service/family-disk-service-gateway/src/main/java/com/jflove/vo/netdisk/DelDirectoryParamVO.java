package com.jflove.vo.netdisk;

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
 * @date 2022/12/16 17:29
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("删除目录参数")
public class DelDirectoryParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 245318822192845836L;
    @ApiModelProperty(value="目录ID")
    @NotNull(message = "目录ID不能为空")
    private Long id;
}
