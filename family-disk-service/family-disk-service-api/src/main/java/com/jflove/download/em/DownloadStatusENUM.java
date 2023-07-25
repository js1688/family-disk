package com.jflove.download.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 16:00
 * @describe 离线下载任务状态
 */
@Getter
public enum DownloadStatusENUM {

    active("active","正在下载"),
    waiting("waiting","未开始"),
    paused("paused","暂停"),
    error("error","下载失败"),
    complete("complete","完成"),
    removed("removed","已移除");
    private String code;
    private String name;

    DownloadStatusENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
