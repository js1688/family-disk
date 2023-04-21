package com.jflove.file.dto;

import com.jflove.file.em.FileSourceENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;


/**
 * @author tanjun
 * @date Thu Jan 05 14:03:09 CST 2023
 * @describe 文件信息
 */
@Getter
@Setter
@ToString
public class FileInfoDTO implements Serializable{

  @Serial
  private static final long serialVersionUID = 8264674703322851848L;

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
  private FileSourceENUM source;
  /**
   * 文件MD5值
   */
  private String fileMd5;
  /**
   * 文件多媒体类型
   */
  private String mediaType;
}
