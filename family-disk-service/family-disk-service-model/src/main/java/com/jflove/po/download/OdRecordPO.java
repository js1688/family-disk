package com.jflove.po.download;
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
 * @date Wed Jul 19 11:53:36 CST 2023
 * @describe 离线下载记录
 */
@Getter
@Setter
@ToString
@TableName("od_record")
public class OdRecordPO implements Serializable{
	@Serial
	private static final long serialVersionUID =  6416213822689510235L;

  /**
   * 主键
   */
  @TableId(type = IdType.AUTO)
  private long id;
  /**
   * aria2文件下载记录唯一标识
   */
  private String gid;
  /**
   * 文件保存名称
   */
  private String fileName;
  /**
   * 网盘目录ID
   */
  private long targetId;
  /**
   * 所属空间id
   */
  private long spaceId;

  /**
   * uri类型
   */
  private String uriType;

  /**
   * 描述信息
   */
  private String msg;

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
