package com.jflove.share.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.share.dto.ShareLinkDTO;

/**
 * @author: tanjun
 * @date: 2023/2/9 5:48 PM
 * @desc: 分享管理
 */
public interface IShareAdmin {

    /**
     * 创建分享
     * @param password
     * @param bodyId
     * @param spaceId
     * @param invalidTime
     * @return
     */
    ResponseHeadDTO<ShareLinkDTO> create(String password, long bodyId, long spaceId, String invalidTime);

    /**
     * 删除分享链接
     * @param id
     * @param spaceId
     */
    ResponseHeadDTO delLink(long id,long spaceId);

    /**
     * 获取分享链接列表
     * @param spaceId
     * @return
     */
    ResponseHeadDTO<ShareLinkDTO> getLinkList(long spaceId);
}
