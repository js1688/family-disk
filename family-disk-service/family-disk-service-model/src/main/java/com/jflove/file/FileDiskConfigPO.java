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
 * @date Mon Dec 12 11:33:49 CST 2022
 * @describe 可存储文件的磁盘配置
 */
@Getter
@Setter
@ToString
@TableName("file_disk_config")
public class FileDiskConfigPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  3141618709424952290L;

  /**
   * null
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 磁盘类型(0=本地磁盘,1=HDFS,2=NAS)
   */
  private long type;
  /**
   * 磁盘总大小(GB)
   */
  private long maxSize;
  /**
   * 磁盘地址
   */
  private String path;
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
   * 磁盘可用(GB)
   */
  private long usableSize;
}
