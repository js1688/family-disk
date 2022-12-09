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
 * @date Fri Dec 09 15:47:51 CST 2022
 * @describe 用户验证码存储
 */
@Getter
@Setter
@ToString
@TableName("user_captcha")
public class UserCaptchaPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  8748045535114819029L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 邮箱号
   */
  private String email;
  /**
   * 验证码
   */
  private String captcha;
  /**
   * 验证码到期时间戳(10位)
   */
  private long captchaExpireTime;
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
