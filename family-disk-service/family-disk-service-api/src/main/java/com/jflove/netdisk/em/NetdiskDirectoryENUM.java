package com.jflove.netdisk.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/16 15:02
 * @describe
 */
@Getter
public enum NetdiskDirectoryENUM {
    FOLDER("FOLDER","文件夹"),
    FILE("FILE","文件");
    private String code;
    private String name;

    NetdiskDirectoryENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
