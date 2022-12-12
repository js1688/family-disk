package com.jflove.controller.admin;

import com.jflove.ResponseHeadDTO;
import com.jflove.admin.api.IDiskService;
import com.jflove.admin.em.FileDiskTypeENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.admin.AddDiskParamVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author tanjun
 * @date 2022/12/12 11:38
 * @describe 磁盘管理
 */
@RestController
@RequestMapping("/disk/manage")
@Api(tags = "磁盘管理")
public class DiskController {
    @DubboReference
    private IDiskService diskService;

    @ApiOperation(value = "添加一个存储磁盘位置")
    @PostMapping("/addDisk")
    public ResponseHeadVO<Long> addDisk(@RequestBody @Valid AddDiskParamVO param){
        ResponseHeadDTO<Long> dto = diskService.addDisk(FileDiskTypeENUM.valueOf(param.getType()),param.getPath());
        ResponseHeadVO<Long> vo = new ResponseHeadVO<>();
        BeanUtils.copyProperties(dto,vo);
        return vo;
    }
}
