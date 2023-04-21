package com.jflove.po.share;
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
 * @date Thu Feb 09 17:43:13 CST 2023
 * @describe 分享链接
 */
@Getter
@Setter
@ToString
@TableName("share_link")
public class ShareLinkPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  962477724551942893L;

  /**
   * null
   */
  @TableId(type = IdType.AUTO)
  private long id;
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
   * 空间id
   */
  private long spaceId;
  /**
   * 链接uuid
   */
  private String uuid;
  /**
   * 链接密码,空则不需要密码
   */
  private String password;
  /**
   * 内容类型
   */
  private String bodyType;
  /**
   * 失效时间戳10位
   */
  private long invalidTime;
  /**
   * 链接指向内容id
   */
  private long bodyId;
}
