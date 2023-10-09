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

        if("/".equals(url)){//根目录
            return new MyFolderResource(url,manageFactory);
        }
        //暂时判断路径如果不带后缀,或者最后一位是/就算是文件夹,目前没有好办法判断,在此时拿不到用户身份以及所属空间无法从数据库中判断目录
        String [] sp = url.split("/");
        String last = sp[sp.length -1];
        if("新建的".equals(last)){
            return null;
        }
        if("/".equals(last)){
            return new MyFolderResource(url,manageFactory);
        }else if(!last.contains(".")){
            return new MyFolderResource(url,manageFactory);
        }else{
            return new MyFileResource(url,manageFactory);
        }
    }
}
