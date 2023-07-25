package com.jflove.scheduling.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.mapper.admin.FileDiskConfigMapper;
import com.jflove.mapper.file.FileInfoMapper;
import com.jflove.po.file.FileDiskConfigPO;
import com.jflove.po.file.FileInfoPO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/4/23 11:36 AM
 * @desc: 清除回收站中,无引用,过期文件
 */
@Service
@Log4j2
@EnableAsync
public class ClearExpireFileService{
    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    @Async("myTaskExecutor")
    @Transactional
    public void run() {
        try{
            //查询出哪些文件信息已经到了要删除的时候
            long xz = System.currentTimeMillis() / 1000;
            List<FileInfoPO> delFiles =  fileInfoMapper.selectList(new LambdaQueryWrapper<FileInfoPO>()
                    .eq(FileInfoPO::getMarkDelete,1)
                    .lt(FileInfoPO::getDeleteTime, xz)//执行删除的时间小于了现在的时间,则代表可以删除了
            );
            delFiles.forEach(v-> {
                if(!fileInfoMapper.exists(new LambdaQueryWrapper<FileInfoPO>()
                        .eq(FileInfoPO::getFileMd5, v.getFileMd5())
                        .and(
                                e -> e.eq(FileInfoPO::getMarkDelete, 0)//文件没有被标记删除
                                        .or(
                                                e2 -> e2.eq(FileInfoPO::getMarkDelete, 1).gt(FileInfoPO::getDeleteTime, xz)//文件被标记删除了但还未到清理日期
                                        )
                        ).last(" limit 1")
                )){
                    //找到文件所属磁盘信息
                    FileDiskConfigPO selectd = fileDiskConfigMapper.selectOne(new LambdaQueryWrapper<FileDiskConfigPO>()
                            .eq(FileDiskConfigPO::getId, v.getDiskId())
                    );
                    clearDisk(selectd,v);//执行清理
                    fileInfoMapper.deleteById(v.getId());
                }
            });
        }catch (Exception e){
            log.error("清除过期无引用文件时发生异常",e);
        }
    }

    /**
     * 从磁盘中清理文件
     * @param selectd
     * @param infoPO
     * @return
     */
    private boolean clearDisk(FileDiskConfigPO selectd,FileInfoPO infoPO) {
        switch (selectd.getType()){
            case "LOCAL"://本地磁盘中清理
                String path = String.format("%s/%s%s", selectd.getPath(), infoPO.getFileMd5(), infoPO.getType());
                try {
                    Files.deleteIfExists(Path.of(path));
                }catch (Exception e){}
                //刷新磁盘可使用空间
                File file = new File(selectd.getPath());
                DataSize total = DataSize.ofBytes(file.getTotalSpace());
                DataSize usableSpace = DataSize.ofBytes(file.getUsableSpace());//直接从磁盘中读取剩余可用空间,更加准确
                selectd.setMaxSize(total.toGigabytes());
                selectd.setUsableSize(usableSpace.toGigabytes());
                selectd.setUpdateTime(null);
                fileDiskConfigMapper.updateById(selectd);
                return true;
        }
        return false;
    }
}
