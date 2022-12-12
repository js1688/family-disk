package com.jflove.file.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 16:00
 * @describe 文件来源
 */
@Getter
public enum FileSourceENUM {

    NOTEPAD(0,"记事本"),
    CLOUDDISK(1,"云盘");
    private long code;
    private String name;

    FileSourceENUM(long code, String name) {
        this.code = code;
        this.name = name;
    }
}
