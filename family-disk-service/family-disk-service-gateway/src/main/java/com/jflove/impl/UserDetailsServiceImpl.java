package com.jflove.impl;

import com.jflove.ResponseHeadDTO;
import com.jflove.tool.JJwtTool;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
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

    @Override
    public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
        //验证token
        Jws<Claims> jws = jJwtTool.parseJwt(token);
        Claims claims = jws.getBody();
        //token验证通过,返回用户信息
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.getUserInfoByEmail(claims.getId());
        Assert.notNull(dto.getData(),dto.getMessage());
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
