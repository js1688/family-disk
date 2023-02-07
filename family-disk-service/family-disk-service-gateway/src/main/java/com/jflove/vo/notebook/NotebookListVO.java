package com.jflove.vo.notebook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;


/**
 * @author tanjun
 * @date Mon Jan 16 17:22:17 CST 2023
 * @describe 备忘录列表
 */
@Getter
@Setter
@ToString
@ApiModel("备忘录列表")
public class NotebookListVO implements Serializable{


  @ApiModelProperty(value="主键")
  private long id;

  @ApiModelProperty(value="创建日期")
  private Date createTime;

  @ApiModelProperty(value="修改日期")
  private Date updateTime;

  @ApiModelProperty(value="标签")
  private long tag;

  @ApiModelProperty(value="类型(笔记=0,待办=1)")
  private long type;

  @ApiModelProperty(value="关键字")
  private String keyword;
}
