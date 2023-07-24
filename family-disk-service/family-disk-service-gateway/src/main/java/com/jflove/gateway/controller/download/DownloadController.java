package com.jflove.gateway.controller.download;

import com.jflove.ResponseHeadDTO;
import com.jflove.download.api.IOfflineDownloadService;
import com.jflove.download.em.UriTypeENUM;
import com.jflove.gateway.config.HttpConstantConfig;
import com.jflove.gateway.vo.ResponseHeadVO;
import com.jflove.gateway.vo.download.AddParamVO;
import com.jflove.gateway.vo.journal.JournalListVO;
import com.jflove.user.em.UserSpaceRoleENUM;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author: tanjun
 * @date: 2023/1/17 10:12 AM
 * @desc:
 */
@RestController
@RequestMapping("/download")
@Api(tags = "离线下载")
@Log4j2
public class DownloadController {

    @DubboReference
    private IOfflineDownloadService offlineDownloadService;

    @Autowired
    private HttpServletRequest autowiredRequest;


    @ApiOperation(value = "添加一个下载任务")
    @PostMapping("/add")
    public ResponseHeadVO<JournalListVO> getJournalList(@RequestBody @Valid AddParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有添加权限");
        }
        ResponseHeadDTO dto = offlineDownloadService.add(UriTypeENUM.valueOf(param.getUriType()),param.getUri(),useSpaceId,param.getTargetId());
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "查询下载文件列表")
    @PostMapping("/getFiles")
    public ResponseHeadVO delJournalList(AddParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        ResponseHeadDTO dto = offlineDownloadService.getFiles(useSpaceId,param.getFileName());
        return new ResponseHeadVO<>(dto.getDatas());
    }
}
