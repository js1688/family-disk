package com.jflove.gateway.controller.user;

import com.jflove.ResponseHeadDTO;
import com.jflove.gateway.config.HttpConstantConfig;
import com.jflove.gateway.tool.JJwtTool;
import com.jflove.user.api.IUserEmail;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.gateway.vo.ResponseHeadVO;
import com.jflove.gateway.vo.user.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

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
    private HttpServletRequest autowiredRequest;
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

    @ApiOperation(value = "获取当前登录账号信息")
    @GetMapping("/getUserInfo")
    public ResponseHeadVO<UserInfoVO> getUserInfo(){
        String useUserEmail = (String)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_EMAIL);
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.getUserInfoByEmail(useUserEmail);
        if(dto.isResult()){
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(dto.getData(),vo);
            if(dto.getData().getSpaces() != null) {
                List<UserSpaceRelVO> usrList = new ArrayList<>(dto.getData().getSpaces().size());
                dto.getData().getSpaces().forEach(v->{
                    UserSpaceRelVO usr = new UserSpaceRelVO();
                    BeanUtils.copyProperties(v, usr);
                    usr.setRole(v.getRole().getCode());
                    usr.setState(v.getState().getCode());
                    usrList.add(usr);
                });
                vo.setSpaces(usrList);
            }
            return new ResponseHeadVO<>(dto.isResult(),vo,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
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
        if(dto.isResult()){
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(dto.getData(),vo);
            return new ResponseHeadVO<>(dto.isResult(),vo,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }
}
