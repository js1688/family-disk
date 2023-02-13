package com.jflove.share.api;

import com.jflove.ResponseHeadDTO;

/**
 * @author: tanjun
 * @date: 2023/2/9 5:48 PM
 * @desc: 获得笔记的分享内容
 */
public interface INoteShare {

    /**
     * 获取分享内容
     * @param uuid
     * @param password
     * @return
     */
    ResponseHeadDTO<String> getBody(String uuid,String password);
}
