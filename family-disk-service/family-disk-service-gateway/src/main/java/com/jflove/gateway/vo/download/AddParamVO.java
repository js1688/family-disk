package com.jflove.gateway.vo.download;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/16 14:21
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("添加离线下载任务")
public class AddParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5579622014745474436L;
    @ApiModelProperty(value="uri地址类型")
    @NotBlank(message = "uri地址类型不能为空")
    private String uriType;

    @ApiModelProperty(value="地址")
    @NotBlank(message = "地址不能为空")
    private String uri;

    @ApiModelProperty(value = "存放目的地")
    @NotNull(message = "存放目的地不能为空")
    private Long targetId;

}
