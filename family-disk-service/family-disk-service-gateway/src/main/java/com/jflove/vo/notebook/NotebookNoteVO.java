package com.jflove.vo.notebook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @author tanjun
 * @date Mon Jan 16 17:22:17 CST 2023
 * @describe 笔记列表
 */
@Getter
@Setter
@ToString
@ApiModel("笔记")
public class NotebookNoteVO implements Serializable{


  @Serial
  private static final long serialVersionUID = -5661182288545238002L;

  @ApiModelProperty(value="主键")
  private long id;

  @ApiModelProperty(value="创建日期")
  private Date createTime;

  @ApiModelProperty(value="修改日期")
  private Date updateTime;

  @ApiModelProperty(value="笔记内容")
  @NotBlank(message = "笔记内容不能为空")
  private String text;

  @ApiModelProperty(value="笔记html内容")
  private String html;

  @ApiModelProperty(value="标签")
  @NotNull(message = "标签不能为空")
  private Long tag;
}
