package com.jflove.controller.share;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.share.api.INoteShare;
import com.jflove.share.dto.ShareLinkDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.share.NoteShareCreateParamVO;
import com.jflove.vo.share.ShareLinkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/1/17 10:12 AM
 * @desc:
 */
@RestController
@RequestMapping("/note/share")
@Api(tags = "笔记分享")
@Log4j2
public class NoteShareController {

    @DubboReference
    private INoteShare noteShare;

    @Autowired
    private HttpServletRequest autowiredRequest;


    @ApiOperation(value = "获取分享内容")
    @GetMapping("/getBody/{uuid}")
    public ResponseHeadVO<String> getBody(
        @ApiParam("链接id") @PathVariable("uuid") String uuid,
        @ApiParam("解锁密码") @RequestParam("password") String password
    ){
        Assert.notNull(uuid,"错误的请求:链接id不能为空");
        ResponseHeadDTO<String> dto = noteShare.getBody(uuid,password);
        return new ResponseHeadVO<>(dto.isResult(),dto.getData(),dto.getMessage());
    }

    @ApiOperation(value = "获取分享内容列表")
    @GetMapping("/getLinkList")
    public ResponseHeadVO<ShareLinkVO> getLinkList(){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        ResponseHeadDTO<ShareLinkDTO> dto = noteShare.getLinkList(useSpaceId);
        if(dto.isResult()){
            List<ShareLinkVO> vos = new ArrayList<>(dto.getDatas().size());
            dto.getDatas().forEach(v->{
                ShareLinkVO vo = new ShareLinkVO();
                BeanUtils.copyProperties(v,vo);
                vo.setBodyType(v.getBodyType().getCode());
                vo.setInvalidTime(new Date(v.getInvalidTime() * 1000));
                vos.add(vo);
            });
            return new ResponseHeadVO<>(dto.isResult(),vos,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "删除笔记")
    @PostMapping("/delNote")
    public ResponseHeadVO delNote(@RequestBody NoteShareCreateParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有删除权限");
        }
        if(param.getBodyId() == null){
            throw new NullPointerException("内容id不能为空");
        }
        ResponseHeadDTO<String> ret = noteShare.delLink(param.getBodyId(),useSpaceId);
        return new ResponseHeadVO(ret.isResult(),ret.getData(),ret.getMessage());
    }

    @ApiOperation(value = "创建笔记分享")
    @PostMapping("/create")
    public ResponseHeadVO create(@RequestBody @Valid NoteShareCreateParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有创建权限");
        }
        ResponseHeadDTO<String> ret = noteShare.create(param.getPassword(),param.getBodyId(),useSpaceId,param.getInvalidTime());
        return new ResponseHeadVO(ret.isResult(),ret.getData(),ret.getMessage());
    }
}
