package com.jflove.controller.user;

import com.jflove.tool.JJwtTool;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.user.EmailPasswordLoginParamVO;
import com.jflove.vo.user.GetUserInfoByEmailParamVO;
import com.jflove.vo.user.UserInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author tanjun
 * @date 2022/12/6 16:19
 * @describe 用户管理
 */
@RestController
@RequestMapping("/user/info")
@Api(tags = "用户管理")
public class UserInfoController {

    @DubboReference
    private IUserInfo userInfo;

    @Autowired
    private JJwtTool jJwtTool;

    @ApiOperation(value = "根据邮箱获取账号信息")
    @PostMapping("/getUserInfoByEmail")
    public ResponseHeadVO<UserInfoVO> getUserInfoByEmail(@RequestBody @Valid GetUserInfoByEmailParamVO param){
        UserInfoDTO dto = userInfo.getUserInfoByEmail(param.getEmail());
        if(dto != null){
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(dto,vo);
            return new ResponseHeadVO<UserInfoVO>(true,vo,"查询用户信息成功");
        }
        return new ResponseHeadVO<UserInfoVO>(false,"该用户不存在");
    }

    @ApiOperation(value = "邮箱+密码登录")
    @PostMapping("/emailPasswordLogin")
    public ResponseHeadVO<String> emailPasswordLogin(@RequestBody @Valid EmailPasswordLoginParamVO param) {
        UserInfoDTO dto = userInfo.emailPasswordLogin(param.getEmail(),param.getPassword());
        if(dto != null){
            String token = jJwtTool.createJwt(dto.getEmail(),dto.getName());
            return new ResponseHeadVO<String>(true,token,"邮箱+密码登录成功");
        }
        return new ResponseHeadVO<String>(false,"邮箱+密码登录失败,验证不通过");
    }
}
