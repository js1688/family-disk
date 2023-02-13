package com.jflove.share.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.share.dto.NetdiskShareDTO;

/**
 * @author: tanjun
 * @date: 2023/2/9 5:48 PM
 * @desc: 获得网盘分享内容
 */
public interface INetdiskShare {

    /**
     * 获取分享内容
     * @param uuid
     * @param password
     * @return
     */
    ResponseHeadDTO<NetdiskShareDTO> getDirectory(String uuid, String password);
}
