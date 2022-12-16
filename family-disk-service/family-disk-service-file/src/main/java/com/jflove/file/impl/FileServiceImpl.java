package com.jflove.file.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.file.mapper.FileDiskConfigMapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.FileInfoPO;
import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.mapper.FileInfoMapper;
import com.jflove.file.service.IFileReadAndWrit;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author tanjun
 * @date 2022/12/13 10:53
 * @describe
 */
@DubboService
@Log4j2
public class FileServiceImpl implements IFileService {

//    @Autowired
//    @Qualifier("StreamObserverWriteImpl")
//    private StreamObserver so;

    @Value("${file.storage.path.temp}")
    private String tempPath;//分片文件临时存放

    private static final String  tempFileSuffix = ".temp";

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    public StreamObserver<FileTransmissionDTO> addFile(StreamObserver<Boolean> response) {
        return new StreamObserver<FileTransmissionDTO>() {
            @Override
            public void onNext(FileTransmissionDTO data) {
                try {
                    log.info("dubbo文件传输流,接收到传输,文件名称:{},文件类型:{},文件总大小:{},分片个数:{},当前分片序号:{},流大小:{}",
                        data.getName(),data.getType(),data.getTotalSize(),data.getShardingNum(),data.getShardingSort(),data.getShardingStream().length);
                    Files.write(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(data.getShardingSort()),tempFileSuffix)),data.getShardingStream());
                    //判断是否是最后一个分片
                    if(data.getShardingSort() == data.getShardingNum()){
                        log.info("dubbo文件传输本次完毕");
                        //匹配库中是否已存在这个文件,如果存在则不执行写盘,直接引用已存在的文件以及磁盘id
                        FileInfoPO fip = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                                .eq(FileInfoPO::getFileMd5,data.getFileMd5())
                        );
                        FileInfoPO newPo = new FileInfoPO();
                        newPo.setName(data.getName());
                        newPo.setType(data.getType());
                        newPo.setSource(data.getSource().getCode());
                        newPo.setSpaceId(data.getSpaceId());
                        newPo.setCreateUserId(data.getCreateUserId());
                        newPo.setSize(data.getTotalSize());
                        newPo.setFileMd5(data.getFileMd5());
                        if(fip != null){//文件已存在了,直接做关系绑定
                            newPo.setDiskId(fip.getDiskId());//磁盘使用旧的磁盘id
                        }else {
                            //查找出文件可以存放到哪个磁盘上
                            FileDiskConfigPO selectd = fileDiskConfigMapper.selectOne(new LambdaQueryWrapper<FileDiskConfigPO>()
                                    .orderByDesc(FileDiskConfigPO::getUsableSize)//按可用空间降序,取最大的存储
                                    .select(FileDiskConfigPO::getId,FileDiskConfigPO::getPath,FileDiskConfigPO::getType)
                                    .last(" limit 1")
                            );
                            IFileReadAndWrit fileReadAndWrit = applicationContext.getBean("FileReadAndWrit" + selectd.getType(),IFileReadAndWrit.class);
                            fileReadAndWrit.writ(data,selectd,tempFileSuffix,tempPath);//写盘
                            newPo.setDiskId(selectd.getId());
                        }
                        //清除临时文件
                        for (int i = 0; i <= data.getShardingNum(); i++) {
                            Files.deleteIfExists(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(i), tempFileSuffix)));
                        }
                        //将文件信息记录
                        fileInfoMapper.insert(newPo);
                        response.onCompleted();//写盘成功
                    }
                }catch (Exception e){
                    log.error("文件写盘时发生异常",e);
                    response.onError(e);//写盘失败
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    @Override
    public ResponseHeadDTO<Boolean> isExist(String md5, long spaceId,long source) {
        boolean ex = fileInfoMapper.exists(new LambdaQueryWrapper<FileInfoPO>()
                .eq(FileInfoPO::getFileMd5,md5)
                .eq(FileInfoPO::getSpaceId,spaceId)
                .eq(FileInfoPO::getSource,source)
        );
        return new ResponseHeadDTO<>(ex);
    }
}
