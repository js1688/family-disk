package com.jflove.webdav.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
     * 过滤掉文件
     */
    private List<String> files = new ArrayList();

    public String [] getUrlStrings(){
        if(files == null){
            return null;
        }
        return files.stream().toArray(String[]::new);
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
        if(files.contains(url)){
            return true;
        }
        String [] us = url.split("/");
        if(us == null || us.length == 0){
            return false;
        }
        for (String u:files) {
            if(!StringUtils.hasLength(u)){
                continue;
            }
            if ("*".equals(String.valueOf(u.charAt(u.length()-1)))) {//*结尾,模糊匹配
                if(us[us.length-1].startsWith(u.substring(0,u.length()-1))){
                    return true;
                }
            }
            if ("*".equals(String.valueOf(u.charAt(0)))){//*开头,模糊匹配
                if(us[us.length-1].endsWith(u.substring(1,u.length()))){
                    return true;
                }
            }
            if(us[us.length-1].equals(u)){
                return true;
            }
        }
        return false;
    }
}
