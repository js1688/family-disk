package com.jflove.webdav.vo;

import io.milton.resource.Resource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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

    private List<Resource> children;

    public FolderVO(String name, String id) {
        super(name,id);
    }

    public FolderVO(String name, String id, List<Resource> children) {
        super(name, id);
        this.children = children;
    }

    public FolderVO(String name, String id, String contentType, long contentLength) {
        super(name, id, contentType, contentLength);
    }
}
