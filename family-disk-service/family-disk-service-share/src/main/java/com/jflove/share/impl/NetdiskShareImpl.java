package com.jflove.share.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.netdisk.NetdiskDirectoryPO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.share.ShareLinkPO;
import com.jflove.share.api.INetdiskShare;
import com.jflove.share.dto.DirectoryInfoDTO;
import com.jflove.share.dto.NetdiskShareDTO;
import com.jflove.share.em.ShareBodyTypeENUM;
import com.jflove.share.mapper.NetdiskDirectoryMapper;
import com.jflove.share.mapper.ShareLinkMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author: tanjun
 * @date: 2023/2/14 9:10 AM
 * @desc:
 */
@DubboService
@Log4j2
public class NetdiskShareImpl implements INetdiskShare {

    @Autowired
    private ShareLinkMapper shareLinkMapper;

    @Autowired
    private NetdiskDirectoryMapper netdiskDirectoryMapper;


    @Override
    public ResponseHeadDTO<NetdiskShareDTO> getDirectory(String uuid, String password) {
        ShareLinkPO po = shareLinkMapper.selectOne(new LambdaQueryWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getUuid,uuid)
                .eq(ShareLinkPO::getPassword, Optional.ofNullable(password).orElse(""))
                .eq(ShareLinkPO::getBodyType, ShareBodyTypeENUM.NETDISK.getCode())
        );
        if(po == null){
            if(StringUtils.hasLength(password)){
                return new ResponseHeadDTO<>(false,"分享已失效或密码错误,请刷新网页后重试.");
            }
            return new ResponseHeadDTO<>(false,"分享已失效.");
        }

        NetdiskDirectoryPO ndp = netdiskDirectoryMapper.selectById(po.getBodyId());
        if(ndp == null){
            return new ResponseHeadDTO<>(false,"分享的内容已经被删除了");
        }
        DirectoryInfoDTO d = new DirectoryInfoDTO();
        BeanUtils.copyProperties(ndp,d);
        d.setType(NetdiskDirectoryENUM.valueOf(ndp.getType()));
        addChildDirectory(d);
        NetdiskShareDTO ret = new NetdiskShareDTO();
        List<DirectoryInfoDTO> array = new ArrayList(1);
        array.add(d);
        ret.setList(array);
        ret.setInvalidTime(po.getInvalidTime());
        return new ResponseHeadDTO<>(ret);
    }

    /**
     * 添加子目录
     * @param dto
     */
    private void addChildDirectory(DirectoryInfoDTO dto){
        List<NetdiskDirectoryPO> clist = netdiskDirectoryMapper.selectList(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getPid,dto.getId())
        );
        List<DirectoryInfoDTO> cdto = new ArrayList<>(clist.size());
        clist.forEach(v->{
            DirectoryInfoDTO d = new DirectoryInfoDTO();
            BeanUtils.copyProperties(v,d);
            d.setType(NetdiskDirectoryENUM.valueOf(v.getType()));
            if(NetdiskDirectoryENUM.FOLDER.getCode().equals(v.getType())) {
                addChildDirectory(d);
            }
            cdto.add(d);
        });
        dto.setChild(cdto);
    }
}
