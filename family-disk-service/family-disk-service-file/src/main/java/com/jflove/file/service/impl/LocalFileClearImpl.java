package com.jflove.file.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.file.FileDiskConfigPO;
import com.jflove.file.FileInfoPO;
import com.jflove.file.impl.FileServiceImpl;
import com.jflove.file.mapper.FileDiskConfigMapper;
import com.jflove.file.mapper.FileInfoMapper;
import com.jflove.file.service.IFileClear;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/1/13 9:30 AM
 * @desc: 本地磁盘文件清理
 */
@Log4j2
@Component
@EnableScheduling
public class LocalFileClearImpl implements IFileClear {
    @Value("${file.storage.path.temp}")
    private String tempPath;//分片文件临时存放

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    /**
     * 清除临时目录中非今天的文件
     */
    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    public void clearTemp() {
        File[] fs = new File(tempPath).listFiles(e->e.getName().endsWith(FileServiceImpl.tempFileSuffix));
        for (File f:fs) {
            try {
                BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                FileTime time = attrs.lastModifiedTime();
                DateTime now = DateUtil.beginOfDay(new Date());
                if(now.getTime() > time.toMillis()){//今天的开始时间戳大于了文件时间戳,则代表文件是需要被清理的过期临时文件
                    f.delete();
                }
            }catch (IOException e){}
        }
    }

    /**
     * 清除那些回收站中已经到期的数据
     */
    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    public void clearDustbin(){
        //查询出哪些文件信息已经到了要删除的时候
        long xz = System.currentTimeMillis() / 1000;
        List<FileInfoPO> delFiles =  fileInfoMapper.selectList(new LambdaQueryWrapper<FileInfoPO>()
                .eq(FileInfoPO::getMarkDelete,1)
                .lt(FileInfoPO::getDeleteTime, xz)//执行删除的时间小于了现在的时间,则代表可以删除了
        );
        delFiles.forEach(v->{
            try {
                //不存在其它引用了才执行删除物理文件
                if (!fileInfoMapper.exists(new LambdaQueryWrapper<FileInfoPO>()
                        .eq(FileInfoPO::getFileMd5, v.getFileMd5())
                        .and(
                                e -> e.eq(FileInfoPO::getMarkDelete, 0)//文件没有被标记删除
                                        .or(
                                                e2 -> e2.eq(FileInfoPO::getMarkDelete, 1).gt(FileInfoPO::getDeleteTime, xz)//文件被标记删除了但还未到清理日期
                                        )
                        ).last(" limit 1")
                )) {
                    FileDiskConfigPO selectd = fileDiskConfigMapper.selectOne(new LambdaQueryWrapper<FileDiskConfigPO>()
                            .eq(FileDiskConfigPO::getId, v.getDiskId())
                            .eq(FileDiskConfigPO::getType, "LOCAL")//这个实现只管本地磁盘的
                    );
                    if (selectd == null) {
                        return;
                    }
                    String path = String.format("%s/%s%s", selectd.getPath(), v.getFileMd5(), v.getType());
                    Files.deleteIfExists(Path.of(path));
                }
                fileInfoMapper.deleteById(v.getId());
            }catch (IOException e){
                log.error("清理垃圾箱中的文件时发生异常",e);
            }
        });
    }
}
