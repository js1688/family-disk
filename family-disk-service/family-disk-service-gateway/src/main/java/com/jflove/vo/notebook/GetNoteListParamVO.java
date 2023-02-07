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
@ApiModel("查询笔记本条件")
public class GetNoteListParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 2388172038897141624L;

    @ApiModelProperty(value="搜索关键字")
    private String keyword;

    @ApiModelProperty(value="标签")
    private long tag;
}
