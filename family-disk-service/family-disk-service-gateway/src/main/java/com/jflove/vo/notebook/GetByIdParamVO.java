package com.jflove.vo.notebook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/1/17 10:15 AM
 * @desc:
 */
@Getter
@Setter
@ToString
@ApiModel("备忘录按id查询条件")
public class GetByIdParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 7622838224315552244L;

    @ApiModelProperty(value="ID")
    private Long id;
}
