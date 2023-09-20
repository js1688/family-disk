package com.jflove.webdav.config;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.ResourceFactory;
import io.milton.http.fs.SimpleSecurityManager;
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

    @Bean("miltonFilter")
    public FilterRegistrationBean webDavFilterRegistration() {
        final FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SpringMiltonFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean("milton.http.manager")
    public HttpManagerBuilder httpManagerBuilder(@Autowired @Qualifier("MyResourceFactory") ResourceFactory resourceFactory){
        HttpManagerBuilder builder = new HttpManagerBuilder();
        builder.setResourceFactory(resourceFactory);
        builder.setSecurityManager(new SimpleSecurityManager());//todo 没用?
        return builder;
    }
}
