package com.jflove.vo.netdisk;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@ApiModel("修改目录名称参数")
public class UpdateDirectoryNameParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 5450640916782203172L;
    @ApiModelProperty(value="目录ID")
    private Long id;

    @ApiModelProperty(value="目录新名称")
    private String name;
}
