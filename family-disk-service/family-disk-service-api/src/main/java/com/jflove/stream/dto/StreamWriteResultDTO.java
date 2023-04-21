package com.jflove.stream.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/4/21 4:26 PM
 * @desc: 文件流写入返回
 */

@Setter
@Getter
@ToString
public class StreamWriteResultDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -4024412426807881910L;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 所属磁盘id
     */
    private long diskId;

}
