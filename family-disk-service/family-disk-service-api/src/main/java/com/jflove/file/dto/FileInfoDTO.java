package com.jflove.file.dto;

import com.jflove.file.em.FileSourceENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tanjun
 * @date 2022/12/12 16:03
 * @describe
 */
@Getter
@Setter
@ToString
public class FileInfoDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5422773639194361219L;
    /**
     * 主键
     */
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
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 创建用户id
     */
    private long createUserId;
    /**
     * 所属空间id
     */
    private long spaceId;
    /**
     * 文件md5值
     */
    private String md5;
    /**
     * 文件大小(B)
     */
    private long size;
    /**
     * 文件编码
     */
    private String code;
    /**
     * 文件来源(0=记事本,1=云盘)
     */
    private FileSourceENUM source;
}
