package com.jflove.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.admin.api.IFileAdministration;
import com.jflove.admin.mapper.FileInfoMapper;
import com.jflove.file.FileInfoPO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.user.api.IUserSpace;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

/**
 * @author: tanjun
 * @date: 2023/1/10 4:52 PM
 * @desc:
 */
@DubboService
@Log4j2
public class FileAdministrationImpl implements IFileAdministration {
    @DubboReference
    private IUserSpace userSpace;
    @Autowired
    private FileInfoMapper fileInfoMapper;

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

    @Override
    @Transactional
    public ResponseHeadDTO dustbinRecovery(String fileMd5,long spaceId,FileSourceENUM source) {
        //检查这个文件是否已经在用户的垃圾箱
        FileInfoPO delFile = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                .eq(FileInfoPO::getFileMd5,fileMd5)
                .eq(FileInfoPO::getSpaceId,spaceId)
                .eq(FileInfoPO::getSource,source.getCode())
                .eq(FileInfoPO::getMarkDelete,1)
        );
        if(delFile != null) {
            delFile.setDeleteTime(0);
            delFile.setMarkDelete(0);
            delFile.setUpdateTime(null);
            fileInfoMapper.updateById(delFile);
            return new ResponseHeadDTO(true,fileMd5,"已经从垃圾箱恢复文件");
        }
        return new ResponseHeadDTO(false,fileMd5,"垃圾箱没有这个文件");
    }

    @Override
    @Transactional
    public ResponseHeadDTO checkDuplicate(String fileName,String type,String mediaType,String fileMd5,long spaceId,FileSourceENUM source,long totalSize,long createUserId) {
        //匹配库中是否已存在这个文件,如果存在则不执行写盘,直接引用已存在的文件以及磁盘id
        FileInfoPO fip = fileInfoMapper.selectOne(new LambdaQueryWrapper<FileInfoPO>()
                .eq(FileInfoPO::getFileMd5,fileMd5)
                .last(" limit 1")
        );
        if(fip == null){
            return new ResponseHeadDTO(false,fileMd5,"磁盘中没有这个文件可以引用");
        }
        if(fip.getSpaceId() != spaceId) {//不是自己空间的文件
            //是否可以存的下
            DataSize ds = DataSize.ofBytes(totalSize);
            ResponseHeadDTO use = userSpace.useSpaceByte(spaceId,ds.toMegabytes(),true,true);
            if(!use.isResult()){
                return new ResponseHeadDTO(false,fileMd5,"用户存储空间不足");
            }
            FileInfoPO newPo = new FileInfoPO();
            newPo.setMediaType(mediaType);
            newPo.setName(fileName);
            newPo.setType(type);
            newPo.setFileMd5(fileMd5);
            newPo.setCreateUserId(createUserId);
            newPo.setSpaceId(spaceId);
            newPo.setSource(source.getCode());
            newPo.setSize(totalSize);
            newPo.setDiskId(fip.getDiskId());
            fileInfoMapper.insert(newPo);
        }
        return new ResponseHeadDTO(true,fileMd5,"文件在服务器中已存在,实现秒存");
    }
}
