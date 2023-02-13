package com.jflove.share.dto;

import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/2/13 5:39 PM
 * @desc: 网盘分享目录信息
 */
@Getter
@Setter
@ToString
public class DirectoryInfoDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2118762824428943713L;

    private long id;
    /**
     * 类型
     */
    private NetdiskDirectoryENUM type;
    /**
     * 文件md5值
     */
    private String fileMd5;
    /**
     * 目录的上级id
     */
    private long pid;
    /**
     * 所属空间id
     */
    private long spaceId;
    /**
     * 目录名称
     */
    private String name;

    /**
     * 文件多媒体类型
     */
    private String mediaType;

    /**
     * 子目录
     */
    private List<DirectoryInfoDTO> child;
}
