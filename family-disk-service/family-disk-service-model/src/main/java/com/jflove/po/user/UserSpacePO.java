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
 * @date Wed Dec 14 11:01:50 CST 2022
 * @describe 用户空间
 */
@Getter
@Setter
@ToString
@TableName("user_space")
public class UserSpacePO implements Serializable{
	@Serial
	private static final long serialVersionUID =  1126301540904665401L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
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
  @TableField(fill = FieldFill.INSERT)
  private java.sql.Timestamp createTime;
  /**
   * 修改时间
   */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private java.sql.Timestamp updateTime;
}
