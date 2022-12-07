package com.jflove.user;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;


/**
 * @author tanjun
 * @date Wed Dec 07 10:45:16 CST 2022
 * @describe 用户信息
 */
@Getter
@Setter
@TableName("user_info")
public class UserInfoPO implements Serializable{

	private static final long serialVersionUID =  900768261691275673L;

  /**
   * 主键
   */
  @TableId
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
