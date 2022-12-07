package com.jflove.user;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;


/**
 * @author tanjun
 * @date Wed Dec 07 10:45:16 CST 2022
 * @describe 用户与空间关联关系
 */
@Getter
@Setter
@TableName("user_space_rel")
public class UserSpaceRelPO implements Serializable{

	private static final long serialVersionUID =  3271141513638552805L;

  /**
   * null
   */
  @TableId
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
