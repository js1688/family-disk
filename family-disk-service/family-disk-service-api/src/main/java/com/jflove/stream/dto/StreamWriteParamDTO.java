package com.jflove.stream.dto;

import com.jflove.file.em.FileSourceENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/4/21 4:26 PM
 * @desc: 文件流写入参数
 */

@Setter
@Getter
@ToString
public class StreamWriteParamDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 4863114432701129600L;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 第几片分片
     */
    private int shardingSort;

    /**
     * 本次文件分片个数
     */
    private int shardingNum;

    /**
     * 文件总大小(B)
     */
    private long totalSize;

    /**
     * 二进制流
     */
    private byte [] stream;

    /**
     * 文件来源
     */
    private FileSourceENUM source;

    /**
     * 所属空间id
     */
    private Long spaceId;

    /**
     * 文件原始名称
     */
    private String originalFileName;

    /**
     * 媒体类型
     */
    private String mediaType;

    /**
     * 创建用户
     */
    private long createUserId;

    /**
     * 自盘时开始的位置
     */
    private long seek;

}
