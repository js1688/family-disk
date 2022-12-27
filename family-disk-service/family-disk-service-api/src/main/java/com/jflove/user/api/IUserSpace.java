package com.jflove.user.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.user.dto.UserSpaceDTO;

/**
 * @author tanjun
 * @date 2022/12/9 16:42
 * @describe 用户空间
 */
public interface IUserSpace {

    /**
     * 创建空间
     * @param createUserId
     * @param title
     * @return
     */
    ResponseHeadDTO<UserSpaceDTO> createSpace(Long createUserId,String title);

    /**
     * 获取空间信息
     * @param spaceId
     * @return
     */
    ResponseHeadDTO<UserSpaceDTO> getSpaceInfo(Long spaceId);
}
