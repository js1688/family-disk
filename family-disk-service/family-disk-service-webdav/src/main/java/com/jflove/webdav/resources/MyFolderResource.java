package com.jflove.webdav.resources;

import com.jflove.ResponseHeadDTO;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.user.em.UserRelStateENUM;
import com.jflove.webdav.factory.ManageFactory;
import com.jflove.webdav.vo.BaseVO;
import com.jflove.webdav.vo.FileVO;
import com.jflove.webdav.vo.FolderVO;
import io.milton.http.Auth;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.FolderResource;
import io.milton.resource.Resource;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/9/15 10:22 AM
 * @desc:
 */
@Log4j2
public class MyFolderResource extends BaseResource implements FolderResource {

    private FolderVO folder;
    private ManageFactory manageFactory;




    public MyFolderResource(String url, ManageFactory manageFactory){
        super(manageFactory,url);
        this.manageFactory = manageFactory;
    }


    public MyFolderResource(FolderVO folder,ManageFactory manageFactory) {
        super(manageFactory,null);
        this.folder = folder;
        super.setBase(folder);
        this.manageFactory = manageFactory;
    }

    @Override
    public BaseVO initBase() {
        //通过url识别出目录信息
        long spaceId = super.getUser().getSpaces().stream().filter(e->e.getState() == UserRelStateENUM.USE).findFirst().get().getSpaceId();
        ResponseHeadDTO<NetdiskDirectoryDTO> urlLast = manageFactory.getDirectoryByUrl(spaceId,super.getUrl());
        if(!urlLast.isResult()){
            return null;
        }
        NetdiskDirectoryDTO v = urlLast.getData();
        if(NetdiskDirectoryENUM.FOLDER == v.getType()){
            this.folder = new FolderVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime());
        }else if(NetdiskDirectoryENUM.FILE == v.getType()){
            this.folder = new FolderVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime(),v.getMediaType(),v.getSizeB());
        }
        return this.folder;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        //查询子目录
        ResponseHeadDTO<NetdiskDirectoryDTO> children = manageFactory.getChildren(super.getUser(),folder);
        if(!children.isResult()){
            return new ArrayList<>();
        }
        List<Resource> list = new ArrayList<>(children.getDatas().size());
        children.getDatas().forEach(v->{
            if(NetdiskDirectoryENUM.FOLDER == v.getType()){
                list.add(new MyFolderResource(new FolderVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime()),manageFactory));
            }else if(NetdiskDirectoryENUM.FILE == v.getType()){
                list.add(new MyFileResource(new FileVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime(),v.getMediaType(),v.getSizeB(),v.getFileMd5()),manageFactory));
            }
        });
        return list;
    }


    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return 60l;
    }

    @Override
    public Resource createNew(String s, InputStream inputStream, Long aLong, String s1) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        return super.createNew(s, inputStream, aLong, s1);
    }
}
