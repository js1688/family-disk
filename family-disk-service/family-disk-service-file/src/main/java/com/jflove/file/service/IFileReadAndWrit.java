package com.jflove.file.service;

import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.dto.FileTransmissionDTO;

/**
 * @author tanjun
 * @date 2022/12/14 18:33
 * @describe 文件读写标准类
 */
public interface IFileReadAndWrit {

    /**
     * 写盘
     * @param data 文件信息
     * @param selectd 选择的磁盘信息
     * @param tempFileSuffix 临时文件后缀
     * @param tempPath 临时文件存放目录
     * @return
     */
    boolean writ(FileTransmissionDTO data, FileDiskConfigPO selectd,String  tempFileSuffix,String tempPath);
}
