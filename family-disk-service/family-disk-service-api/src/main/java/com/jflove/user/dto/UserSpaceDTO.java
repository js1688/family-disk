package com.jflove.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @author tanjun
 * @date Fri Dec 09 15:47:51 CST 2022
 * @describe 用户空间
 */
@Getter
@Setter
@ToString
public class UserSpaceDTO implements Serializable{

  @Serial
  private static final long serialVersionUID = -4390298175098952979L;
  /**
   * 主键
   */
  private long id;
  /**
   * 最大空间大小(MB)
   */
  private long maxSize;
  /**
   * 空间编码
   */
  private String code;
  /**
   * 已使用空间大小(MB)
   */
  private long useSize;
  /**
   * 空间主题
   */
  private String title;
  /**
   * 空间创建用户id
   */
  private long createUserId;
  /**
   * 创建时间
   */
  private Date createTime;
  /**
   * 修改时间
   */
  private Date updateTime;
}
