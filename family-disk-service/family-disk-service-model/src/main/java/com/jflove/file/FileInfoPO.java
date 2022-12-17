package com.jflove.file;
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
 * @date Fri Dec 16 16:47:04 CST 2022
 * @describe 文件信息
 */
@Getter
@Setter
@ToString
@TableName("file_info")
public class FileInfoPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  1512940729162111348L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 所属磁盘id
   */
  private long diskId;
  /**
   * 文件名称
   */
  private String name;
  /**
   * 文件类型
   */
  private String type;
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
   * 创建用户id
   */
  private long createUserId;
  /**
   * 所属空间id
   */
  private long spaceId;
  /**
   * 文件大小(B)
   */
  private long size;
  /**
   * 文件来源(记事本,云盘)
   */
  private String source;
  /**
   * 文件MD5值
   */
  private String fileMd5;
  /**
   * 标记删除(1是,0否)
   */
  private long markDelete;
  /**
   * 执行删除时间
   */
  private long deleteTime;
}