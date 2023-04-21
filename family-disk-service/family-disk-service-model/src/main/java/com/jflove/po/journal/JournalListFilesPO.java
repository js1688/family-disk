package com.jflove.po.journal;
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
 * @date Tue Jan 17 11:59:13 CST 2023
 * @describe 日记关联的文件
 */
@Getter
@Setter
@ToString
@TableName("journal_list_files")
public class JournalListFilesPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  3335255810773952094L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 日记id
   */
  private long journalId;
  /**
   * 文件md5值
   */
  private String fileMd5;
  /**
   * 文件名
   */
  private String fileName;
  /**
   * 创建日期
   */
  @TableField(fill = FieldFill.INSERT)
  private java.sql.Timestamp createTime;
  /**
   * 修改日期
   */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private java.sql.Timestamp updateTime;
  /**
   * 多媒体类型
   */
  private String mediaType;
}
