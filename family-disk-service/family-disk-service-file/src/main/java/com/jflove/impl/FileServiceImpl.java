package com.jflove.impl;

import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileTransmissionDTO;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author tanjun
 * @date 2022/12/13 10:53
 * @describe
 */
@DubboService
@Log4j2
public class FileServiceImpl implements IFileService {

    @Autowired
    @Qualifier("StreamObserverWriteImpl")
    private StreamObserver so;

    @Override
    public StreamObserver<FileTransmissionDTO> addFile(StreamObserver<Boolean> response) {
        return so;
    }
}
