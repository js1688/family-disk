package com.jflove.share.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.share.dto.ShareLinkDTO;

/**
 * @author: tanjun
 * @date: 2023/2/9 5:48 PM
 * @desc: 笔记分享
 */
public interface INoteShare {

    /**
     * 创建笔记分享
     * @param password
     * @param bodyId
     * @param spaceId
     * @param invalidTime
     * @return
     */
    ResponseHeadDTO<String> create(String password, long bodyId, long spaceId, String invalidTime);


    /**
     * 获取分享内容
     * @param uuid
     * @param password
     * @return
     */
    ResponseHeadDTO<String> getBody(String uuid,String password);

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
