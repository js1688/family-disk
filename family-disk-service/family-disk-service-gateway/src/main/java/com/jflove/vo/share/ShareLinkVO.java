package com.jflove.vo.share;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @author tanjun
 * @date Thu Feb 09 17:43:13 CST 2023
 * @describe 分享链接
 */
@Getter
@Setter
@ToString
@ApiModel("分享链接信息")
public class ShareLinkVO implements Serializable{

  @Serial
  private static final long serialVersionUID = -8628619786272679643L;
  @ApiModelProperty(value="主键")
  private long id;
  @ApiModelProperty(value="创建时间")
  private Date createTime;
  @ApiModelProperty(value="修改时间")
  private Date updateTime;
  @ApiModelProperty(value="链接uuid")
  private String uuid;
  @ApiModelProperty(value="链接解锁密码")
  private String password;
  @ApiModelProperty(value="链接内容类型")
  private String bodyType;
  @ApiModelProperty(value="失效时间")
  private Date invalidTime;
  @ApiModelProperty(value="链接指向内容id")
  private long bodyId;
  @ApiModelProperty(value="标题关键字")
  private String keyword;
}
