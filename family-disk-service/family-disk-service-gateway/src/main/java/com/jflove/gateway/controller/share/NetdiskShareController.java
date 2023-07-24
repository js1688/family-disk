package com.jflove.gateway.controller.share;

import cn.hutool.json.JSONUtil;
import com.jflove.ResponseHeadDTO;
import com.jflove.gateway.handler.ByteResourceHttpRequestHandler;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.netdisk.api.INetdiskDirectory;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.share.api.INetdiskShare;
import com.jflove.share.dto.NetdiskShareDTO;
import com.jflove.gateway.tool.JJwtTool;
import com.jflove.gateway.vo.ResponseHeadVO;
import com.jflove.gateway.vo.netdisk.DirectoryInfoVO;
import com.jflove.gateway.vo.share.NetdiskShareDirectoryVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @DubboReference
    private INetdiskDirectory netdiskDirectory;

    @Autowired
    private JJwtTool jJwtTool;

    @Autowired
    private ByteResourceHttpRequestHandler resourceHttpRequestHandle;

    private final static String SHARE_TOKEN = "SHARE_TOKEN";//分享token 头部key


    @ApiOperation(value = "媒体资源边播边下")
    @GetMapping("/media/play/{token}/{id}")
    public void mediaPlay(HttpServletRequest request,HttpServletResponse response,
                          @ApiParam("目录id") @PathVariable("id") Long id,
                          @ApiParam("token") @PathVariable("token") String token
    )throws Exception {
        Assert.hasLength(token,"错误的请求:token不能为空");
        request.setAttribute(SHARE_TOKEN,token);
        sliceGetFile(request,response,id);
    }


    @ApiOperation(value = "下载文件(分片下载方式)")
    @RequestMapping(value = "/slice/getFile/{id}", method = {RequestMethod.GET,RequestMethod.POST})
    public void sliceGetFile(
            HttpServletRequest request, HttpServletResponse response,
            @ApiParam("目录id") @PathVariable("id") Long id) throws Exception{
        Assert.notNull(id,"目录id不能为空");
        String token = request.getHeader(SHARE_TOKEN);
        if(!StringUtils.hasLength(token)){
            token = (String) request.getAttribute(SHARE_TOKEN);
        }
        //从token中的负载信息中拿出被授权的文件id,防止越权
        Jws<Claims> jws = jJwtTool.parseJwt(token);
        String fileIds = jws.getBody().get("fileIds",String.class);
        Assert.hasLength(fileIds,"token中的授权id为空");
        List<String> authIds = List.of(fileIds.split(","));
        if(!authIds.contains(id.toString())){
            throw new SecurityException("越权访问");
        }
        ResponseHeadDTO<NetdiskDirectoryDTO> dto = netdiskDirectory.getDirectoryById(id);
        if(!dto.isResult()){
            throw new NullPointerException(dto.getMessage());
        }
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,dto.getData().getFileMd5());
        request.setAttribute(ByteResourceHttpRequestHandler.SOURCE, FileSourceENUM.CLOUDDISK.getCode());
        request.setAttribute(ByteResourceHttpRequestHandler.SPACE_ID,dto.getData().getSpaceId());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,HttpHeaders.CONTENT_RANGE);
        resourceHttpRequestHandle.handleRequest(request, response);
    }


    @ApiOperation(value = "获取网盘分享目录")
    @GetMapping("/getBody/{uuid}")
    public ResponseHeadVO<NetdiskShareDirectoryVO> getBody(
            @ApiParam("链接id") @PathVariable("uuid") String uuid,
            @ApiParam("解锁密码") @RequestParam("password") String password
    ){
        Assert.hasLength(uuid,"错误的请求:链接id不能为空");
        ResponseHeadDTO<NetdiskShareDTO> dto = netdiskShare.getDirectory(uuid,password);
        //将分享的目录下所有文件类型的目录id值添加到临时签发的token负载信息中,以便校验越权问题
        if(dto.isResult()){
            List<Long> fileIds = new ArrayList<>();
            getFileId(fileIds,dto.getData().getList());
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

    private void getFileId(List<Long> s,List<NetdiskDirectoryDTO> dtos){
        if(dtos != null){
            dtos.forEach(v->{
                if(v.getType() == NetdiskDirectoryENUM.FILE){
                    s.add(v.getId());
                }
                getFileId(s,v.getChildren());
            });
        }
    }
}
