package com.jflove.admin.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 10:46
 * @describe 磁盘类型
 */
@Getter
public enum FileDiskTypeENUM {
    LOCAL(0,"本地磁盘"),
    HDFS(1,"HDFS"),
    NAS(2,"NAS");
    private long code;
    private String name;

    FileDiskTypeENUM(long code, String name) {
        this.code = code;
        this.name = name;
    }
}
