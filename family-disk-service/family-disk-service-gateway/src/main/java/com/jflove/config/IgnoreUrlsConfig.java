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
    private List<String> urls = new ArrayList();

    public String [] getStrings(){
        if(urls == null){
            return null;
        }
        String [] a = new String[urls.size()];
        urls.toArray(a);
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
            if(us.length >= uss.length){
                for (int i = 0; i < uss.length; i++) {
                    String config = uss[i];
                    String request = us[i];
                    if(!"**".equals(config) && !config.equals(request)){//如果配置的不是所有并且配置与请求匹配不通过,跳过这个配置url的匹配
                        continue A;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
