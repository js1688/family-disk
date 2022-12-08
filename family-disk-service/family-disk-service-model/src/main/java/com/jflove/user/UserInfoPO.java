package com.jflove.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;


/**
 * @author tanjun
 * @date Wed Dec 07 15:53:38 CST 2022
 * @describe 用户信息
 */
@Getter
@Setter
@ToString
@TableName("user_info")
public class UserInfoPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  3095257218662389664L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 用户邮箱
   */
  private String email;
  /**
   * 用户名称
   */
  private String name;
  /**
   * 密码
   */
  private String password;
  /**
   * 创建时间
   */
  private java.sql.Timestamp createTime;
  /**
   * 修改时间
   */
  private java.sql.Timestamp updateTime;
}
