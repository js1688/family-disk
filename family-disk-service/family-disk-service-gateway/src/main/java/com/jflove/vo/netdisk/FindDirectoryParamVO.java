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
@ApiModel("查找目录参数")
public class FindDirectoryParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 2408183911588142292L;

    @ApiModelProperty(value="父目录ID")
    private Long pid;

    @ApiModelProperty(value="查找关键字")
    private String keyword;
}
