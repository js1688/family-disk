package com.jflove.stream.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/4/21 4:26 PM
 * @desc: 文件流写入响应
 */

@Setter
@Getter
@ToString
public class StreamWriteResultDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -1295899713998383229L;

    public StreamWriteResultDTO() {
    }

    public StreamWriteResultDTO(String fileMd5, String mediaType) {
        this.fileMd5 = fileMd5;
        this.mediaType = mediaType;
    }

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 媒体类型
     */
    private String mediaType;


}
