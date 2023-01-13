package com.jflove.controller.admin;

import com.jflove.vo.ResponseHeadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: tanjun
 * @date: 2023/1/13 2:15 PM
 * @desc: api调用网络环境
 */
@RestController
@RequestMapping("/admin/network")
@Api(tags = "api调用网络环境")
public class ApiEnvironmentController {

    @ApiOperation(value = "获取服务端本地环境的ip和端口")
    @GetMapping("/getServiceLocalPath")
    public ResponseHeadVO<String> getServiceLocalPath(HttpServletRequest request, HttpServletResponse response){
        return new ResponseHeadVO<>("local="+request.getLocalAddr() + ",Remote="+request.getRemoteAddr());
    }
}
