package com.jflove.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.mapper.user.UserInfoMapper;
import com.jflove.mapper.user.UserSpaceMapper;
import com.jflove.mapper.user.UserSpaceRelMapper;
import com.jflove.po.user.UserInfoPO;
import com.jflove.po.user.UserSpacePO;
import com.jflove.po.user.UserSpaceRelPO;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserRelStateENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    /**
     * 在改变空间大小的时候,需要使用到的锁
     * key 空间id
     */
    private ConcurrentHashMap<String, Lock> spaceSizeLock = new ConcurrentHashMap<>();


    @Override
    @Transactional
    public ResponseHeadDTO useSpaceByte(Long spaceId, long useMb,boolean increase,boolean isUse) {
        ResponseHeadDTO<UserSpaceDTO> info = getSpaceInfo(spaceId);
        if (!info.isResult()) {
            return new ResponseHeadDTO(info.isResult(), info.getMessage());
        }
        String lockKey = spaceId.toString();
        if(!spaceSizeLock.containsKey(lockKey)){
            Lock lock = new ReentrantLock(true);
            spaceSizeLock.put(lockKey,lock);
        }
        try{
            spaceSizeLock.get(lockKey).lock();
            //使用大小+1mb,因为计算会忽略小数
            //这里的使用空间可能会与电脑文件上看到的不一样,因为本程序计算的字节大小是1024b为1k,电脑系统大多数都是以1000b为1k,包括大部分硬盘的存量计算也是1000
            UserSpaceDTO usd = info.getData();
            useMb += 1;
            if(increase && usd.getMaxSize() - usd.getUseSize() <= useMb){//如果是增加已使用量
                return new ResponseHeadDTO(false, "用户空间不足");
            }
            if(isUse){
                UserSpacePO po = new UserSpacePO();
                BeanUtils.copyProperties(usd, po);
                po.setUseSize(increase ? usd.getUseSize() + useMb : usd.getUseSize() - useMb);
                po.setUpdateTime(null);
                userSpaceMapper.updateById(po);
                return new ResponseHeadDTO(true, "用户空间使用量修改成功");
            }
        }finally {
            spaceSizeLock.get(lockKey).unlock();
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
    @Transactional
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
    @Transactional
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
    public ResponseHeadDTO<UserSpaceRelDTO> getUserInfoBySpaceId(long createUserId) {
        UserSpacePO po = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>().eq(UserSpacePO::getCreateUserId,createUserId));
        if(po == null){
            return new ResponseHeadDTO(false,"你没有自己的空间");
        }
        List<UserSpaceRelPO> usrp = userSpaceRelMapper.selectList(new LambdaQueryWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getCreateUserId,createUserId)
                .eq(UserSpaceRelPO::getSpaceId,po.getId())
                .ne(UserSpaceRelPO::getUserId,createUserId)
        );
        if(usrp == null || usrp.size() == 0){
            return new ResponseHeadDTO<>(true,new ArrayList<>(),"未查到数据");
        }
        List<UserSpaceRelDTO> uids = new ArrayList<>(usrp.size());
        usrp.forEach(v->{
            UserSpaceRelDTO dto = new UserSpaceRelDTO();
            BeanUtils.copyProperties(v,dto);
            UserInfoPO uip = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoPO>()
                    .eq(UserInfoPO::getId,v.getUserId())
            );
            if(uip != null){
                dto.setState(UserRelStateENUM.valueOf(v.getState()));
                if(StringUtils.hasLength(v.getRole())) {
                    dto.setRole(UserSpaceRoleENUM.valueOf(v.getRole()));
                }
                dto.setUserName(uip.getName());
                uids.add(dto);
            }
        });
        return new ResponseHeadDTO<>(uids);
    }

    @Override
    @Transactional
    public ResponseHeadDTO inviteSpace(String email, long userId) {
        UserSpacePO po = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>().eq(UserSpacePO::getCreateUserId,userId));
        if(po == null){
            return new ResponseHeadDTO(false,"邀请失败,你没有自己的空间");
        }
        UserInfoPO userInfoPO = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfoPO>().eq(UserInfoPO::getEmail,email));
        if(userInfoPO == null){
            return new ResponseHeadDTO(false,"邀请失败,用户不存在");
        }
        UserSpaceRelPO usrp = new UserSpaceRelPO();
        usrp.setSpaceId(po.getId());
        usrp.setCreateUserId(po.getCreateUserId());
        usrp.setUserId(userInfoPO.getId());
        usrp.setTitle(po.getTitle());
        usrp.setState(UserRelStateENUM.NOTUSED.getCode());//设置成未使用
        usrp.setRole(UserSpaceRoleENUM.READ.getCode());//设置成只读
        userSpaceRelMapper.insert(usrp);
        return new ResponseHeadDTO(true,"邀请成功,默认权限:" + UserSpaceRoleENUM.READ.getName());
    }

    @Override
    @Transactional
    public ResponseHeadDTO exitRel(long spaceId, long userId) {
        UserSpacePO po = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>().eq(UserSpacePO::getId,spaceId));
        if(po == null){
            return new ResponseHeadDTO(false,"退出失败,没有这个空间");
        }
        if(po.getCreateUserId() == userId){
            return new ResponseHeadDTO(false,"退出失败,你不能退出自己的空间");
        }
        UserSpaceRelPO usrp = userSpaceRelMapper.selectOne(new LambdaUpdateWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getCreateUserId,po.getCreateUserId())
                .eq(UserSpaceRelPO::getUserId,userId)
                .eq(UserSpaceRelPO::getSpaceId,spaceId)
        );
        if(usrp == null){
            return new ResponseHeadDTO(false,"退出失败,不存在这个关系");
        }
        //正在使用的空间不能退出
        if(UserRelStateENUM.USE.getCode().equals(usrp.getState())){
            return new ResponseHeadDTO(false,"退出失败,你正在使用这个空间");
        }
        userSpaceRelMapper.deleteById(usrp.getId());
        return new ResponseHeadDTO(true,"退出成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO removeRel(long useUserId,long removeUserId) {
        if(useUserId == removeUserId){
            return new ResponseHeadDTO(false,"移除失败,不能移除自己");
        }
        UserSpacePO po = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>().eq(UserSpacePO::getCreateUserId,useUserId));
        if(po == null){
            return new ResponseHeadDTO(false,"移除失败,你没有自己的空间");
        }

        UserSpaceRelPO usrp = userSpaceRelMapper.selectOne(new LambdaUpdateWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getCreateUserId,useUserId)
                .eq(UserSpaceRelPO::getUserId,removeUserId)
                .eq(UserSpaceRelPO::getSpaceId,po.getId())
        );
        if(usrp == null){
            return new ResponseHeadDTO(false,"移除失败,不存在这个关系");
        }
        //如果删除的权限是正在使用的,那删除后将用户自己创建的空间设置成正在使用,关联用户id和创建用户id都是同一个id就可以确定这条关系是他创建的空间
        if(UserRelStateENUM.USE.getCode().equals(usrp.getState())){
            UserSpaceRelPO createRel = userSpaceRelMapper.selectOne(new LambdaQueryWrapper<UserSpaceRelPO>()
                    .eq(UserSpaceRelPO::getCreateUserId,removeUserId)
                    .eq(UserSpaceRelPO::getUserId,removeUserId)
            );
            if(createRel != null){
                createRel.setUpdateTime(null);
                createRel.setState(UserRelStateENUM.USE.getCode());
                userSpaceRelMapper.updateById(createRel);
            }
        }
        userSpaceRelMapper.deleteById(usrp.getId());
        return new ResponseHeadDTO(true,"移除成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO setRelRole(long useUserId, long targetUserId, UserSpaceRoleENUM role) {
        UserSpacePO po = userSpaceMapper.selectOne(new LambdaQueryWrapper<UserSpacePO>().eq(UserSpacePO::getCreateUserId,useUserId));
        if(po == null){
            return new ResponseHeadDTO(false,"设置失败,你没有自己的空间");
        }
        UserSpaceRelPO usrp = userSpaceRelMapper.selectOne(new LambdaQueryWrapper<UserSpaceRelPO>()
                .eq(UserSpaceRelPO::getSpaceId,po.getId())
                .eq(UserSpaceRelPO::getCreateUserId,useUserId)
                .eq(UserSpaceRelPO::getUserId,targetUserId)
        );
        if(usrp == null){
            return new ResponseHeadDTO<>(false,"设置失败");
        }
        if(UserRelStateENUM.APPROVAL.getCode().equals(usrp.getState())){//如果关系是待审批,直接设置成未使用
            usrp.setState(UserRelStateENUM.NOTUSED.getCode());
        }
        usrp.setUpdateTime(null);
        usrp.setRole(role.getCode());
        userSpaceRelMapper.updateById(usrp);
        return new ResponseHeadDTO(true,"设置权限成功");
    }
}
