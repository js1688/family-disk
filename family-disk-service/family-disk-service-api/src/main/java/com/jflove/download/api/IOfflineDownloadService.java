package com.jflove.download.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.download.em.UriTypeENUM;

/**
 * @author: tanjun
 * @date: 2023/7/11 3:23 PM
 * @desc: 离线下载
 */
public interface IOfflineDownloadService {

    /**
     * 添加一条通用的下载任务
     * @param uriType uri 地址类型
     * @param uris (HTTP/FTP/SFTP/BitTorrent)
     * @param spaceId 任务关联到的空间id
     * @param targetId 文件下载成功后的网盘目录id
     * @return
     */
    ResponseHeadDTO add(UriTypeENUM uriType,String uris, Long spaceId, Long targetId);

    /**
     * 查询下载文件的列表
     * @param spaceId 任务关联到的空间id
     * @param fileName 文件名称
     * @return
     */
    ResponseHeadDTO getFiles(Long spaceId,String fileName);

}
