package com.jflove.webdav.config;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.ResourceFactory;
import io.milton.servlet.SpringMiltonFilter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: tanjun
 * @date: 2023/9/12 2:15 PM
 * @desc:
 */
@Configuration
@Log4j2
public class WebDavConfig {

    @Bean("springMiltonFilter")
    public FilterRegistrationBean springMiltonFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SpringMiltonFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }

    @Bean("milton.http.manager")
    public HttpManagerBuilder httpManagerBuilder(@Autowired @Qualifier("MyResourceFactory") ResourceFactory resourceFactory){
        HttpManagerBuilder builder = new HttpManagerBuilder();
        builder.setResourceFactory(resourceFactory);
        builder.setEnableOptionsAuth(true);//开启验证,内置了多种收集认证的处理器,会自己识别不同的认证方式,其中就包含了账号与密码验证方式 BasicAuth
        return builder;
    }
}
