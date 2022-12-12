package com.jflove.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.FileDiskConfigPO;
import com.jflove.admin.api.IDiskService;
import com.jflove.admin.em.FileDiskTypeENUM;
import com.jflove.admin.mapper.FileDiskConfigMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @author tanjun
 * @date 2022/12/12 10:51
 * @describe 磁盘服务
 */
@DubboService
@Log4j2
public class DiskServiceImpl implements IDiskService {

    @Autowired
    private FileDiskConfigMapper fileDiskConfigMapper;

    @Override
    public ResponseHeadDTO<Long> addDisk(FileDiskTypeENUM type, String path) {
        switch (type){
            case LOCAL:
                try{
                    File file = new File(path);
                    if (file.exists()) {
                        if(!file.isDirectory()){
                            return new ResponseHeadDTO<>(false,"添加失败,地址必须是目录");
                        }
                        //判断地址是否存在
                        if(fileDiskConfigMapper.selectCount(new LambdaQueryWrapper<FileDiskConfigPO>()
                                .eq(FileDiskConfigPO::getType,type.getCode())
                                .eq(FileDiskConfigPO::getPath,path)
                            ) > 0){
                            return new ResponseHeadDTO<>(false,"添加失败,地址不可以重复添加");
                        }
                        //校验本地盘还剩多少可用
                        long total = file.getTotalSpace()/1024/1024/1024;//返回磁盘大小字节 GB
                        long usableSpace = file.getUsableSpace()/1024/1024/1024;//返回可用字节 GB
                        if(usableSpace <= 1){
                            return new ResponseHeadDTO<>(false,"添加失败,可使用空间不足1G");
                        }
                        FileDiskConfigPO po = new FileDiskConfigPO();
                        po.setPath(path);
                        po.setType(type.getCode());
                        po.setMaxSize(total);
                        po.setUsableSize(usableSpace);
                        fileDiskConfigMapper.insert(po);
                        return new ResponseHeadDTO<>(true,po.getId(),
                                String.format("添加磁盘路径成功,总大小(GB):%s,剩余可用(GB):%s",
                                        String.valueOf(total),
                                        String.valueOf(usableSpace)));
                    }
                    return new ResponseHeadDTO<>(false,"添加失败,不存在的磁盘路径");
                }catch (Exception e){

                }
            default:
                return new ResponseHeadDTO<>(false,"添加失败,暂时还不支持添加该类型磁盘");
        }
    }
}
