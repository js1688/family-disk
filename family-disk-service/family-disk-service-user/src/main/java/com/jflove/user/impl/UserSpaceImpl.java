package com.jflove.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.user.UserInfoPO;
import com.jflove.user.UserSpacePO;
import com.jflove.user.UserSpaceRelPO;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.em.UserRelStateENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.user.mapper.UserInfoMapper;
import com.jflove.user.mapper.UserSpaceMapper;
import com.jflove.user.mapper.UserSpaceRelMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Value("${user.space.init-size}")
    private DataSize maxFileSize;//创建空间的大小

    private ConcurrentHashMap<Long/**spaceId**/,Long/**使用量缓存*/> spaceUseMb = new ConcurrentHashMap<>();


    @Override
    @Transactional
    public ResponseHeadDTO useSpaceByte(Long spaceId, long useMb,boolean increase,boolean isUse) {
        ResponseHeadDTO<UserSpaceDTO> info = getSpaceInfo(spaceId);
        if (!info.isResult()) {
            return new ResponseHeadDTO(info.isResult(), info.getMessage());
        }
        //使用大小+1mb,因为计算会忽略小数
        //这里的使用空间可能会与电脑文件上看到的不一样,因为本程序计算的字节大小是1024b为1k,电脑系统大多数都是以1000b为1k,包括大部分硬盘的存量计算也是1000
        useMb += 1;
        UserSpaceDTO usd = info.getData();
        //如果缓存计数大于数据库则代表数据库的写入还未生效,有效使用内存缓存中的计数
        long useSize = 0;
        if(spaceUseMb.containsKey(spaceId)) {
            useSize = spaceUseMb.get(spaceId) > usd.getUseSize() ? spaceUseMb.get(spaceId) : usd.getUseSize();
        }else{
            useSize = usd.getUseSize();
        }
        if (!((usd.getMaxSize() - useSize) >= useMb)) {
            return new ResponseHeadDTO(false, "用户空间不足");
        }
        if (isUse) {
            UserSpacePO po = new UserSpacePO();
            BeanUtils.copyProperties(usd, po);
            po.setUseSize(increase ? useSize + useMb : useSize - useMb);
            spaceUseMb.put(spaceId,po.getUseSize());
            po.setUpdateTime(null);
            userSpaceMapper.updateById(po);
            return new ResponseHeadDTO(true, "用户空间使用成功");
        }
        return new ResponseHeadDTO(true, "用户空间可以存储");
    }

    @Override
    public ResponseHeadDTO<UserSpaceDTO> getSpaceInfo(Long spaceId) {
        UserSpacePO usp = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>()
                .eq(UserSpacePO::getId, spaceId)
        );
        if (usp == null) {
            return new ResponseHeadDTO<>(false, "空间id不存在");
        }
        UserSpaceDTO dto = new UserSpaceDTO();
        BeanUtils.copyProperties(usp, dto);
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
        usrp.setState(UserRelStateENUM.USE.getCode());//默认设置正在使用
        usrp.setTitle(usp.getTitle());
        userSpaceRelMapper.insert(usrp);
        UserSpaceDTO dto = new UserSpaceDTO();
        BeanUtils.copyProperties(usp,dto);
        return new ResponseHeadDTO<>(true,dto,"创建空间成功");
    }

    @Override
    public ResponseHeadDTO joinSpace(String targetSpaceCode, long userId) {
        UserSpacePO usp = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>()
                .eq(UserSpacePO::getCode,targetSpaceCode)
                .ne(UserSpacePO::getCreateUserId,userId)
        );
        if(usp == null){
            return new ResponseHeadDTO(false,"空间编码不存在");
        }
        if(userSpaceRelMapper.exists(new LambdaQueryWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getUserId,userId)
                .eq(UserSpaceRelPO::getSpaceId,usp.getId())
        )){
            return new ResponseHeadDTO(false,"不需要重复提交申请");
        }

        UserSpaceRelPO usrp = new UserSpaceRelPO();
        usrp.setSpaceId(usp.getId());
        usrp.setCreateUserId(usp.getCreateUserId());
        usrp.setUserId(userId);
        usrp.setTitle(usp.getTitle());
        usrp.setState(UserRelStateENUM.APPROVAL.getCode());//设置成待审批
        userSpaceRelMapper.insert(usrp);
        return new ResponseHeadDTO(true,"已申请加入,等待空间所有者审批");
    }

    @Override
    public ResponseHeadDTO switchSpace(long targetSpaceId,long originalSpaceId,long userId) {
        UserSpaceRelPO po = userSpaceRelMapper.selectOne(new LambdaQueryWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getUserId,userId)
                .eq(UserSpaceRelPO::getSpaceId,targetSpaceId)
                .eq(UserSpaceRelPO::getState,UserRelStateENUM.NOTUSED.getCode())
        );
        if(po == null){
            return new ResponseHeadDTO(false,"不能切换到目标空间");
        }
        UserSpaceRelPO po2 = userSpaceRelMapper.selectOne(new LambdaQueryWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getUserId,userId)
                .eq(UserSpaceRelPO::getSpaceId,originalSpaceId)
                .eq(UserSpaceRelPO::getState,UserRelStateENUM.USE.getCode())
        );
        if(po2 == null){
            return new ResponseHeadDTO(false,"原始空间不正确");
        }
        po2.setState(UserRelStateENUM.NOTUSED.getCode());
        po2.setUpdateTime(null);
        userSpaceRelMapper.updateById(po2);
        po.setState(UserRelStateENUM.USE.getCode());
        po.setUpdateTime(null);
        userSpaceRelMapper.updateById(po);
        return new ResponseHeadDTO(true,"切换空间成功");
    }

    @Override
    public ResponseHeadDTO<List<UserInfoDTO>> getUserInfoBySpaceId(long spaceId, long createUserId) {
        List<UserSpaceRelPO> usrp = userSpaceRelMapper.selectList(new LambdaQueryWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getCreateUserId,createUserId)
                .eq(UserSpaceRelPO::getSpaceId,spaceId)
                .ne(UserSpaceRelPO::getUserId,createUserId)
        );
        if(usrp == null || usrp.size() == 0){
            return new ResponseHeadDTO<>(false,new ArrayList<>(),"未查到数据");
        }
        List<Long> userIds = usrp.stream().map(UserSpaceRelPO::getUserId).toList();
        List<UserInfoPO> us = userInfoMapper.selectList(new LambdaQueryWrapper<UserInfoPO>()
                .in(UserInfoPO::getId,userIds)
                .select(UserInfoPO::getId,UserInfoPO::getName,UserInfoPO::getEmail)
        );
        List<UserInfoDTO> uids = new ArrayList<>(us.size());
        us.forEach(v->{
            UserInfoDTO dto = new UserInfoDTO();
            BeanUtils.copyProperties(v,dto);
            uids.add(dto);
        });
        return new ResponseHeadDTO(uids);
    }
}
