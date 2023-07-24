package com.jflove.manage.impl.share;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.mapper.netdisk.NetdiskDirectoryMapper;
import com.jflove.mapper.notebook.NotebookNoteMapper;
import com.jflove.mapper.share.ShareLinkMapper;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.po.netdisk.NetdiskDirectoryPO;
import com.jflove.po.notebook.NotebookNotePO;
import com.jflove.po.share.ShareLinkPO;
import com.jflove.share.api.IShareAdmin;
import com.jflove.share.dto.ShareLinkDTO;
import com.jflove.share.em.ShareBodyTypeENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author: tanjun
 * @date: 2023/2/13 3:19 PM
 * @desc:
 */
@DubboService
@Log4j2
public class ShareAdminImpl implements IShareAdmin {

    @Autowired
    private ShareLinkMapper shareLinkMapper;

    @Autowired
    private NotebookNoteMapper notebookNoteMapper;
    @Autowired
    private NetdiskDirectoryMapper netdiskDirectoryMapper;

    @Override
    @Transactional
    public ResponseHeadDTO<ShareLinkDTO> create(ShareBodyTypeENUM bodyType,String password, long bodyId, long spaceId, String invalidTime) {
        switch (bodyType){
            case NOTE:
                if(!notebookNoteMapper.exists(new LambdaQueryWrapper<NotebookNotePO>()
                        .eq(NotebookNotePO::getId,bodyId)
                        .eq(NotebookNotePO::getSpaceId,spaceId)
                )){
                    return new ResponseHeadDTO<>(false,"笔记不存在");
                }
                break;
            case NETDISK:
                //判断给定的目录下有多少文件,如果超过了100个则拒绝分享,这样做的目的是防止生成的越权token太大了
                NetdiskDirectoryPO root = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                        .eq(NetdiskDirectoryPO::getId,bodyId)
                        .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
                );
                if(root == null){
                    return new ResponseHeadDTO<>(false,"网盘目录不存在");
                }
                if(NetdiskDirectoryENUM.FOLDER.getCode().equals(root.getType())){
                    List<Long> ids = new ArrayList<>();
                    addChildDirectory(ids,List.of(root),spaceId);
                    //查找所有目录下的文件
                    Long count = netdiskDirectoryMapper.selectCount(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                            .in(NetdiskDirectoryPO::getPid,ids)
                            .eq(NetdiskDirectoryPO::getType,NetdiskDirectoryENUM.FILE.getCode())
                            .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
                    );
                    int max = 50;
                    if(count > 50){
                        return new ResponseHeadDTO<>(false,String.format("该目录下文件内容大于了%s个,大量文件请先压缩上传再分享",max));
                    }
                }
                break;
        }

        String uuid = UUID.randomUUID().toString();
        long dqsj = DateUtil.parse(invalidTime, DatePattern.NORM_DATETIME_PATTERN).getTime() / 1000;
        ShareLinkPO po = new ShareLinkPO();
        po.setBodyType(bodyType.getCode());
        po.setBodyId(bodyId);
        po.setPassword(password);
        po.setSpaceId(spaceId);
        po.setInvalidTime(dqsj);
        po.setUuid(uuid);
        shareLinkMapper.insert(po);
        String link = String.format("lock=%s&uuid=%s", StringUtils.hasLength(password),uuid);
        ShareLinkDTO dto = new ShareLinkDTO();
        BeanUtils.copyProperties(po,dto);
        dto.setBodyType(ShareBodyTypeENUM.valueOf(po.getBodyType()));
        dto.setUrl(link);
        return new ResponseHeadDTO<>(true,dto,"创建分享成功,地址已复制");
    }

    /**
     * 递归查出一个目录下所有的子目录
     * @param ids
     * @param p
     */
    private void addChildDirectory(List<Long> ids,List<NetdiskDirectoryPO> p,long spaceId){
        if(p == null || p.size() == 0){
            return;
        }
        List<Long> pids = p.stream().map(NetdiskDirectoryPO::getId).toList();
        ids.addAll(pids);
        List<NetdiskDirectoryPO> child = netdiskDirectoryMapper.selectList(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .in(NetdiskDirectoryPO::getPid,pids)
                .eq(NetdiskDirectoryPO::getType,NetdiskDirectoryENUM.FOLDER.getCode())
                .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
        );
        addChildDirectory(ids,child,spaceId);
    }

    @Override
    @Transactional
    public ResponseHeadDTO delLink(long id, long spaceId) {
        if(!shareLinkMapper.exists(new LambdaQueryWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getSpaceId,spaceId)
                .eq(ShareLinkPO::getId,id)
        )){
            return new ResponseHeadDTO<>(false,"","链接不存在");
        }
        shareLinkMapper.deleteById(id);
        return new ResponseHeadDTO<>(true,"","删除成功");
    }

    @Override
    public ResponseHeadDTO<ShareLinkDTO> getLinkList(long spaceId) {
        List<ShareLinkPO> pos =  shareLinkMapper.selectList(new LambdaQueryWrapper<ShareLinkPO>()
                .eq(ShareLinkPO::getSpaceId,spaceId)
        );
        List<ShareLinkDTO> dtoList = new ArrayList<>(pos.size());
        pos.forEach(v->{
            ShareLinkDTO listDTO = new ShareLinkDTO();
            BeanUtils.copyProperties(v,listDTO);
            listDTO.setBodyType(ShareBodyTypeENUM.valueOf(v.getBodyType()));
            listDTO.setKeyword("分享的内容已经被删除");
            switch (listDTO.getBodyType()){
                case NOTE:
                    NotebookNotePO notePO = notebookNoteMapper.selectOne(new LambdaQueryWrapper<NotebookNotePO>()
                            .eq(NotebookNotePO::getId,v.getBodyId())
                            .select(NotebookNotePO::getKeyword)
                    );
                    if(notePO != null){
                        listDTO.setKeyword(notePO.getKeyword());
                    }
                    break;
                case NETDISK:
                    NetdiskDirectoryPO diskPO = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                            .eq(NetdiskDirectoryPO::getId,v.getBodyId())
                            .select(NetdiskDirectoryPO::getName)
                    );
                    if(diskPO != null){
                        listDTO.setKeyword(diskPO.getName());
                    }
                    break;
            }
            String link = String.format("lock=%s&uuid=%s", StringUtils.hasLength(v.getPassword()),v.getUuid());
            listDTO.setUrl(link);
            dtoList.add(listDTO);
        });
        return new ResponseHeadDTO<>(true,dtoList,"查询成功");
    }
}
