package com.jflove.webdav.factory;

import com.jflove.webdav.config.IgnoreUrlsConfig;
import com.jflove.webdav.resources.MyFileResource;
import com.jflove.webdav.resources.MyFolderResource;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: tanjun
 * @date: 2023/9/15 10:20 AM
 * @desc:
 */
@Log4j2
@Component("MyResourceFactory")
public class MyResourceFactory implements ResourceFactory {

    @Autowired
    private ManageFactory manageFactory;

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
        log.debug("获取资源:host:{},s1:{}",host,url);
        if(ignoreUrlsConfig.isSkip(url)){//需要跳过的资源路径
            log.debug("跳过的路径:{}",url);
            return null;
        }
        if("/".equals(url.substring(url.length()-1,url.length()))){
            return new MyFolderResource(url,manageFactory);
        }else{
            return new MyFileResource(url,manageFactory);
        }
    }
}
