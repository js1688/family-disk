package com.jflove.scheduling.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.mapper.file.FileInfoMapper;
import com.jflove.mapper.user.UserSpaceMapper;
import com.jflove.po.file.FileInfoPO;
import com.jflove.po.user.UserSpacePO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: tanjun
 * @date: 2023/4/23 11:53 AM
 * @desc: 纠正用户空间使用量
 */
@Service
@Log4j2
@EnableAsync
public class CorrectUseSpaceService{
    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private UserSpaceMapper userSpaceMapper;

    @Async("myTaskExecutor")
    public void run() {
        //查出每个空间存储的文件
        List<FileInfoPO> allSize = fileInfoMapper.selectList(new LambdaQueryWrapper<FileInfoPO>().select(FileInfoPO::getSpaceId,FileInfoPO::getSize));
        //按空间id汇总统计使用量
        Map<Long, LongSummaryStatistics> count = allSize.stream().collect(Collectors.groupingBy(FileInfoPO::getSpaceId, Collectors.summarizingLong(FileInfoPO::getSize)));
        count.forEach((k,v)->{
            UserSpacePO usp = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>()
                    .eq(UserSpacePO::getId, k)
            );
            if(usp != null){
                long approval = DataSize.of(v.getSum(), DataUnit.BYTES).toMegabytes();//本次核算的使用量
                long recorded = usp.getUseSize();//数据库记录的已使用量
                long differ = approval - recorded;//正负差
                usp.setUseSize(recorded + differ);
                usp.setUpdateTime(null);
                userSpaceMapper.updateById(usp);
            }
        });
    }
}
