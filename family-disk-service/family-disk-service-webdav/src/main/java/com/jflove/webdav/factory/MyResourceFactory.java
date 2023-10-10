package com.jflove.webdav.factory;

import com.jflove.ResponseHeadDTO;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.user.dto.UserSpaceDTO;
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
        log.debug("访问资源:host:{},s1:{}",host,url);
        if(ignoreUrlsConfig.isSkip(url)){//需要跳过的资源路径
            log.debug("跳过的路径:{}",url);
            return null;
        }
        if("/".equals(url)){//根目录
            return new MyFolderResource(url,manageFactory,null);
        }
        //url的第一位必须是空间编码
        String [] sp = url.substring(1).split("/");
        String first = sp[0];//第一个位置一定是空间编码
        sp[0] = "/";
        url = String.join("/",sp);//去掉空间编码后的url
        url = url.replaceAll("//","/");
        log.debug("去掉空间编码后的资源:{}",url);
        //在查询空间编码信息,和判断空间下的目录是否存在时,不涉及到授权验证问题,主要的目的是用于判断资源是否存在,因为在访问资源时会验证授权
        //通过空间编码得到空间ID
        ResponseHeadDTO<UserSpaceDTO> userSpace = manageFactory.getSpaceByCode(first);
        UserSpaceDTO dto = userSpace.getData();
        if(dto != null){//空间编码存在,判断本次要访问的目录是否存在
            ResponseHeadDTO<NetdiskDirectoryDTO> nd = manageFactory.getDirectoryByUrl(dto.getId(),url);
            if(!nd.isResult()){//不存在这个目录
                return null;
            }
            if (nd.getData().getType() == NetdiskDirectoryENUM.FOLDER){
                return new MyFolderResource(url,manageFactory,dto);
            }else if(nd.getData().getType() == NetdiskDirectoryENUM.FILE){
                return new MyFileResource(url,manageFactory,dto);
            }
        }
        return new MyFolderResource(url,manageFactory,null);
    }
}
