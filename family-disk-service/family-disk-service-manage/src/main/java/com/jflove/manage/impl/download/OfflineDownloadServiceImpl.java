package com.jflove.manage.impl.download;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.download.api.IOfflineDownloadService;
import com.jflove.download.em.UriTypeENUM;
import com.jflove.manage.service.IAria2c;
import com.jflove.mapper.download.OdRecordMapper;
import com.jflove.mapper.netdisk.NetdiskDirectoryMapper;
import com.jflove.po.download.OdRecordPO;
import com.jflove.po.netdisk.NetdiskDirectoryPO;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/7/19 11:26 AM
 * @desc:
 */
@DubboService
@Log4j2
public class OfflineDownloadServiceImpl implements IOfflineDownloadService {

    @Autowired
    private OdRecordMapper odRecordMapper;
    @Autowired
    private NetdiskDirectoryMapper netdiskDirectoryMapper;
    @Autowired
    private ApplicationContext context;

    @Override
    @Transactional
    public ResponseHeadDTO add(UriTypeENUM uriType,String uri, Long spaceId, Long targetId) {
        boolean is = netdiskDirectoryMapper.exists(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getId,targetId)
                .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
        );
        if(!is){
            return new ResponseHeadDTO(false, "目标目录不存在");
        }
        //发送下载任务给aria2
        IAria2c aria2c = context.getBean(uriType.getCode(), IAria2c.class);
        if (aria2c == null) {
            return new ResponseHeadDTO(false, "未实现的下载方式");
        }
        String gid = aria2c.addUri(uri);
        if(!StringUtils.hasLength(gid)){
            return new ResponseHeadDTO(false, "添加到离线任务失败");
        }
        //拿到返回的aria2的ID, 写入到数据库中
        OdRecordPO op = new OdRecordPO();
        op.setGid(gid);
        op.setUriType(uriType.getCode());
        op.setSpaceId(spaceId);
        op.setTargetId(targetId);
        odRecordMapper.insert(op);
        return new ResponseHeadDTO(true,String.format("文件[%s]添加到离线下载任务成功",op.getFileName()));
    }

    @Override
    public ResponseHeadDTO getFiles(Long spaceId,String fileName) {
        List<OdRecordPO> gids = odRecordMapper.selectList(new LambdaQueryWrapper<OdRecordPO>()
                .eq(OdRecordPO::getSpaceId,spaceId)
                .like(!StringUtils.hasLength(fileName),OdRecordPO::getFileName,fileName)
        );
        JSONArray dwTasks = new JSONArray(gids.size());
        gids.forEach(v->{
            IAria2c aria2c = context.getBean(UriTypeENUM.valueOf(v.getUriType()).getCode(), IAria2c.class);
            List dwInfo = aria2c.getFiles(v.getGid());
            Map f0 = (Map)dwInfo.get(0);
            String path = (String)f0.get("path");
            String fn = path.substring(path.lastIndexOf("/")+1);
            if(!StringUtils.hasLength(v.getFileName())){
                //如果没有文件名称,则补一下文件名称,因为aria没那么快返回文件名称
                v.setFileName(fn);
                odRecordMapper.updateById(v);
            }
            NetdiskDirectoryPO ndp = netdiskDirectoryMapper.selectOne(
                    new LambdaQueryWrapper<NetdiskDirectoryPO>().eq(NetdiskDirectoryPO::getId,v.getTargetId())
                            .select(NetdiskDirectoryPO::getName)
            );
            //删掉不要的节点,避免暴漏过多的信息
            f0.remove("uris");
            f0.remove("path");
            JSONObject jo = JSONUtil.parseObj(v);
            jo.putOpt("targetName", ndp == null ? "根目录" : ndp.getName());
            DataSize length = DataSize.ofBytes(Long.parseLong((String) f0.get("length")));
            DataSize completedLength = DataSize.ofBytes(Long.parseLong((String) f0.get("completedLength")));
            jo.putOpt("progress",completedLength.toMegabytes() + "/" + length.toMegabytes());
            jo.putAll(f0);
            dwTasks.add(jo);
        });
        return new ResponseHeadDTO(dwTasks);
    }
}
