package com.jflove.webdav.resources;

import com.jflove.ResponseHeadDTO;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.webdav.factory.ManageFactory;
import com.jflove.webdav.vo.BaseVO;
import io.milton.common.Path;
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

    protected UserSpaceDTO userSpace;//正在使用的空间信息

    protected UserSpaceRoleENUM role;//空间权限

    private String url;//资源url


    public BaseResource(ManageFactory manageFactory,String url,UserSpaceDTO userSpace) {
        this.manageFactory = manageFactory;
        this.url = url;
        this.userSpace = userSpace;
    }

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
        if(userSpace == null){//没有空间信息
            return null;
        }
        ResponseHeadDTO<UserInfoDTO> userInfo = manageFactory.getUserInfoByEmail(s);
        if(!userInfo.isResult() || !userInfo.getData().getPassword().equals(s1)){//找不到用户
            return null;
        }
        //检查该用户是否拥有这个空间的权限
        Optional<UserSpaceRelDTO> spaceRel = userInfo.getData().getSpaces().stream().filter(e->e.getSpaceId() == userSpace.getId()).findFirst();
        if(!spaceRel.isPresent()){//用户对这个空间没权限
            //没有这个空间的权限
            return null;
        }
        role = spaceRel.get().getRole();
        user = userInfo.getData();
        log.debug("用户:{},身份验证成功,正在使用的空间id:{},url:{}",s,userSpace.getId(),url);
        return this;
    }


    
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        boolean result = auth != null && auth.getTag() != null;
        if(!result){
            log.error("auth:{}",auth.toString());
            log.error("路径:{},验证未通过",url);
        }
        return true;
    }

    
    public String getRealm() {
        if(userSpace == null){
            return null;
        }
        return userSpace.getCode();
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
        log.error("调用了child:{},url:{}",s,url);
        return null;
    }

    
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        return null;
    }

    
    public void copyTo(CollectionResource collectionResource, String s) throws NotAuthorizedException, BadRequestException, ConflictException {

    }

    
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        Resource r = (Resource) this;
        //检查是否对这个空间有写入权限
        if(role != UserSpaceRoleENUM.WRITE){
            throw new NotAuthorizedException("没有这个空间的写入权限",r);
        }
        ResponseHeadDTO<Integer> result = manageFactory.getNetdiskDirectory().delDirectory(userSpace.getId(),base.getId());
        if(!result.isResult()){
            throw new BadRequestException(r,"删除目录失败");
        }
    }

    
    public String processForm(Map<String, String> map, Map<String, FileItem> map1) throws BadRequestException, NotAuthorizedException, ConflictException {
        return null;
    }


    public void sendContent(OutputStream outputStream, Range range, Map<String, String> map, String s) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {

    }

    
    public CollectionResource createCollection(String s) throws NotAuthorizedException, ConflictException, BadRequestException {
        return null;
    }

    
    public void moveTo(CollectionResource collectionResource, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        Resource r = (Resource) this;
        //检查是否对这个空间有写入权限
        if(role != UserSpaceRoleENUM.WRITE){
            throw new NotAuthorizedException("没有这个空间的写入权限",r);
        }
        MyFolderResource p = (MyFolderResource) collectionResource;
        //这里要区分是移动到其它目录,还是修改目录名称, 可以通过方法参数中的名称判断,如果名称与url中的名称不一致则代表只是修改名称
        if(Path.path(url).getName().equals(name)){
            manageFactory.getNetdiskDirectory().moveDirectory(userSpace.getId(),base.getId(),p.getBase().getId());
        }else{
            manageFactory.getNetdiskDirectory().updateName(userSpace.getId(),base.getId(),name);
        }
    }

    public Resource createNew(String s, InputStream inputStream, Long aLong, String s1) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        return null;
    }
}
