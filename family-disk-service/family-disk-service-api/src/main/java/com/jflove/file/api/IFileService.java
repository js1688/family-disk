package com.jflove.file.api;

import com.jflove.file.dto.FileTransmissionDTO;
import org.apache.dubbo.common.stream.StreamObserver;

/**
 * @author tanjun
 * @date 2022/12/12 15:17
 * @describe 文件管理,官方推荐IDL方式编写api接口标准,效率和性能更高,但是为了代码可读性选择StreamObserver方式
 */
public interface IFileService {

    /**
     * 双向流
     * 分片方式添加一个文件到磁盘
     * @param response 处理响应
     * @return
     */
    StreamObserver<FileTransmissionDTO> addFile(StreamObserver<Boolean> response);

}
