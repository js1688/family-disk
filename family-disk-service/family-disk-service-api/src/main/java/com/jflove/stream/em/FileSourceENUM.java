package com.jflove.stream.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 16:00
 * @describe 文件来源
 */
@Getter
public enum FileSourceENUM {

    NOTEPAD("NOTEPAD","记事本"),
    CLOUDDISK("CLOUDDISK","云盘"),
    JOURNAL("JOURNAL","日记");
    private String code;
    private String name;

    FileSourceENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
