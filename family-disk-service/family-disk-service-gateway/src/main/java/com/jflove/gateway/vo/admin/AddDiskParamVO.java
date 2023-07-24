package com.jflove.gateway.vo.admin;

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
 * @date 2022/12/12 11:41
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("添加一个存储磁盘位置参数")
public class AddDiskParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6942235249640330786L;
    @ApiModelProperty(value="磁盘类型(LOCAL,HDFS,NAS)")
    @NotBlank(message = "磁盘类型不能为空")
    private String type;
    @ApiModelProperty(value="地址")
    @NotBlank(message = "地址不能为空")
    private String path;
}
