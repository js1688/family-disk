package com.jflove.webdav.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

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
     * 文件唯一标识
     */
    private String md5;

    public FileVO(String name, long id, Date createDate, Date modifiedDate, String contentType, long contentLength, String md5) {
        super(name, id,createDate,modifiedDate,contentType, contentLength);
        this.md5 = md5;
    }
}
