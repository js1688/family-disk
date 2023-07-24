package com.jflove.gateway.vo.netdisk;

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
@ApiModel("移动目录参数")
public class MoveDirectoryParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6644419400728156796L;

    @ApiModelProperty(value="目录ID")
    @NotNull(message = "目录ID不能为空")
    private Long id;

    @ApiModelProperty(value="目标目录ID")
    @NotNull(message = "目标目录ID不能为空")
    private Long targetDirId;
}
