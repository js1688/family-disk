package com.jflove.netdisk.dto;

import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @author tanjun
 * @date Fri Dec 16 11:57:03 CST 2022
 * @describe 网盘文件目录
 */
@Getter
@Setter
@ToString
public class NetdiskDirectoryDTO implements Serializable{


  @Serial
  private static final long serialVersionUID = -4741481982856126386L;

  private long id;
  /**
   * 类型
   */
  private NetdiskDirectoryENUM type;
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
  private Date createTime;
  /**
   * 修改时间
   */
  private Date updateTime;
  /**
   * 目录名称
   */
  private String name;

  /**
   * 文件多媒体类型
   */
  private String mediaType;
}
