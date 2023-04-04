package com.jflove.file.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.file.em.FileSourceENUM;

/**
 * @author: tanjun
 * @date: 2023/1/10 4:47 PM
 * @desc: 文件管理
 */
public interface IFileAdministration {
    /**
     * 标记删除文件
     * @param md5
     * @param spaceId
     * @param source
     * @return
     */
    ResponseHeadDTO<Boolean> delFile(String md5, long spaceId, FileSourceENUM source);

    /**
     * 这个用户空间是否已经拥有了这个文件
     * @param md5
     * @param spaceId
     * @param source
     * @return
     */
    ResponseHeadDTO<Boolean> isExist(String md5,long spaceId,FileSourceENUM source);

    /**
     * 修改文件名称
     * @param md5
     * @param spaceId
     * @param name
     * @param source
     * @return
     */
    ResponseHeadDTO updateName(String md5,long spaceId,String name,FileSourceENUM source);

    /**
     * 从垃圾箱恢复文件
     * @param fileMd5
     * @param spaceId
     * @param source
     * @return
     */
    ResponseHeadDTO dustbinRecovery(String fileMd5,long spaceId,FileSourceENUM source);

    /**
     * 获取文件大小
     * @param fileMd5
     * @return
     */
    ResponseHeadDTO getFileSize(String fileMd5);

    /**
     * 检查文件是否重复,如果重复则直接建立引用
     * @param fileName
     * @param type
     * @param mediaType
     * @param fileMd5
     * @param spaceId
     * @param source
     * @param totalSize
     * @param createUserId
     * @return
     */
    ResponseHeadDTO checkDuplicate(String fileName,String type,String mediaType,String fileMd5,long spaceId,FileSourceENUM source,long totalSize,long createUserId);
}
