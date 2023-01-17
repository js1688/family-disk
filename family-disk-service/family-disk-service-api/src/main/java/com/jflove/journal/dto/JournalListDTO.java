package com.jflove.journal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * @author tanjun
 * @date Mon Jan 16 17:22:17 CST 2023
 * @describe 日记列表
 */
@Getter
@Setter
@ToString
public class JournalListDTO implements Serializable{

  @Serial
  private static final long serialVersionUID = -6507858368403409827L;
  /**
   * 主键
   */
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
  private Date createTime;
  /**
   * 修改日期
   */
  private Date updateTime;
  /**
   * 创建人ID
   */
  private long createUserId;
  /**
   * 所属空间id
   */
  private long spaceId;

  /**
   * 关联的文件
   */
  private List<JournalListFilesDTO> files;
}
