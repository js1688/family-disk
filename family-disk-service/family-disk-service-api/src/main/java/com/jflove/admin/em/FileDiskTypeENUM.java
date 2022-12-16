package com.jflove.admin.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 10:46
 * @describe 磁盘类型
 */
@Getter
public enum FileDiskTypeENUM {
    LOCAL("LOCAL","本地磁盘"),
    HDFS("HDFS","HDFS"),
    NAS("NAS","NAS");
    private String code;
    private String name;

    FileDiskTypeENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
