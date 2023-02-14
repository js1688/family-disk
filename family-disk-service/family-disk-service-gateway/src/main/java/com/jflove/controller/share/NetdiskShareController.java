package com.jflove.controller.share;

import cn.hutool.json.JSONUtil;
import com.jflove.ResponseHeadDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.share.api.INetdiskShare;
import com.jflove.share.dto.DirectoryInfoDTO;
import com.jflove.share.dto.NetdiskShareDTO;
import com.jflove.tool.JJwtTool;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.share.DirectoryInfoVO;
import com.jflove.vo.share.NetdiskShareDirectoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/2/13 5:41 PM
 * @desc:
 */
@RestController
@RequestMapping("/netdisk/share")
@Api(tags = "网盘分享")
@Log4j2
public class NetdiskShareController {

    @DubboReference
    private INetdiskShare netdiskShare;

    @Autowired
    private JJwtTool jJwtTool;

    @ApiOperation(value = "获取笔记分享内容")
    @GetMapping("/getBody/{uuid}")
    public ResponseHeadVO<NetdiskShareDirectoryVO> getBody(
            @ApiParam("链接id") @PathVariable("uuid") String uuid,
            @ApiParam("解锁密码") @RequestParam("password") String password
    ){
        Assert.hasLength(uuid,"错误的请求:链接id不能为空");
        ResponseHeadDTO<NetdiskShareDTO> dto = netdiskShare.getDirectory(uuid,password);
        //将分享的目录下所有文件类型的目录id值添加到临时签发的token负载信息中,以便校验越权问题
        //todo token有可能会很大,到时候可以在网盘模块中创建分享的时候,校验目录下有多少个文件,如果大于了临界点就不让创建分享
        if(dto.isResult()){
            List<Long> fileIds = new ArrayList<>();
            getFileMd5(fileIds,dto.getData().getList());
            NetdiskShareDirectoryVO vo = new NetdiskShareDirectoryVO();
            List<DirectoryInfoVO> list = JSONUtil.toList(JSONUtil.toJsonStr(dto.getData().getList()),DirectoryInfoVO.class);
            vo.setList(list);
            //签发临时token,有效期至分享失效日期
            Map<String, Object> map = new HashMap<>();
            map.put("fileIds", String.join(",", fileIds.stream().map(String::valueOf).toList()));
            String token = jJwtTool.createJwt(null,null,map,dto.getData().getInvalidTime() * 1000l);
            vo.setTempToken(token);
            return new ResponseHeadVO<>(dto.isResult(),vo,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }

    private void getFileMd5(List<Long> s,List<DirectoryInfoDTO> dtos){
        if(dtos != null){
            dtos.forEach(v->{
                if(v.getType() == NetdiskDirectoryENUM.FILE){
                    s.add(v.getId());
                }
                getFileMd5(s,v.getChild());
            });
        }
    }
}
