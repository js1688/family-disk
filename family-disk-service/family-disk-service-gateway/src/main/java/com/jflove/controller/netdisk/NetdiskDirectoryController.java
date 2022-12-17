package com.jflove.controller.netdisk;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.netdisk.api.INetdiskDirectory;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.netdisk.AddDirectoryParamVO;
import com.jflove.vo.netdisk.DelDirectoryParamVO;
import com.jflove.vo.netdisk.MoveDirectoryParamVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author tanjun
 * @date 2022/12/16 15:26
 * @describe
 */
@RestController
@RequestMapping("/netdisk")
@Api(tags = "网盘目录")
@Log4j2
public class NetdiskDirectoryController {

    @DubboReference
    private INetdiskDirectory netdiskDirectory;

    @Autowired
    private HttpServletRequest autowiredRequest;

    @ApiOperation(value = "添加目录")
    @PostMapping("/addDirectory")
    public ResponseHeadVO<AddDirectoryParamVO> addDirectory(@RequestBody @Valid AddDirectoryParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有添加权限");
        }
        NetdiskDirectoryDTO dto = new NetdiskDirectoryDTO();
        BeanUtils.copyProperties(param,dto);
        dto.setSpaceId(useSpaceId);
        dto.setType(NetdiskDirectoryENUM.valueOf(param.getType()));
        ResponseHeadDTO<NetdiskDirectoryDTO> retDto = netdiskDirectory.addDirectory(dto);
        ResponseHeadVO<AddDirectoryParamVO> retVo = new ResponseHeadVO<AddDirectoryParamVO>();
        BeanUtils.copyProperties(retDto,retVo);
        return retVo;
    }

    @ApiOperation(value = "删除目录")
    @PostMapping("/delDirectory")
    public ResponseHeadVO<Integer> delDirectory(@RequestBody @Valid DelDirectoryParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有删除权限");
        }
        ResponseHeadDTO<Integer> dto = netdiskDirectory.delDirectory(useSpaceId,param.getId());
        ResponseHeadVO<Integer> retVo = new ResponseHeadVO<Integer>();
        BeanUtils.copyProperties(dto,retVo);
        return retVo;
    }

    @ApiOperation(value = "移动目录")
    @PostMapping("/moveDirectory")
    public ResponseHeadVO<AddDirectoryParamVO> moveDirectory(@RequestBody @Valid MoveDirectoryParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有移动权限");
        }
        ResponseHeadDTO<NetdiskDirectoryDTO> dto = netdiskDirectory.moveDirectory(useSpaceId,param.getId(),param.getTargetDirId());
        ResponseHeadVO<AddDirectoryParamVO> retVo = new ResponseHeadVO<AddDirectoryParamVO>();
        BeanUtils.copyProperties(dto,retVo);
        return retVo;
    }
}