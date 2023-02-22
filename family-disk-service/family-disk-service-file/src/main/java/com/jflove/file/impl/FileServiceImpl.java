package com.jflove.file.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.FileInfoPO;
import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileReadReqDTO;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.dto.FileTransmissionRepDTO;
import com.jflove.file.mapper.FileDiskConfigMapper;
import com.jflove.file.mapper.FileInfoMapper;
import com.jflove.file.service.IFileReadAndWrit;
import com.jflove.user.api.IUserSpace;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;
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

    @Value("${file.storage.path.temp}")
    private String tempPath;//分片文件临时存放

    public static final String  tempFileSuffix = ".temp";

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @DubboReference
    private IUserSpace userSpace;



    @Value("${file.storage.space}")
    private DataSize tempSpace;//临时空间占用大小

    @Override
    public StreamObserver<FileTransmissionDTO> readByte(StreamObserver<FileTransmissionDTO> response) {
        return new StreamObserver<FileTransmissionDTO>() {
            @Override
            public void onNext(FileTransmissionDTO data) {
                FileInfoPO po = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                        .eq(FileInfoPO::getFileMd5,data.getFileMd5())
                        .eq(FileInfoPO::getSpaceId,data.getSpaceId())
                        .eq(FileInfoPO::getSource,data.getSource().getCode())
                );
                if(po == null){
                    response.onError(new RuntimeException("文件不存在"));
                    return;
                }
                data.setType(po.getType());
                data.setMediaType(po.getMediaType());
                //查找磁盘
                FileDiskConfigPO selectd = fileDiskConfigMapper.selectById(po.getDiskId());
                IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(),IFileReadAndWrit.class);
                fileReadAndWrit.readByte(data,selectd,response);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                response.onCompleted();//必须给请求方也响应一个结束,如果不这么做则不会释放,导致内存溢出
            }
        };
    }

    @Override
    public StreamObserver<FileReadReqDTO> getFile(StreamObserver<FileTransmissionDTO> response) {
        return new StreamObserver<FileReadReqDTO>() {
            @Override
            public void onNext(FileReadReqDTO data) {
                FileInfoPO po = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                        .eq(FileInfoPO::getFileMd5,data.getFileMd5())
                        .eq(FileInfoPO::getSpaceId,data.getSpaceId())
                        .eq(FileInfoPO::getSource,data.getSource().getCode())
                );
                if(po == null){
                    response.onError(new RuntimeException("文件不存在"));
                    return;
                }
                FileTransmissionDTO repDto = new FileTransmissionDTO();
                BeanUtils.copyProperties(data,repDto);
                repDto.setType(po.getType());
                repDto.setName(po.getName());
                repDto.setTotalSize(po.getSize());
                repDto.setMediaType(po.getMediaType());
                //查找磁盘
                FileDiskConfigPO selectd = fileDiskConfigMapper.selectById(po.getDiskId());
                IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(),IFileReadAndWrit.class);
                fileReadAndWrit.read(repDto,selectd,response);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                response.onCompleted();//必须给请求方也响应一个结束,如果不这么做则不会释放,导致内存溢出
            }
        };
    }

    @Override
    public StreamObserver<FileTransmissionDTO> writeByte(StreamObserver<FileTransmissionRepDTO> response) {
        return new StreamObserver<FileTransmissionDTO>() {
            @Override
            @Transactional
            public void onNext(FileTransmissionDTO data) {
                log.info("dubbo文件传输流,接收到传输,文件名称:{},文件类型:{},文件总大小:{},当前分片起:{},当前分片止:{},分片数量:{},第{}分片",
                        data.getName(),data.getType(),data.getTotalSize(),data.getRangeStart(),data.getRangeEnd(),data.getShardingNum(),data.getShardingSort());
                try {
                    Path p = Path.of(String.format("%s/%s-%s%s", tempPath,data.getFileMd5(), String.valueOf(data.getShardingSort()), tempFileSuffix));
                    if(Files.notExists(p)) {
                        Files.write(p, data.getShardingStream());
                    }
                }catch (IOException e){
                    log.error("分片存储异常",e);
                    response.onNext(new FileTransmissionRepDTO(data.getName(),data.getFileMd5(),false,"分片文件存储异常"));
                    return;
                }
                //判断所有的分片是否都存在如果存在,则开始合并
                boolean ok = true;
                for (int i = 0; i < data.getShardingNum(); i++) {
                    Path p = Path.of(String.format("%s/%s-%s%s", tempPath,data.getFileMd5(), String.valueOf(i), tempFileSuffix));
                    if(Files.notExists(p)){
                        ok = false;
                        break;
                    }
                }
                if(ok){//这已经是最后一片了,开始合并
                    try {
                        data.setShardingNum(data.getShardingNum()-1);//前端算的分片是从1开始,减1
                        DataSize ds = DataSize.ofBytes(data.getTotalSize());
                        //查找出文件可以存放到哪个磁盘上
                        FileDiskConfigPO selectd = fileDiskConfigMapper.selectOne(new LambdaQueryWrapper<FileDiskConfigPO>()
                                .orderByDesc(FileDiskConfigPO::getUsableSize)//按可用空间降序,取最大的存储
                                .last(" limit 1")
                        );
                        //判断是否还存的下
                        if (ds.toGigabytes() > selectd.getUsableSize() - tempSpace.toGigabytes()) {
                            response.onNext(new FileTransmissionRepDTO(data.getName(), data.getFileMd5(), false, String.format("文件写盘失败,服务器存储空间已不足%sGB.", String.valueOf(tempSpace.toGigabytes()))));
                            return;
                        }
                        //检查用户是否还可以存储
                        ResponseHeadDTO use = userSpace.useSpaceByte(data.getSpaceId(), ds.toMegabytes(), true, true);
                        if (!use.isResult()) {
                            response.onNext(new FileTransmissionRepDTO(data.getName(), data.getFileMd5(), false, "用户存储空间不足"));
                            return;
                        }
                        IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(), IFileReadAndWrit.class);
                        boolean result = fileReadAndWrit.writ(data, selectd, tempFileSuffix, tempPath);//写盘
                        if (!result) {
                            userSpace.useSpaceByte(data.getSpaceId(), ds.toMegabytes(), false, true);
                            response.onNext(new FileTransmissionRepDTO(data.getName(), data.getFileMd5(), false, "文件合并写盘失败"));
                            return;
                        }
                        FileInfoPO newPo = new FileInfoPO();
                        BeanUtils.copyProperties(data, newPo);
                        newPo.setSize(data.getTotalSize());
                        newPo.setDiskId(selectd.getId());
                        newPo.setId(0);
                        newPo.setSource(data.getSource().getCode());
                        //将文件信息记录
                        fileInfoMapper.insert(newPo);
                        //刷新磁盘可使用空间
                        File file = new File(selectd.getPath());
                        DataSize total = DataSize.ofBytes(file.getTotalSpace());
                        DataSize usableSpace = DataSize.ofBytes(file.getUsableSpace());//直接从磁盘中读取剩余可用空间,更加准确
                        selectd.setMaxSize(total.toGigabytes());
                        selectd.setUsableSize(usableSpace.toGigabytes());
                        selectd.setUpdateTime(null);
                        fileDiskConfigMapper.updateById(selectd);
                        response.onNext(new FileTransmissionRepDTO(data.getName(), data.getFileMd5(), true, "文件所有分片写盘成功"));
                    }catch (Exception e){
                        response.onError(e);
                    }finally {
                        //清除临时文件
                        for (int i = 0; i <= data.getShardingNum(); i++) {
                            try {
                                Files.deleteIfExists(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(i), tempFileSuffix)));
                            } catch (IOException e) {
                            }
                        }
                    }
                }else{
                    response.onNext(new FileTransmissionRepDTO(data.getName(),"ok", true, "分片文件写盘成功"));
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                response.onCompleted();//必须给请求方也响应一个结束,如果不这么做则不会释放,导致内存溢出
            }
        };
    }

    @Override
    @Transactional
    public StreamObserver<FileTransmissionDTO> addFile(StreamObserver<FileTransmissionRepDTO> response) {
        return new StreamObserver<FileTransmissionDTO>() {
            @Override
            @Transactional
            public void onNext(FileTransmissionDTO data) {
                log.info("dubbo文件传输流,接收到传输,文件名称:{},文件类型:{},文件总大小:{},分片个数:{},当前分片序号:{},流大小:{},第{}分片",
                        data.getName(),data.getType(),data.getTotalSize(),data.getShardingNum(),data.getShardingSort(),data.getShardingStream().length,data.getShardingSort());
                try {
                    Path s = Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(data.getShardingSort()), tempFileSuffix));
                    if(Files.notExists(s)) {
                        Files.write(s, data.getShardingStream());
                    }
                }catch (IOException e){
                    log.error("分片存储异常",e);
                    response.onNext(new FileTransmissionRepDTO(data.getName(),data.getFileMd5(),false,"分片文件存储异常"));
                    return;
                }
                //判断是否是最后一个分片
                if(data.getShardingSort() == data.getShardingNum()){
                    try {
                        DataSize ds = DataSize.ofBytes(data.getTotalSize());
                        //查找出文件可以存放到哪个磁盘上
                        FileDiskConfigPO selectd = fileDiskConfigMapper.selectOne(new LambdaQueryWrapper<FileDiskConfigPO>()
                                .orderByDesc(FileDiskConfigPO::getUsableSize)//按可用空间降序,取最大的存储
                                .last(" limit 1")
                        );
                        //判断是否还存的下
                        if (ds.toGigabytes() > selectd.getUsableSize() - tempSpace.toGigabytes()) {
                            response.onNext(new FileTransmissionRepDTO(data.getName(),data.getFileMd5(),false,String.format("文件写盘失败,服务器存储空间已不足%sGB.", String.valueOf(tempSpace.toGigabytes()))));
                            return;
                        }

                        //检查用户是否还可以存储
                        ResponseHeadDTO use = userSpace.useSpaceByte(data.getSpaceId(), ds.toMegabytes(), true, true);
                        if (!use.isResult()) {
                            userSpace.useSpaceByte(data.getSpaceId(), ds.toMegabytes(), false, true);
                            response.onNext(new FileTransmissionRepDTO(data.getName(), data.getFileMd5(), false, "用户存储空间不足"));
                            return;
                        }

                        IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(), IFileReadAndWrit.class);
                        boolean result = fileReadAndWrit.writ(data, selectd, tempFileSuffix, tempPath);//写盘
                        if(!result){
                            response.onNext(new FileTransmissionRepDTO(data.getName(), data.getFileMd5(), false, "文件合并写盘失败"));
                            return;
                        }
                        FileInfoPO newPo = new FileInfoPO();
                        BeanUtils.copyProperties(data, newPo);
                        newPo.setSize(data.getTotalSize());
                        newPo.setDiskId(selectd.getId());
                        newPo.setId(0);
                        newPo.setSource(data.getSource().getCode());
                        //将文件信息记录
                        fileInfoMapper.insert(newPo);
                        //刷新磁盘可使用空间
                        File file = new File(selectd.getPath());
                        DataSize total = DataSize.ofBytes(file.getTotalSpace());
                        DataSize usableSpace = DataSize.ofBytes(file.getUsableSpace());//直接从磁盘中读取剩余可用空间,更加准确
                        selectd.setMaxSize(total.toGigabytes());
                        selectd.setUsableSize(usableSpace.toGigabytes());
                        selectd.setUpdateTime(null);
                        fileDiskConfigMapper.updateById(selectd);
                        response.onNext(new FileTransmissionRepDTO(data.getName(), data.getFileMd5(), true, "文件写盘成功"));
                    }catch (Exception e){
                        response.onNext(new FileTransmissionRepDTO(data.getName(),data.getFileMd5(),false,"文件写盘失败"));
                        throw e;
                    }finally {
                        //清除临时文件
                        for (int i = 0; i <= data.getShardingNum(); i++) {
                            try {
                                Files.deleteIfExists(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(i), tempFileSuffix)));
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                response.onCompleted();//必须给请求方也响应一个结束,如果不这么做则不会释放,导致内存溢出
            }
        };
    }


}
