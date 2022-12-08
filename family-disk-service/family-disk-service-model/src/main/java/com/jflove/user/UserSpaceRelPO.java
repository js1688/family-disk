package com.jflove.user;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.io.Serial;


/**
 * @author tanjun
 * @date Wed Dec 07 15:53:38 CST 2022
 * @describe 用户与空间关联关系
 */
@Getter
@Setter
@ToString
@TableName("user_space_rel")
public class UserSpaceRelPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  6962775026110048702L;

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
  private java.sql.Timestamp createTime;
  /**
   * 修改时间
   */
  private java.sql.Timestamp updateTime;
}
