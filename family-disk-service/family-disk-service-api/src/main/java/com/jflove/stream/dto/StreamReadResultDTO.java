package com.jflove.stream.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/4/21 4:26 PM
 * @desc: 读取文件流结果
 */

@Setter
@Getter
@ToString
public class StreamReadResultDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -7491685249483466279L;

    /**
     * 读取时使用的参数
     */
    private StreamReadParamDTO param;

    /**
     * 本次读取长度
     */
    private long readLength;

    /**
     * 文件总大小(B)
     */
    private long totalSize;

    /**
     * 本次读取到的二进制流
     */
    private byte [] stream;

}
