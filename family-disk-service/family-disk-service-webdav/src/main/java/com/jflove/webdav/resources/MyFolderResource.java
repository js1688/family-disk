package com.jflove.webdav.resources;

import com.jflove.webdav.vo.FolderVO;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.FolderResource;
import io.milton.resource.Resource;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author: tanjun
 * @date: 2023/9/15 10:22 AM
 * @desc:
 */
@Log4j2
public class MyFolderResource extends BaseResource implements FolderResource {

    private FolderVO folder;

    public MyFolderResource(FolderVO folder) {
        super(folder);
        this.folder = folder;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        return Optional.ofNullable(folder.getChildren()).orElse(new ArrayList<>());
    }

}
