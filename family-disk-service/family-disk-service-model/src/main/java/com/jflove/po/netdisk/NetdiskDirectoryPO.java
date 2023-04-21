package com.jflove.po.netdisk;
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
 * @date Thu Jan 05 14:03:01 CST 2023
 * @describe 网盘文件目录
 */
@Getter
@Setter
@ToString
@TableName("netdisk_directory")
public class NetdiskDirectoryPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  5991701900534856184L;

  /**
   * null
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * 类型
   */
  private String type;
  /**
   * 文件md5值
   */
  private String fileMd5;
  /**
   * 目录的上级id
   */
  private long pid;
  /**
   * 所属空间id
   */
  private long spaceId;
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
   * 目录名称
   */
  private String name;
  /**
   * 文件多媒体类型
   */
  private String mediaType;
}
