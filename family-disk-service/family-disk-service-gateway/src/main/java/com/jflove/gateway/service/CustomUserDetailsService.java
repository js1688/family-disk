package com.jflove.gateway.service;

import com.jflove.ResponseHeadDTO;
import com.jflove.gateway.config.HttpConstantConfig;
import com.jflove.gateway.tool.JJwtTool;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserRelStateENUM;
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
import java.util.Optional;

/**
 * @author tanjun
 * @date 2022/12/7 17:39
 * @describe 用户权限确定
 */
@Component("CustomUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {
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
        if(!StringUtils.hasLength(claims.getId())){
            throw new UsernameNotFoundException("token不正确");
        }
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.getUserInfoByEmail(claims.getId());
        Assert.notNull(dto.getData(),dto.getMessage());
        if(dto.getData().getSpaces() != null){//获取用户所关联的空间,然后找到正在使用的空间
            Optional<UserSpaceRelDTO> rel = dto.getData().getSpaces().stream()
                    .filter(e->e.getState() == UserRelStateENUM.USE).findFirst();
            if(!rel.isEmpty()){//正在使用的空间不为空,才设置这两个属性
                autowiredRequest.setAttribute(HttpConstantConfig.USE_SPACE_ROLE,rel.get().getRole());
                autowiredRequest.setAttribute(HttpConstantConfig.USE_SPACE_ID,rel.get().getSpaceId());
            }
        }
        autowiredRequest.setAttribute(HttpConstantConfig.USE_USER_ID,dto.getData().getId());
        autowiredRequest.setAttribute(HttpConstantConfig.USE_USER_EMAIL,dto.getData().getEmail());
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_" + dto.getData().getRole().getCode()));
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
