package com.jflove.gateway.controller.user;

import com.jflove.ResponseHeadDTO;
import com.jflove.gateway.config.HttpConstantConfig;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.gateway.vo.ResponseHeadVO;
import com.jflove.gateway.vo.user.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tanjun
 * @date 2022/12/14 10:21
 * @describe 用户空间管理
 */
@RestController
@RequestMapping("/user/space")
@Api(tags = "用户空间管理")
public class UserSpaceController {

    @DubboReference
    private IUserSpace userSpace;

    @Autowired
    private HttpServletRequest autowiredRequest;


    @ApiOperation(value = "获取正在使用的空间信息")
    @GetMapping("/getSpaceInfo")
    public ResponseHeadVO<UserSpaceVO> getSpaceInfo(){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        ResponseHeadDTO<UserSpaceDTO> dto = userSpace.getSpaceInfo(useSpaceId);
        if(dto.isResult()){
            UserSpaceVO vo = new UserSpaceVO();
            BeanUtils.copyProperties(dto.getData(),vo);
            return new ResponseHeadVO<>(dto.isResult(),vo,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "创建空间")
    @PostMapping("/createSpace")
    public ResponseHeadVO<UserSpaceVO> createSpace(@RequestBody @Valid CreateSpaceParamVO param){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO<UserSpaceDTO> dto = userSpace.createSpace(useUserId,param.getTitle());
        if(dto.isResult()){
            UserSpaceVO vo = new UserSpaceVO();
            BeanUtils.copyProperties(dto.getData(),vo);
            return new ResponseHeadVO<>(dto.isResult(),vo,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "切换空间")
    @PostMapping("/switchSpace")
    public ResponseHeadVO switchSpace(@RequestBody @Valid SwitchSpaceParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO dto = userSpace.switchSpace(param.getTargetSpaceId(),useSpaceId,useUserId);
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "申请加入空间")
    @PostMapping("/joinSpace")
    public ResponseHeadVO joinSpace(@RequestBody @Valid JoinSpaceParamVO param){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO dto = userSpace.joinSpace(param.getTargetSpaceCode(),useUserId);
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "移除空间与用户关系")
    @PostMapping("/removeRel")
    public ResponseHeadVO removeRel(@RequestBody @Valid RemoveRelParamVO param){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO dto = userSpace.removeRel(useUserId,param.getRemoveUserId());
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "邀请对方加入我的空间")
    @PostMapping("/inviteSpace")
    public ResponseHeadVO inviteSpace(@RequestBody @Valid GetUserInfoByEmailParamVO param){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO dto = userSpace.inviteSpace(param.getEmail(),useUserId);
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "用户退出空间")
    @PostMapping("/exitRel")
    public ResponseHeadVO exitRel(@RequestBody @Valid ExitRelParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Assert.notNull(useSpaceId,"请先切换到空间");
        if(useSpaceId.longValue() == param.getSpaceId().longValue()){
            throw new SecurityException("退出失败,你正在使用这个空间");
        }
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO dto = userSpace.exitRel(param.getSpaceId(),useUserId);
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "设置用户与空间的权限")
    @PostMapping("/setRelRole")
    public ResponseHeadVO setRelRole(@RequestBody @Valid SetRelRoleParamVO param){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO dto = userSpace.setRelRole(useUserId,param.getTargetUserId(),UserSpaceRoleENUM.valueOf(param.getRole()));
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "查找用户创建的空间下有多少关联用户")
    @GetMapping("/getUserInfoBySpaceId")
    public ResponseHeadVO<UserSpaceRelVO> getUserInfoBySpaceId(){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        ResponseHeadDTO<UserSpaceRelDTO> dto = userSpace.getUserInfoBySpaceId(useUserId);
        if(dto.isResult()){
            List<UserSpaceRelVO> list = new ArrayList<>(dto.getDatas().size());
            dto.getDatas().forEach(v->{
                UserSpaceRelVO vo = new UserSpaceRelVO();
                BeanUtils.copyProperties(v, vo);
                vo.setState(v.getState().getCode());
                if(v.getRole() != null) {
                    vo.setRole(v.getRole().getCode());
                }
                list.add(vo);
            });
            return new ResponseHeadVO<>(dto.isResult(),list,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),new ArrayList<>(),dto.getMessage());
    }
}
