package com.jflove.manage.impl.share;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.api.IFileAdministration;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.mapper.netdisk.NetdiskDirectoryMapper;
import com.jflove.mapper.share.ShareLinkMapper;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.po.netdisk.NetdiskDirectoryPO;
import com.jflove.po.share.ShareLinkPO;
import com.jflove.share.api.INetdiskShare;
import com.jflove.share.dto.NetdiskShareDTO;
import com.jflove.share.em.ShareBodyTypeENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

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

    @Autowired
    private IFileAdministration fileAdministration;


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
        NetdiskDirectoryDTO d = new NetdiskDirectoryDTO();
        BeanUtils.copyProperties(ndp,d);
        d.setType(NetdiskDirectoryENUM.valueOf(ndp.getType()));
        if(d.getType() == NetdiskDirectoryENUM.FILE){
            ResponseHeadDTO<Long> sizeRet = fileAdministration.getFileSize(d.getFileMd5(),d.getSpaceId(), FileSourceENUM.CLOUDDISK);
            long mb = DataSize.ofBytes(sizeRet.getData()).toMegabytes();
            d.setSize(String.valueOf(mb == 0l ? 1 : mb));
        }else{
            d.setSize("-");
        }
        addChildDirectory(d);
        NetdiskShareDTO ret = new NetdiskShareDTO();
        List<NetdiskDirectoryDTO> array = new ArrayList(1);
        array.add(d);
        ret.setList(array);
        ret.setInvalidTime(po.getInvalidTime());
        return new ResponseHeadDTO<>(ret);
    }

    /**
     * 添加子目录
     * @param dto
     */
    private void addChildDirectory(NetdiskDirectoryDTO dto){
        List<NetdiskDirectoryPO> clist = netdiskDirectoryMapper.selectList(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getPid,dto.getId())
        );
        List<NetdiskDirectoryDTO> cdto = new ArrayList<>(clist.size());
        clist.forEach(v->{
            NetdiskDirectoryDTO d = new NetdiskDirectoryDTO();
            BeanUtils.copyProperties(v,d);
            d.setType(NetdiskDirectoryENUM.valueOf(v.getType()));
            if(d.getType() == NetdiskDirectoryENUM.FILE){
                ResponseHeadDTO<Long> sizeRet = fileAdministration.getFileSize(d.getFileMd5(),d.getSpaceId(), FileSourceENUM.CLOUDDISK);
                long mb = DataSize.ofBytes(sizeRet.getData()).toMegabytes();
                d.setSize(String.valueOf(mb == 0l ? 1 : mb));
            }else{
                d.setSize("-");
            }
            if(NetdiskDirectoryENUM.FOLDER.getCode().equals(v.getType())) {
                addChildDirectory(d);
            }
            cdto.add(d);
        });
        dto.setChildren(cdto);
    }
}
