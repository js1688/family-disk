package com.jflove.webdav.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/9/13 11:35 AM
 * @desc:
 */
@Getter
@Setter
public class FileVO extends BaseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3767399183091667164L;

    /**
     * 磁盘路径
     */
    private String diskPath;

    public FileVO(String name, String id, String contentType, long contentLength, String diskPath) {
        super(name, id, contentType, contentLength);
        this.diskPath = diskPath;
    }
}
