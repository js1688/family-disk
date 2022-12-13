package com.jflove.user;
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
 * @date Tue Dec 13 17:25:20 CST 2022
 * @describe 用户与空间关联关系
 */
@Getter
@Setter
@ToString
@TableName("user_space_rel")
public class UserSpaceRelPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  8750729179754120070L;

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
   * 空间权限(0只读,1读写)
   */
  private long role;
}
