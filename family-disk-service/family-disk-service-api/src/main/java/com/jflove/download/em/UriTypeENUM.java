package com.jflove.download.em;

import lombok.Getter;

/**
 * @author tanjun
 * @date 2022/12/12 16:00
 * @describe 下载连接类型
 */
@Getter
public enum UriTypeENUM {

    aria2cSimple("aria2cSimple","普通的下载,url可以是(HTTP/FTP/SFTP/BitTorrent)");
    private String code;
    private String name;

    UriTypeENUM(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
