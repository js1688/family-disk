package com.jflove.po.notebook;
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
 * @date Tue Feb 07 10:36:16 CST 2023
 * @describe 备忘录-笔记
 */
@Getter
@Setter
@ToString
@TableName("notebook_note")
public class NotebookNotePO implements Serializable{
	@Serial
	private static final long serialVersionUID =  2899815778187405427L;

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
   * 所属空间id
   */
  private long spaceId;
  /**
   * markdown内容
   */
  private String text;
  /**
   * markdown生成的html预览
   */
  private String html;
  /**
   * 关键字,截取markdown前部分
   */
  private String keyword;
  /**
   * 标签
   */
  private long tag;
  /**
   * 创建用户id
   */
  private long createUserId;
}
