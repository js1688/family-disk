package com.jflove.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tanjun
 * @date 2022/12/8 9:49
 * @describe 路径忽略配置
 */
@ConfigurationProperties(
        prefix = "spring.security.ignored"
)
@Component
@Getter
@Setter
@ToString
public class IgnoreUrlsConfig {
    /**
     * 白名单请求地址
     */
    private List<String> urls = new ArrayList();

    /**
     * 需要admin角色权限可以访问的地址
     */
    private List<String> admin = new ArrayList<>();
    /**
     * 跨域允许访问地址
     */
    private List<String> origins = new ArrayList<>();

    public String [] getOriginsStrings(){
        if(origins == null){
            return null;
        }
        String [] a = new String[origins.size()];
        origins.toArray(a);
        return a;
    }

    public String [] getUrlStrings(){
        if(urls == null){
            return null;
        }
        String [] a = new String[urls.size()];
        urls.toArray(a);
        return a;
    }

    public String [] getAdminStrings(){
        if(admin == null){
            return null;
        }
        String [] a = new String[admin.size()];
        admin.toArray(a);
        return a;
    }

    /**
     * 路径是否需要跳过
     * @param url
     * @return
     */
    public boolean isSkip(String url){
        if(url == null){
            return false;
        }
        if(urls.contains(url)){
            return true;
        }
        String [] us = url.split("/");
        if(us == null || us.length == 0){
            return false;
        }
        A:for (String u:urls) {
            String [] uss = u.split("/");
            if(us.length >= uss.length){//用户访问地址长于或等于配置地址才需要比较,否则肯定是不匹配的
                for (int i = 0; i < uss.length; i++) {
                    String config = uss[i];
                    String request = us[i];
                    if(!"**".equals(config) && !config.equals(request)){//如果配置的不是所有并且配置与请求匹配不通过,跳过这个配置url的匹配
                        continue A;
                    }
                }
            }else{
                continue;
            }
            return true;
        }
        return false;
    }
}
