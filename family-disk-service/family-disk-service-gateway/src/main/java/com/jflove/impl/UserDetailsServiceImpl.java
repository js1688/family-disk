package com.jflove.impl;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.tool.JJwtTool;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserRelStateENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

/**
 * @author tanjun
 * @date 2022/12/7 17:39
 * @describe 用户权限确定
 */
@Component("UserDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private JJwtTool jJwtTool;
    @DubboReference
    private IUserInfo userInfo;

    @Autowired
    private HttpServletRequest autowiredRequest;

    @Override
    public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
        //验证token
        Jws<Claims> jws = jJwtTool.parseJwt(token);
        Claims claims = jws.getBody();
        //token验证通过,返回用户信息
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.getUserInfoByEmail(claims.getId());
        Assert.notNull(dto.getData(),dto.getMessage());
        String useSpaceId =  autowiredRequest.getHeader(HttpConstantConfig.USE_SPACE_ID);
        //如果头部信息中包含了当前使用的空间ID则判断是否有权限
        if(StringUtils.hasLength(useSpaceId) && dto.getData().getSpaces() != null){
            if(!dto.getData().getSpaces().stream()
                    .filter(e->e.getState() == UserRelStateENUM.USE)
                    .map(UserSpaceRelDTO::getSpaceId)
                    .map(String::valueOf).toList().contains(useSpaceId)){
                throw new SecurityException("该用户没有使用当前空间的权限");
            }
            UserSpaceRoleENUM em = dto.getData().getSpaces().stream().filter(e->String.valueOf(e.getSpaceId()).equals(useSpaceId)).toList().get(0).getRole();
            autowiredRequest.setAttribute(HttpConstantConfig.USE_SPACE_ROLE,em);
            autowiredRequest.setAttribute(HttpConstantConfig.USE_SPACE_ID,Long.parseLong(useSpaceId));
        }
        autowiredRequest.setAttribute(HttpConstantConfig.USE_USER_ID,dto.getData().getId());
        autowiredRequest.setAttribute(HttpConstantConfig.USE_USER_EMAIL,dto.getData().getEmail());
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_" + dto.getData().getRole()));
            }

            @Override
            public String getPassword() {
                return dto.getData().getPassword();
            }

            @Override
            public String getUsername() {
                return dto.getData().getEmail();
            }

            @Override
            public boolean isAccountNonExpired() {
                //是账号未过期
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                //为非锁定帐户
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                //是证书未过期
                return true;
            }

            @Override
            public boolean isEnabled() {
                //是启用了
                return true;
            }
        };
    }
}
