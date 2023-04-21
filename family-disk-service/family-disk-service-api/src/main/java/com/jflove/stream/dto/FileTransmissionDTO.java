package com.jflove.stream.dto;

import com.jflove.stream.em.FileSourceENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/12 16:03
 * @describe 文件流传输dto
 */
@Getter
@Setter
@ToString
public class FileTransmissionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5422773639194361219L;

    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件总大小(B)
     */
    private long totalSize;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 本次文件分片个数
     */
    private int shardingNum;

    /**
     * 本次传输分片文件流
     */
    private byte [] shardingStream;

    /**
     * 第几片分片
     */
    private int shardingSort;

    /**
     * 文件来源
     */
    private FileSourceENUM source;

    /**
     * 创建用户id
     */
    private Long createUserId;

    /**
     * 所属空间id
     */
    private Long spaceId;

    /**
     * 文件多媒体类型
     */
    private String mediaType;

    /**
     * 范围开始位置
     */
    private long rangeStart;

    /**
     * 范围结束位置
     */
    private long rangeEnd;

    /**
     * 本次读取长度
     */
    private long readLength;
}
