package com.jflove.journal;
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
 * @date Mon Jan 16 17:22:17 CST 2023
 * @describe 日记列表
 */
@Getter
@Setter
@ToString
@TableName("journal_list")
public class JournalListPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  6639830854097925L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 日记标题
   */
  private String title;
  /**
   * 记录内容
   */
  private String body;
  /**
   * 日记发生日期(yyyy-MM-dd)
   */
  private String happenTime;
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
   * 创建人ID
   */
  private long createUserId;
  /**
   * 所属空间id
   */
  private long spaceId;
}
