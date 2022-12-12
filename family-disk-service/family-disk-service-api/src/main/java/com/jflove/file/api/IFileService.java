package com.jflove.file.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.file.dto.FileInfoDTO;
import org.apache.dubbo.common.stream.StreamObserver;

/**
 * @author tanjun
 * @date 2022/12/12 15:17
 * @describe 文件管理,官方推荐IDL方式编写api接口标准,效率和性能更高,但是为了代码可读性选择StreamObserver方式
 */
public interface IFileService {

    //todo 要做到支持文件分片传输过来,存储到临时目录,最后做一次合并,传输时可以多线程增加速度,这样即使最后网络环境太差,传输中断,也只需要继续发送还未传输的分片即可
    //todo 这个地方的文件传输,速度,大文件,分片,中断后续传,是一个重点解决的问题
    ResponseHeadDTO<FileInfoDTO> addFile(StreamObserver<String> response,FileInfoDTO fileInfo);
}
