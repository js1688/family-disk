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
public class FolderVO extends BaseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3767399183091667164L;

    public FolderVO(String name, long id, Date createDate, Date modifiedDate) {
        super(name,id,createDate,modifiedDate);
    }

    public FolderVO(String name,long id,Date createDate, Date modifiedDate, String contentType, long contentLength) {
        super(name, id,createDate,modifiedDate,contentType, contentLength);
    }
}
