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
 * @desc: 读取文件流参数
 */

@Setter
@Getter
@ToString
public class StreamReadParamDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4794314152218432164L;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件媒体类型
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
     * 读取长度
     */
    private long readLength;

    /**
     * 文件来源
     */
    private FileSourceENUM source;

    /**
     * 所属空间id
     */
    private Long spaceId;
}
