package com.jflove.vo.notebook;

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
@ApiModel("备忘录查询条件")
public class GetListParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 2388172038897141624L;

    @ApiModelProperty(value="搜索关键字")
    private String keyword;

    @ApiModelProperty(value="标签")
    @NotNull(message = "标签不能为空")
    private Long tag;

    @ApiModelProperty(value="类型(笔记=0,待办=1)")
    @NotNull(message = "备忘录类型不能为空")
    private Long type;
}
