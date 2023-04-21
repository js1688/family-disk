package com.jflove.po.user;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.io.Serial;


/**
 * @author tanjun
 * @date Fri Dec 16 16:35:24 CST 2022
 * @describe 用户信息
 */
@Getter
@Setter
@ToString
@TableName("user_info")
public class UserInfoPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  604080445822010497L;

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
  @TableField(fill = FieldFill.INSERT)
  private java.sql.Timestamp createTime;
  /**
   * 修改时间
   */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private java.sql.Timestamp updateTime;
  /**
   * 账户角色
   */
  private String role;
}
