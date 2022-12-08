package com.jflove.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author tanjun
 * @date 2022/12/6 17:11
 * @describe
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Log4j2
public class SecurityConfig{

    @Autowired
    @Qualifier("UserDetailsServiceImpl")
    private UserDetailsService uds;

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;//利用它将异常抛出到 controller 层,让 RestControllerAdvice 捕获到

    /**
     * jwt权限验证拦截器
     * @return
     */
    @Bean
    public OncePerRequestFilter jwtAuthenticationTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                if (!ignoreUrlsConfig.isSkip(request.getServletPath())){
                    try {
                        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
                        Assert.hasLength(token,"token不能为空");
                        log.info("authenticated token:{}", token);
                        UserDetails userDetails = uds.loadUserByUsername(token);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, (Object) null, userDetails.getAuthorities());
                        authentication.setDetails((new WebAuthenticationDetailsSource()).buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }catch (Exception e){
                        resolver.resolveException(request,response,null,e);
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .authorizeRequests()
                .antMatchers(ignoreUrlsConfig.getStrings()).permitAll() //无需认证
                .antMatchers("/**").authenticated()//进行验证
                .and()
                .formLogin().disable()
                .logout().disable()
                .addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class)//添加jwt方式登录认证
                .csrf().disable();//CSRF 防御要求表单登录时携带 CSRF Token，前后端分离时不需要开启
        return http.build();
    }
}
