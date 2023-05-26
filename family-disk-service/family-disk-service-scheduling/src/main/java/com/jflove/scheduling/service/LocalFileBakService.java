package com.jflove.scheduling.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.mapper.admin.FileDiskConfigMapper;
import com.jflove.po.file.FileDiskConfigPO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/4/23 11:36 AM
 * @desc: 本地文件备份服务
 */
@Service
@Log4j2
public class LocalFileBakService implements Runnable{

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    @Override
    public void run() {
        try{
            //查询出所有本地磁盘
            List<FileDiskConfigPO> bakList = fileDiskConfigMapper.selectList(new LambdaQueryWrapper<FileDiskConfigPO>()
                    .eq(FileDiskConfigPO::getType,"LOCAL")
                    .isNotNull(FileDiskConfigPO::getBakPath)
            );
            bakList.forEach(v->{
                //todo 目前没有两块磁盘,无法验证,等买了新磁盘做备份的时候再弄
            });
        }catch (Exception e){
            log.error("本地文件备份时发生异常",e);
        }
    }
}
