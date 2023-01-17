package com.jflove.vo.journal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * @author tanjun
 * @date Mon Jan 16 17:22:17 CST 2023
 * @describe 日记列表
 */
@Getter
@Setter
@ToString
@ApiModel("日记列表")
public class JournalListVO implements Serializable{

  @Serial
  private static final long serialVersionUID = 3697987553363101153L;

  @ApiModelProperty(value="主键")
  private long id;

  @ApiModelProperty(value="日记标题")
  private String title;

  @ApiModelProperty(value="记录内容")
  private String body;

  @ApiModelProperty(value="日记发生日期(yyyy-MM-dd)")
  @NotBlank(message = "日记发生日记不能为空")
  private String happenTime;

  @ApiModelProperty(value="创建日期")
  private Date createTime;

  @ApiModelProperty(value="修改日期")
  private Date updateTime;

  @ApiModelProperty(value="创建人ID")
  private long createUserId;


  @ApiModelProperty(value="所属空间id")
  private long spaceId;

  @ApiModelProperty(value="关联的文件")
  @Valid
  private List<JournalListFilesVO> files;
}
