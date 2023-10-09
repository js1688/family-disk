package com.jflove.webdav.resources;

import com.jflove.ResponseHeadDTO;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserRelStateENUM;
import com.jflove.webdav.factory.ManageFactory;
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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author: tanjun
 * @date: 2023/9/15 2:41 PM
 * @desc: 基础的资源实现,避免实现接口重复写代码,子类可以覆盖实现真实的方法
 */
@Log4j2
@Getter
@Setter
public abstract class BaseResource {

    private BaseVO base;

    private ManageFactory manageFactory;

    protected UserInfoDTO user;//只要验证通过就会存储用户信息

    protected Long realm;

    private String url;//资源url


    public BaseResource(ManageFactory manageFactory,String url) {
        this.manageFactory = manageFactory;
        this.url = url;
    }

    /**
     * 在验证身份后初始化资源属性对象
     * @return
     */
    public abstract BaseVO initBase();


    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    
    public String getContentType(String s) {
        if(base == null){
            return null;
        }
        return base.getContentType();
    }

    
    public Long getContentLength() {
        if(base == null){
            return null;
        }
        return base.getContentLength();
    }

    
    public Date getCreateDate() {
        if(base == null){
            return null;
        }
        return base.getCreateDate();
    }

    
    public String getUniqueId() {
        if(base == null){
            return null;
        }
        return String.valueOf(base.getId());
    }

    
    public String getName() {
        if(base == null){
            return null;
        }
        return base.getName();
    }

    
    public Object authenticate(String s, String s1) {
        ResponseHeadDTO<UserInfoDTO> userInfo = manageFactory.getUserInfoByEmail(s);
        if(!userInfo.isResult() || !userInfo.getData().getPassword().equals(s1)){
            return null;
        }
        Optional<UserSpaceRelDTO> spaceRel = userInfo.getData().getSpaces().stream().filter(e->e.getState() == UserRelStateENUM.USE).findFirst();
        if(!spaceRel.isPresent()){
            return null;
        }
        realm = spaceRel.get().getSpaceId();
        log.debug("用户:{},身份验证成功,正在使用的空间id:{}",s,realm);
        user = userInfo.getData();
        if(StringUtils.hasLength(url)) {
            base = initBase();
        }
        return this;
    }


    
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return auth != null && auth.getTag() != null;
    }

    
    public String getRealm() {
        if(realm == null){
            return null;
        }
        return String.valueOf(realm);
    }

    
    public Date getModifiedDate() {
        if(base == null){
            return null;
        }
        return base.getModifiedDate();
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
