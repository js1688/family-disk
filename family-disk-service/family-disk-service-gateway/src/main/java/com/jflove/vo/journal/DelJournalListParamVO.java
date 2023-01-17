package com.jflove.vo.journal;

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
 * @date: 2023/1/17 10:15 AM
 * @desc:
 */
@Getter
@Setter
@ToString
@ApiModel("删除日志条件")
public class DelJournalListParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6350325083435036734L;

    @ApiModelProperty(value="日记id")
    @NotNull(message = "日记id不能为空")
    private Long id;
}
