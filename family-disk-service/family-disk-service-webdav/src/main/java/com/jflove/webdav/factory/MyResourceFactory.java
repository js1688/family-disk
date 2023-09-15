package com.jflove.webdav.factory;

import com.jflove.webdav.resources.MyFileResource;
import com.jflove.webdav.resources.MyFolderResource;
import com.jflove.webdav.vo.FileVO;
import com.jflove.webdav.vo.FolderVO;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/9/15 10:20 AM
 * @desc:
 */
@Log4j2
@Component("MyResourceFactory")
public class MyResourceFactory implements ResourceFactory {
    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
        log.debug("获取资源:host:{},s1:{}",host,url);
        //根目录
        if("/".equals(url)){
            List<Resource> c = Arrays.asList(new MyFolderResource(new FolderVO("兔兔比比","100")),new MyFolderResource(new FolderVO("丸子君","200")));
            return new MyFolderResource(new FolderVO(url,"0",c));
        }
        log.debug("原始的:{}",url);
        //取最后一位,用于判断是文件还是目录
        String last = url.substring(url.length()-1,url.length());
        boolean isFolder = "/".equals(last) ? true : false;
        if(isFolder){
            url = url.substring(0,url.length() - 1);
        }
        String name = url.substring(url.lastIndexOf("/")+1);
        log.debug("访问目录:{}",name);
        if(name.startsWith(".")){//过滤掉隐藏目录? 不知道为什么会请求
            log.debug("不存在的目录跳过?,后面可能有意义,暂时先跳过");
            return null;
        }
        //查找正常目录
        MyFileResource mfr = new MyFileResource(new FileVO("家庭网盘介绍视频.mp4","300","video/mp4",81049474l,"/Users/tanjun/Movies/家庭网盘介绍视频/家庭网盘介绍视频.mp4"));
        MyFileResource mfr2 = new MyFileResource(new FileVO("家庭网盘介绍视频.zip","400","application/zip",79646014l,"/Users/tanjun/Movies/家庭网盘介绍视频.zip"));
        if("兔兔比比".equals(name)){
            List<Resource> c = Arrays.asList(mfr,mfr2);
            return new MyFolderResource(new FolderVO(name,"100",c));
        }else if("家庭网盘介绍视频.mp4".equals(name)){
            return mfr;
        }else if("家庭网盘介绍视频.zip".equals(name)){
            return mfr2;
        }

        return new MyFolderResource(new FolderVO(url,"666"));
    }
}
