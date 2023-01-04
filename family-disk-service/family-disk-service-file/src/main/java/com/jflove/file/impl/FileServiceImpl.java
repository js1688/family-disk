package com.jflove.file.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.FileInfoPO;
import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileReadReqDTO;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.dto.FileTransmissionRepDTO;
import com.jflove.file.em.FileSourceENUM;
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

    private static final String  tempFileSuffix = ".temp";

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

            }
        };
    }

    @Override
    @Transactional
    public ResponseHeadDTO updateName(String md5, long spaceId, String name, FileSourceENUM source) {
        FileInfoPO po = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                .eq(FileInfoPO::getFileMd5,md5)
                .eq(FileInfoPO::getSpaceId,spaceId)
                .eq(FileInfoPO::getSource,source.getCode())
                .eq(FileInfoPO::getMarkDelete,0)
        );
        if(po == null){
            return new ResponseHeadDTO(false,"文件不存在");
        }
        po.setName(name);
        po.setUpdateTime(null);
        fileInfoMapper.updateById(po);
        return new ResponseHeadDTO(true,"修改成功");
    }

    @Override
    public StreamObserver<FileTransmissionDTO> addFile(StreamObserver<FileTransmissionRepDTO> response) {
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
                        //检查用户是否还可以存储
                        DataSize ds = DataSize.ofBytes(data.getTotalSize());
                        ResponseHeadDTO use = userSpace.useSpaceByte(data.getSpaceId(),ds.toMegabytes());
                        if(!use.isResult()){
                            response.onNext(new FileTransmissionRepDTO(data.getName(),data.getFileMd5(),false,"用户存储空间不足"));
                            return;
                        }
                        //检查这个文件是否已经在用户的垃圾箱
                        FileInfoPO delFile = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                                .eq(FileInfoPO::getFileMd5,data.getFileMd5())
                                .eq(FileInfoPO::getSpaceId,data.getSpaceId())
                                .eq(FileInfoPO::getSource,data.getSource().getCode())
                                .eq(FileInfoPO::getMarkDelete,1)
                        );
                        if(delFile != null){
                            delFile.setDeleteTime(0);
                            delFile.setMarkDelete(0);
                            delFile.setUpdateTime(null);
                            fileInfoMapper.updateById(delFile);
                        }else{
                            //匹配库中是否已存在这个文件,如果存在则不执行写盘,直接引用已存在的文件以及磁盘id
                            FileInfoPO fip = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                                    .eq(FileInfoPO::getFileMd5,data.getFileMd5())
                                    .last(" limit 1")
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
                                        .last(" limit 1")
                                );
                                //判断是否还存的下
                                if(ds.toGigabytes() > selectd.getUsableSize() - tempSpace.toGigabytes()){
                                    throw new RuntimeException(String.format("文件写盘失败,服务器存储空间已不足%sGB.",String.valueOf(tempSpace.toGigabytes())));
                                }
                                IFileReadAndWrit fileReadAndWrit = applicationContext.getBean(IFileReadAndWrit.BEAN_PREFIX + selectd.getType(),IFileReadAndWrit.class);
                                fileReadAndWrit.writ(data,selectd,tempFileSuffix,tempPath);//写盘
                                newPo.setDiskId(selectd.getId());
                                //刷新磁盘可使用空间
                                File file = new File(selectd.getPath());
                                DataSize total = DataSize.ofBytes(file.getTotalSpace());
                                DataSize usableSpace = DataSize.ofBytes(file.getUsableSpace());//直接从磁盘中读取剩余可用空间,更加准确
                                selectd.setMaxSize(total.toGigabytes());
                                selectd.setUsableSize(usableSpace.toGigabytes());
                                selectd.setUpdateTime(null);
                                fileDiskConfigMapper.updateById(selectd);
                            }
                            //将文件信息记录
                            fileInfoMapper.insert(newPo);
                        }
                        //清除临时文件
                        for (int i = 0; i <= data.getShardingNum(); i++) {
                            Files.deleteIfExists(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(i), tempFileSuffix)));
                        }
                        response.onNext(new FileTransmissionRepDTO(data.getName(),data.getFileMd5(),true,"文件写盘成功"));
                    }
                }catch (Exception e){
                    log.error("文件写盘时发生异常",e);
                    //清除临时文件
                    for (int i = 0; i <= data.getShardingNum(); i++) {
                        try {
                            Files.deleteIfExists(Path.of(String.format("%s/%s-%s%s", tempPath, data.getFileMd5(), String.valueOf(i), tempFileSuffix)));
                        }catch (IOException e1){}
                    }
                    response.onNext(new FileTransmissionRepDTO(data.getName(),data.getFileMd5(),false,"文件写盘失败"));
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
    public ResponseHeadDTO<Boolean> isExist(String md5, long spaceId, FileSourceENUM source) {
        boolean ex = fileInfoMapper.exists(new LambdaQueryWrapper<FileInfoPO>()
                .eq(FileInfoPO::getFileMd5,md5)
                .eq(FileInfoPO::getSpaceId,spaceId)
                .eq(FileInfoPO::getSource,source)
                .eq(FileInfoPO::getMarkDelete,0)
        );
        return new ResponseHeadDTO<>(ex);
    }

    @Override
    @Transactional
    public ResponseHeadDTO<Boolean> delFile(String md5, long spaceId, FileSourceENUM source) {
        FileInfoPO po = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                .eq(FileInfoPO::getSource,source)
                .eq(FileInfoPO::getFileMd5,md5)
                .eq(FileInfoPO::getSpaceId,spaceId)
                .eq(FileInfoPO::getMarkDelete,0)
        );
        if(po == null){
            return new ResponseHeadDTO<>(false,"删除失败,不存在这个文件");
        }
        po.setMarkDelete(1);//标记删除
        int after = 30;//三十天后删除
        po.setDeleteTime((System.currentTimeMillis() / 1000) + (60 * 60 * 24 * after));
        po.setUpdateTime(null);
        fileInfoMapper.updateById(po);
        return new ResponseHeadDTO<>(true,true,"文件已删除,可以在回收站中找回或彻底删除.");
    }
}
