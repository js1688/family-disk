package com.jflove.service;

import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.mapper.FileInfoMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author tanjun
 * @date 2022/12/13 15:32
 * @describe dubbo 文件传输流写入实现,非单例
 */
@Component("StreamObserverWriteImpl")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Log4j2
public class StreamObserverWriteImpl implements StreamObserver<FileTransmissionDTO> {

    @Value("${file.storage.path.temp}")
    private String tempPath;//分片文件临时存放

    private static final String  tempFileSuffix = ".temp";

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    public void onNext(FileTransmissionDTO data) {
        log.info("dubbo文件传输流,接收到传输,文件名称:{},文件类型:{},文件总大小:{},分片个数:{},当前分片序号:{},流大小:{}",
                data.getName(),data.getType(),data.getTotalSize(),data.getShardingNum(),data.getShardingSort(),data.getShardingStream().length);
        try {
            Files.write(Path.of(String.format("%s/%s-%s%s", tempPath, data.getCode(), String.valueOf(data.getShardingSort()),tempFileSuffix)),data.getShardingStream());
        }catch (IOException e){
            log.error("写入文件流分片时发生异常",e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("dubbo文件传输流发生异常",throwable);
    }

    @Override
    public void onCompleted() {
        log.info("dubbo文件传输本次完毕");
        //todo 文件传输完毕,开始执行临时分片合并
    }
}
