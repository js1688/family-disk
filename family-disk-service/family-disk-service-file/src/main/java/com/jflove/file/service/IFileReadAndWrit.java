package com.jflove.file.service;

import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.dto.FileTransmissionDTO;
import org.apache.dubbo.common.stream.StreamObserver;

/**
 * @author tanjun
 * @date 2022/12/14 18:33
 * @describe 文件读写标准类
 */
public interface IFileReadAndWrit {

    String BEAN_PREFIX = "FileReadAndWrit_";

    /**
     * 写入
     * @param data 文件信息
     * @param selectd 选择的磁盘信息
     * @param tempFileSuffix 临时文件后缀
     * @param tempPath 临时文件存放目录
     * @return 最终写完返回true
     */
    boolean writ(FileTransmissionDTO data, FileDiskConfigPO selectd,String  tempFileSuffix,String tempPath);

    /**
     * 自动分片读取整个文件
     * @param dto 文件信息
     * @param selectd 选择的磁盘信息
     * @param response 文件流分片传输对象
     */
    void read(FileTransmissionDTO dto, FileDiskConfigPO selectd, StreamObserver<FileTransmissionDTO> response);

    /**
     * 读取指定位置字节
     * @param dto
     * @param selectd
     * @param response
     */
    void readByte(FileTransmissionDTO dto, FileDiskConfigPO selectd, StreamObserver<FileTransmissionDTO> response);
}
