package com.jflove.po.email;
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
 * @date Fri Dec 09 15:48:00 CST 2022
 * @describe 邮件发送记录
 */
@Getter
@Setter
@ToString
@TableName("email_send_record")
public class EmailSendRecordPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  6611982305323124693L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 邮件主题
   */
  private String subject;
  /**
   * 目的地邮箱号
   */
  private String recipient;
  /**
   * 邮件内容
   */
  private String msgBody;
  /**
   * 下一次允许发送的时间戳
   */
  private long nextSendTime;
  /**
   * 主题编码
   */
  private String subjectCode;
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
