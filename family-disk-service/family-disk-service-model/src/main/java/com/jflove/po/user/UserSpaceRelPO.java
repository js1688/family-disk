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
 * @date Wed Feb 08 11:50:09 CST 2023
 * @describe 用户与空间关联关系
 */
@Getter
@Setter
@ToString
@TableName("user_space_rel")
public class UserSpaceRelPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  7502894231552320557L;

  /**
   * null
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 空间的创建用户id
   */
  private long createUserId;
  /**
   * 空间id
   */
  private long spaceId;
  /**
   * 用户id
   */
  private long userId;
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
   * 空间权限
   */
  private String role;
  /**
   * 关系状态
   */
  private String state;
  /**
   * 空间主题
   */
  private String title;
}
