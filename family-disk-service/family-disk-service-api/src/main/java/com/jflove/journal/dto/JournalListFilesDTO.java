package com.jflove.journal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @author tanjun
 * @date Mon Jan 16 17:22:17 CST 2023
 * @describe 日记关联的文件
 */
@Getter
@Setter
@ToString
public class JournalListFilesDTO implements Serializable{


  @Serial
  private static final long serialVersionUID = -8314704143239082833L;
  /**
   * 主键
   */
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
  private Date createTime;
  /**
   * 修改日期
   */
  private Date updateTime;

  /**
   * 多媒体类型
   */
  private String mediaType;
}
