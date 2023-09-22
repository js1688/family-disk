package com.jflove.webdav.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author: tanjun
 * @date: 2023/9/13 11:41 AM
 * @desc:
 */
@Getter
@Setter
public class BaseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7169536656988077736L;

    /**
     * 名称
     */
    private String name;

    /**
     * 数据ID
     */
    private long id;

    /**
     * 类型
     */
    private String contentType;

    /**
     * 文件长度(b)
     */
    private long contentLength;

    private Date createDate;

    private Date modifiedDate;

    public BaseVO(String name,long id,Date createDate,Date modifiedDate) {
        this.name = name;
        this.id = id;
        this.createDate = createDate;
        this.modifiedDate = modifiedDate;
    }

    public BaseVO(String name ,long id,Date createDate,Date modifiedDate,String contentType, long contentLength) {
        this.name = name;
        this.id = id;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.createDate = createDate;
        this.modifiedDate = modifiedDate;
    }
}
