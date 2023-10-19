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
import java.io.IOException;
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
            //删掉标记删除了并且已经到了删除时间的数据
            long xz = System.currentTimeMillis() / 1000;
            fileInfoMapper.delete(new LambdaQueryWrapper<FileInfoPO>()
                    .eq(FileInfoPO::getMarkDelete,1)
                    .lt(FileInfoPO::getDeleteTime, xz)//执行删除的时间小于了现在的时间,则代表可以删除了
            );
            //校准磁盘与数据库中的存储数量
            List<FileDiskConfigPO> diskList = fileDiskConfigMapper.selectList(new LambdaQueryWrapper<FileDiskConfigPO>());
            diskList.forEach(v->{
                calibration(v);
            });
        }catch (Exception e){
            log.error("清除过期无引用文件时发生异常",e);
        }
    }

    /**
     * 校准磁盘文件与数据库中存储的数量,并且移除掉再无关联的文件
     * @param selectd
     * @return
     */
    private boolean calibration(FileDiskConfigPO selectd) {
        switch (selectd.getType()){
            case "LOCAL"://本地磁盘中清理
                try {
                    Files.list(Path.of(selectd.getPath())).forEach(v->{
                        String name = v.getFileName().toString();
                        String fileMd5 = name.substring(0,name.indexOf("."));
                        if(!fileInfoMapper.exists(new LambdaQueryWrapper<FileInfoPO>()
                                .eq(FileInfoPO::getFileMd5,fileMd5)
                        )){//没有关联信息了,删文件
                            try {
                                Files.deleteIfExists(v);
                            }catch (IOException e){}
                        }
                    });
                }catch (IOException e){}
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
