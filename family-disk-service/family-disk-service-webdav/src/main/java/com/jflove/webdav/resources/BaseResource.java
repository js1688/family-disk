package com.jflove.webdav.resources;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.jflove.webdav.vo.BaseVO;
import io.milton.http.Auth;
import io.milton.http.FileItem;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/9/15 2:41 PM
 * @desc: 基础的资源实现,避免实现接口重复写代码,子类可以覆盖实现真实的方法
 */
@Log4j2
public class BaseResource {

    private BaseVO base;

    public BaseResource(BaseVO base) {
        this.base = base;
    }

    
    public Long getMaxAgeSeconds(Auth auth) {
        return 60l;
    }

    
    public String getContentType(String s) {
        return base.getContentType();
    }

    
    public Long getContentLength() {
        return base.getContentLength();
    }

    
    public Date getCreateDate() {
        return DateUtil.parse("2022-09-13 14:17:42", DatePattern.NORM_DATETIME_MINUTE_FORMAT);
    }

    
    public String getUniqueId() {
        return base.getId();
    }

    
    public String getName() {
        return base.getName();
    }

    
    public Object authenticate(String s, String s1) {
        return null;
    }

    
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return true;
    }

    
    public String getRealm() {
        return null;
    }

    
    public Date getModifiedDate() {
        return DateUtil.parse("2023-08-13 14:17:42", DatePattern.NORM_DATETIME_MINUTE_FORMAT);
    }

    
    public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
        return null;
    }


    
    public Resource child(String s) throws NotAuthorizedException, BadRequestException {
        return null;
    }

    
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        return null;
    }

    
    public void copyTo(CollectionResource collectionResource, String s) throws NotAuthorizedException, BadRequestException, ConflictException {

    }

    
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {

    }

    
    public String processForm(Map<String, String> map, Map<String, FileItem> map1) throws BadRequestException, NotAuthorizedException, ConflictException {
        return null;
    }

    
    public void sendContent(OutputStream outputStream, Range range, Map<String, String> map, String s) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {

    }

    
    public CollectionResource createCollection(String s) throws NotAuthorizedException, ConflictException, BadRequestException {
        return null;
    }

    
    public void moveTo(CollectionResource collectionResource, String s) throws ConflictException, NotAuthorizedException, BadRequestException {

    }

    
    public Resource createNew(String s, InputStream inputStream, Long aLong, String s1) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        return null;
    }
}
