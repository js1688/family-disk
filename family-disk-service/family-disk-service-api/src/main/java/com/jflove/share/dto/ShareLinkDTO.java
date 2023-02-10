package com.jflove.share.dto;

import com.jflove.share.em.ShareBodyTypeENUM;
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
public class ShareLinkDTO implements Serializable{

  @Serial
  private static final long serialVersionUID = -3397285670287323880L;
  /**
   * null
   */
  private long id;
  /**
   * 创建时间
   */
  private Date createTime;
  /**
   * 修改时间
   */
  private Date updateTime;
  /**
   * 空间id
   */
  private long spaceId;
  /**
   * 链接uuid
   */
  private String uuid;
  /**
   * 链接密码,空则不需要密码
   */
  private String password;
  /**
   * 内容类型
   */
  private ShareBodyTypeENUM bodyType;
  /**
   * 失效时间戳10位
   */
  private long invalidTime;
  /**
   * 链接指向内容id
   */
  private long bodyId;

  /**
   * 标题关键字
   */
  private String keyword;
}
