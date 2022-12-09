package com.jflove.controller.user;

import com.jflove.ResponseHeadDTO;
import com.jflove.tool.JJwtTool;
import com.jflove.user.api.IUserEmail;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.user.CreateUserInfoParamVO;
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
    @DubboReference
    private IUserEmail userEmail;

    @Autowired
    private JJwtTool jJwtTool;

    @ApiOperation(value = "注册时发送邮箱验证码")
    @PostMapping("/sendRegisterEmailCaptcha")
    public ResponseHeadVO<String> sendRegisterEmailCaptcha(@RequestBody @Valid GetUserInfoByEmailParamVO param){
        ResponseHeadDTO<String> dto = userEmail.sendRegisterEmailCaptcha(param.getEmail());
        ResponseHeadVO<String> vo = new ResponseHeadVO<>();
        BeanUtils.copyProperties(dto,vo);
        return vo;
    }

    @ApiOperation(value = "根据邮箱获取账号信息")
    @PostMapping("/getUserInfoByEmail")
    public ResponseHeadVO<UserInfoVO> getUserInfoByEmail(@RequestBody @Valid GetUserInfoByEmailParamVO param){
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.getUserInfoByEmail(param.getEmail());
        ResponseHeadVO<UserInfoVO> vo = new ResponseHeadVO<>();
        BeanUtils.copyProperties(dto,vo);
        return vo;
    }

    @ApiOperation(value = "邮箱+密码登录")
    @PostMapping("/emailPasswordLogin")
    public ResponseHeadVO<String> emailPasswordLogin(@RequestBody @Valid EmailPasswordLoginParamVO param) {
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.emailPasswordLogin(param.getEmail(),param.getPassword());
        if(dto.isResult()){
            String token = jJwtTool.createJwt(dto.getData().getEmail(),dto.getData().getName());
            return new ResponseHeadVO<String>(true,token,"邮箱+密码登录成功");
        }
        return new ResponseHeadVO<String>(false,"邮箱+密码登录失败,验证不通过");
    }

    @ApiOperation(value = "创建账号")
    @PostMapping("/createUserInfo")
    public ResponseHeadVO<UserInfoVO> createUserInfo(@RequestBody @Valid CreateUserInfoParamVO param){
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.createUserInfo(param.getEmail(),param.getPassword(),param.getName(),param.getCaptcha());
        ResponseHeadVO<UserInfoVO> vo = new ResponseHeadVO<>();
        BeanUtils.copyProperties(dto,vo);
        return vo;
    }
}
