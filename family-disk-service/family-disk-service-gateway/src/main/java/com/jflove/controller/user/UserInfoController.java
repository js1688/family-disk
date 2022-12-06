package com.jflove.controller.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tanjun
 * @date 2022/12/6 16:19
 * @describe 用户管理
 */
@RestController
@RequestMapping("/user/info")
@Api(tags = "用户管理")
public class UserInfoController {

    @ApiOperation(value = "单个商品详情")
    @GetMapping("/findGoodsById")
    public String findGoodsById(
            @ApiParam(name = "商品ID,正整数")
            @RequestParam(value = "goodsId", required = false, defaultValue = "0") Integer goodsId) {

        return "啊类";
    }
}
