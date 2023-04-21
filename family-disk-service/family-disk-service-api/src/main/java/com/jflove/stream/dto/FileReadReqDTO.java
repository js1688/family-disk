package com.jflove.stream.dto;

import com.jflove.stream.em.FileSourceENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2023/1/3 15:51
 * @describe 文件读取参数
 */
@Setter
@Getter
@ToString
public class FileReadReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6835697913609855529L;

    public FileReadReqDTO() {
    }

    public FileReadReqDTO(String fileMd5, FileSourceENUM source, long spaceId) {
        this.fileMd5 = fileMd5;
        this.source = source;
        this.spaceId = spaceId;
    }

    /**
     * 文件md5
     */
    private String fileMd5;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件来源
     */
    private FileSourceENUM source;

    /**
     * 所属空间id
     */
    private long spaceId;

    /**
     * 文件名
     */
    private String name;

}
