package com.jflove.share.em;

import lombok.Getter;

/**
 * @author: tanjun
 * @date: 2023/2/9 5:44 PM
 * @desc: 分享内容类型
 */
@Getter
public enum ShareBodyTypeENUM {
    NOTE("NOTE","笔记"),
    NETDISK("NETDISK","网盘");
    private String code;
    private String name;

    ShareBodyTypeENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
