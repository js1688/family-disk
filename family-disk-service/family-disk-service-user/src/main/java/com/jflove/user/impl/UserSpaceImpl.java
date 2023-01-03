package com.jflove.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.user.UserSpacePO;
import com.jflove.user.UserSpaceRelPO;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.user.mapper.UserSpaceMapper;
import com.jflove.user.mapper.UserSpaceRelMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.util.UUID;

/**
 * @author tanjun
 * @date 2022/12/14 10:31
 * @describe
 */
@DubboService
@Log4j2
public class UserSpaceImpl implements IUserSpace {

    @Autowired
    private UserSpaceMapper userSpaceMapper;
    @Autowired
    private UserSpaceRelMapper userSpaceRelMapper;

    @Value("${user.space.init-size}")
    private DataSize maxFileSize;//创建空间的大小


    @Override
    public ResponseHeadDTO useSpaceByte(Long spaceId, long useMb) {
        ResponseHeadDTO<UserSpaceDTO> info = getSpaceInfo(spaceId);
        if(!info.isResult()){
            return new ResponseHeadDTO(info.isResult(),info.getMessage());
        }
        //使用大小+1mb,因为计算会忽略小数
        useMb+=1;
        UserSpaceDTO usd = info.getData();
        if(!((usd.getMaxSize() - usd.getUseSize()) >= useMb)){
            return new ResponseHeadDTO(false,"用户空间不足");
        }
        UserSpacePO po = new UserSpacePO();
        BeanUtils.copyProperties(usd,po);
        po.setUseSize(po.getUseSize() + useMb);
        po.setUpdateTime(null);
        userSpaceMapper.updateById(po);
        return new ResponseHeadDTO(true,"用户空间使用成功");
    }

    @Override
    public ResponseHeadDTO<UserSpaceDTO> getSpaceInfo(Long spaceId) {
        UserSpacePO usp = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>()
                .eq(UserSpacePO::getId,spaceId)
        );
        if(usp == null){
            return new ResponseHeadDTO<>(false,"空间id不存在");
        }
        UserSpaceDTO dto = new UserSpaceDTO();
        BeanUtils.copyProperties(usp,dto);
        return new ResponseHeadDTO<UserSpaceDTO>(dto);
    }

    @Override
    @Transactional
    public ResponseHeadDTO<UserSpaceDTO> createSpace(Long createUserId, String title) {
        //该用户是否已经创建过空间
        if(userSpaceMapper.selectCount(new LambdaQueryWrapper<UserSpacePO>()
                .eq(UserSpacePO::getCreateUserId,createUserId)
        ) > 0){
            return new ResponseHeadDTO<>(false,"创建空间失败,该用户已拥有自己的空间");
        }
        //可以创建空间
        UserSpacePO usp = new UserSpacePO();
        usp.setCreateUserId(createUserId);
        usp.setTitle(title);
        usp.setMaxSize(maxFileSize.toMegabytes());
        usp.setUseSize(0);
        usp.setCode(UUID.randomUUID().toString());
        userSpaceMapper.insert(usp);
        UserSpaceRelPO usrp = new UserSpaceRelPO();
        usrp.setSpaceId(usp.getId());
        usrp.setCreateUserId(usp.getCreateUserId());
        usrp.setUserId(createUserId);
        usrp.setRole(UserSpaceRoleENUM.WRITE.getCode());
        userSpaceRelMapper.insert(usrp);
        UserSpaceDTO dto = new UserSpaceDTO();
        BeanUtils.copyProperties(usp,dto);
        return new ResponseHeadDTO<>(true,dto,"创建空间成功");
    }
}
