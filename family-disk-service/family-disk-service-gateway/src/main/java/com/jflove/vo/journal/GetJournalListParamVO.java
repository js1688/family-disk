package com.jflove.vo.journal;

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
@ApiModel("查询日志条件")
public class GetJournalListParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -515619245972167430L;


    @ApiModelProperty(value="请输入日记标题或日期(格式:2020年01月02日)")
    private String keyword;
}
