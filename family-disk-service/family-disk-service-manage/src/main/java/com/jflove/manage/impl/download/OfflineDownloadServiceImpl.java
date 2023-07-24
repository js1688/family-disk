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
import com.jflove.po.download.OdRecordPO;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private ApplicationContext context;

    @Override
    @Transactional
    public ResponseHeadDTO add(UriTypeENUM uriType,String uri, Long spaceId, Long targetId) {
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
        //查询一下,从aria2中获取到文件名称,避免多一个参数
        List dwInfo = aria2c.getFiles(gid);
        Map f0 = (Map)dwInfo.get(0);
        String path = (String)f0.get("path");
        String fileName = path.substring(path.lastIndexOf("/")+1);
        op.setFileName(fileName);
        op.setUriType(uriType.getCode());
        op.setSpaceId(spaceId);
        op.setTargetId(targetId);
        odRecordMapper.insert(op);
        return new ResponseHeadDTO(true,String.format("文件[%s]添加到离线下载任务成功",op.getFileName()));
    }

    @Override
    public ResponseHeadDTO getFiles(Long spaceId) {
        List<OdRecordPO> gids = odRecordMapper.selectList(new LambdaQueryWrapper<OdRecordPO>().eq(OdRecordPO::getSpaceId,spaceId));
        JSONArray dwTasks = new JSONArray(gids.size());
        gids.forEach(v->{
            IAria2c aria2c = context.getBean(UriTypeENUM.valueOf(v.getUriType()).getCode(), IAria2c.class);
            List dwInfo = aria2c.getFiles(v.getGid());
            Map f0 = (Map)dwInfo.get(0);
            //删掉不要的节点,避免暴漏过多的信息
            f0.remove("uris");
            f0.remove("path");
            JSONObject jo = JSONUtil.parseObj(v);
            jo.putAll(f0);
            dwTasks.add(jo);
        });
        return new ResponseHeadDTO(dwTasks);
    }
}
