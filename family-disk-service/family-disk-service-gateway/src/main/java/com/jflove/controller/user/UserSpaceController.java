package com.jflove.controller.user;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.user.*;
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
        Assert.notNull(useSpaceId,"错误的请求:空间ID不能为空");
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
        Assert.notNull(useUserId,"错误的请求:用户ID不能为空");
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
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        Assert.notNull(useUserId,"错误的请求:用户ID不能为空");
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Assert.notNull(useSpaceId,"错误的请求:空间ID不能为空");
        ResponseHeadDTO dto = userSpace.switchSpace(param.getTargetSpaceId(),useSpaceId,useUserId);
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "申请加入空间")
    @PostMapping("/joinSpace")
    public ResponseHeadVO joinSpace(@RequestBody @Valid JoinSpaceParamVO param){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        Assert.notNull(useUserId,"错误的请求:用户ID不能为空");
        ResponseHeadDTO dto = userSpace.joinSpace(param.getTargetSpaceCode(),useUserId);
        return new ResponseHeadVO(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "查找用户创建的空间下有多少关联用户")
    @GetMapping("/getUserInfoBySpaceId")
    public ResponseHeadVO<UserSpaceRelVO> getUserInfoBySpaceId(){
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        Assert.notNull(useUserId,"错误的请求:用户ID不能为空");
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Assert.notNull(useSpaceId,"错误的请求:空间ID不能为空");

        ResponseHeadDTO<UserSpaceRelDTO> dto = userSpace.getUserInfoBySpaceId(useSpaceId,useUserId);
        if(dto.isResult()){
            List<UserSpaceRelVO> list = new ArrayList<>(dto.getDatas().size());
            dto.getDatas().forEach(v->{
                UserSpaceRelVO vo = new UserSpaceRelVO();
                BeanUtils.copyProperties(v, vo);
                vo.setState(v.getState().getCode());
                list.add(vo);
            });
            return new ResponseHeadVO<>(dto.isResult(),list,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),new ArrayList<>(),dto.getMessage());
    }
}
