package com.jflove.user.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserSpaceRoleENUM;

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

    /**
     * 使用用户空间
     * @param spaceId
     * @param useMb
     * @param increase true=使用空间,false=减少空间
     * @param isUse 是否同时修改存储空间
     * @return
     */
    ResponseHeadDTO useSpaceByte(Long spaceId,long useMb,boolean increase,boolean isUse);

    /**
     * 加入空间
     * @param targetSpaceCode 目标空间编码
     * @param userId 用户id
     * @return
     */
    ResponseHeadDTO joinSpace(String targetSpaceCode,long userId);

    /**
     * 切换正在使用的空间空间
     * @param targetSpaceId 目标空间id
     * @param originalSpaceId 原空间id
     * @param userId 用户id
     * @return
     */
    ResponseHeadDTO switchSpace(long targetSpaceId,long originalSpaceId,long userId);


    /**
     * 获取用户空间关联的所有用户,但不包含空间创建者本身
     * @param createUserId 空间的创建用户
     * @return
     */
    ResponseHeadDTO<UserSpaceRelDTO> getUserInfoBySpaceId(long createUserId);

    /**
     * 将用户从我的空间中移除
     * @param useUserId
     * @param removeUserId
     * @return
     */
    ResponseHeadDTO removeRel(long useUserId,long removeUserId);

    /**
     * 用户退出空间
     * @param spaceId
     * @param userId
     * @return
     */
    ResponseHeadDTO exitRel(long spaceId,long userId);

    /**
     * 通过邮箱邀请用户加入到我的空间
     * @param email
     * @param userId
     * @return
     */
    ResponseHeadDTO inviteSpace(String email,long userId);

    /**
     * 设置用户与空间的权限
     * @param useUserId
     * @param targetUserId
     * @param role
     * @return
     */
    ResponseHeadDTO setRelRole(long useUserId, long targetUserId, UserSpaceRoleENUM role);
}
