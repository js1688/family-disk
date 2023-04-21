package com.jflove.stream.api;

import com.jflove.stream.dto.FileReadReqDTO;
import com.jflove.stream.dto.FileTransmissionDTO;
import com.jflove.stream.dto.FileTransmissionRepDTO;
import org.apache.dubbo.common.stream.StreamObserver;

/**
 * @author tanjun
 * @date 2022/12/12 15:17
 * @describe 文件管理,官方推荐IDL方式编写api接口标准,效率和性能更高,但是为了代码可读性选择StreamObserver方式
 */
public interface IFileService {

    /**
     * 双向流
     * 按指定位置读取文件字节返回
     * @param response
     * @return
     */
    StreamObserver<FileTransmissionDTO> readByte(StreamObserver<FileTransmissionDTO> response);

    /**
     * 双向流
     * 写入字节到指定位置
     * @param response 处理响应
     * @return
     */
    StreamObserver<FileTransmissionDTO> writeByte(StreamObserver<FileTransmissionRepDTO> response);

    /**
     * 双向流
     * 分片方式返回文件流
     * @param response
     */
    StreamObserver<FileReadReqDTO> getFile(StreamObserver<FileTransmissionDTO> response);

    /**
     * 双向流
     * 分片方式添加一个文件到磁盘
     * @param response 处理响应
     * @return
     */
    StreamObserver<FileTransmissionDTO> addFile(StreamObserver<FileTransmissionRepDTO> response);
}
