package com.jflove.file.dto;

import com.jflove.file.em.FileSourceENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/1/9 12:48 PM
 * @desc:
 */
@Getter
@Setter
@ToString
public class FileByteReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8699322019105799531L;

    /**
     * 范围开始位置
     */
    private long rangeStart;

    /**
     * 范围结束位置
     */
    private long rangeEnd;

    /**
     * 文件总长度
     */
    private long totalLength;

    /**
     * 本次读取长度
     */
    private long readLength;

    /**
     * 文件md5s
     */
    private String fileMd5;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件来源
     */
    private FileSourceENUM source;

    /**
     * 所属空间id
     */
    private Long spaceId;

    /**
     * 文件流
     */
    private byte [] data;

    /**
     * 文件多媒体类型
     */
    private String mediaType;

    /**
     * 创建用户id
     */
    private Long createUserId;

    /**
     * 文件名称
     */
    private String fileName;
}
