package com.jflove.gateway.controller.share;

import com.jflove.ResponseHeadDTO;
import com.jflove.share.api.INoteShare;
import com.jflove.gateway.vo.ResponseHeadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "获取笔记分享内容")
    @GetMapping("/getBody/{uuid}")
    public ResponseHeadVO<String> getBody(
        @ApiParam("链接id") @PathVariable("uuid") String uuid,
        @ApiParam("解锁密码") @RequestParam("password") String password
    ){
        Assert.hasLength(uuid,"错误的请求:链接id不能为空");
        ResponseHeadDTO<String> dto = noteShare.getBody(uuid,password);
        return new ResponseHeadVO<>(dto.isResult(),dto.getData(),dto.getMessage());
    }
}
