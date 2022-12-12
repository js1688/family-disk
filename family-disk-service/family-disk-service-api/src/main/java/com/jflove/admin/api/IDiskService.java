package com.jflove.admin.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.admin.em.FileDiskTypeENUM;

/**
 * @author tanjun
 * @date 2022/12/9 17:47
 * @describe 磁盘服务
 */
public interface IDiskService {


    /**
     * 添加一个存储磁盘位置
     * @param type
     * @param path
     * @return
     */
    ResponseHeadDTO<Long> addDisk(FileDiskTypeENUM type,String path);
}
