package com.jflove.gateway.vo.journal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @author tanjun
 * @date Mon Jan 16 17:22:17 CST 2023
 * @describe 日记关联的文件
 */
@Getter
@Setter
@ToString
@ApiModel("日记关联的文件列表")
public class JournalListFilesVO implements Serializable{

  @Serial
  private static final long serialVersionUID = 1032221734793790568L;


  @ApiModelProperty(value="主键")
  private long id;

  @ApiModelProperty(value="日记id")
  private long journalId;

  @ApiModelProperty(value="文件md5值")
  @NotBlank(message = "文件MD5值不能为空")
  private String fileMd5;

  @ApiModelProperty(value="文件名")
  @NotBlank(message = "文件名不能为空")
  private String fileName;

  @ApiModelProperty(value="创建日期")
  private Date createTime;

  @ApiModelProperty(value="修改日期")
  private Date updateTime;

  @ApiModelProperty(value="多媒体类型")
  @NotBlank(message = "多媒体类型不能为了")
  private String mediaType;

}
