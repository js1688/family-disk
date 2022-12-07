package com.jflove.user;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;


/**
 * @author tanjun
 * @date Wed Dec 07 10:45:16 CST 2022
 * @describe 用户空间
 */
@Getter
@Setter
@TableName("user_space")
public class UserSpacePO implements Serializable{

	private static final long serialVersionUID =  1869982543297359484L;

  /**
   * 主键
   */
  @TableId
  private long id;
  /**
   * 最大空间大小(GB)
   */
  private long maxSize;
  /**
   * 空间编码
   */
  private String code;
  /**
   * 已使用空间大小(GB)
   */
  private long useSize;
  /**
   * 空间主题
   */
  private long title;
  /**
   * 空间创建用户id
   */
  private long createUserId;
  /**
   * 创建时间
   */
  private java.sql.Timestamp createTime;
  /**
   * 修改时间
   */
  private java.sql.Timestamp updateTime;
}
