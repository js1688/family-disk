package com.jflove.webdav.resources;

import com.jflove.ResponseHeadDTO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.stream.dto.StreamWriteParamDTO;
import com.jflove.stream.dto.StreamWriteResultDTO;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.webdav.factory.ManageFactory;
import com.jflove.webdav.vo.FileVO;
import com.jflove.webdav.vo.FolderVO;
import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.FolderResource;
import io.milton.resource.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.CoyoteInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author: tanjun
 * @date: 2023/9/15 10:22 AM
 * @desc:
 */
@Log4j2
public class MyFolderResource extends BaseResource implements FolderResource {

    private FolderVO folder;
    private ManageFactory manageFactory;

    public MyFolderResource(String url,FolderVO folder,ManageFactory manageFactory,UserSpaceDTO userSpace) {
        super(manageFactory,url,userSpace);
        this.folder = folder;
        super.setBase(folder);
        this.manageFactory = manageFactory;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        //查询子目录
        ResponseHeadDTO<NetdiskDirectoryDTO> children = manageFactory.getChildren(super.userSpace.getId(),folder);
        if(!children.isResult()){
            return new ArrayList<>();
        }
        List<Resource> list = new ArrayList<>(children.getDatas().size());
        children.getDatas().forEach(v->{
            if(NetdiskDirectoryENUM.FOLDER == v.getType()){
                list.add(new MyFolderResource(getUrl(),new FolderVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime()),manageFactory,super.getUserSpace()));
            }else if(NetdiskDirectoryENUM.FILE == v.getType()){
                list.add(new MyFileResource(getUrl(),new FileVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime(),v.getMediaType(),v.getSizeB(),v.getFileMd5()),manageFactory,super.getUserSpace()));
            }
        });
        return list;
    }


    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return 60l;
    }


    @Override
    public CollectionResource createCollection(String name) throws NotAuthorizedException, ConflictException, BadRequestException {
        //检查是否对这个空间有写入权限
        if(super.role != UserSpaceRoleENUM.WRITE){
            throw new NotAuthorizedException("没有这个空间的写入权限",this);
        }
        NetdiskDirectoryDTO dto = new NetdiskDirectoryDTO();
        dto.setType(NetdiskDirectoryENUM.FOLDER);
        dto.setSpaceId(userSpace.getId());
        ResponseHeadDTO<NetdiskDirectoryDTO> urlLast = manageFactory.getDirectoryByUrl(dto.getSpaceId(),super.getUrl());
        if(!urlLast.isResult()){
            throw new NotAuthorizedException("找不到父级目录",this);
        }
        dto.setPid(urlLast.getData().getId());
        dto.setName(name);
        ResponseHeadDTO<NetdiskDirectoryDTO> result = manageFactory.getNetdiskDirectory().addDirectory(dto);
        if(!result.isResult()){
            throw new BadRequestException(this,"创建文件夹失败");
        }
        return new MyFolderResource(getUrl(),new FolderVO(result.getData().getName(),result.getData().getId(),result.getData().getCreateTime(),result.getData().getUpdateTime())
                ,manageFactory,super.getUserSpace());
    }

    /**
     * 注意:如果使用了nginx代理服务,一定要注意如下配置
     * client_max_body_size 0;#不检查文件流大小
     * proxy_request_buffering off;#禁用客户端请求体缓冲
     * @param name - the name to create within the collection. E.g. myFile.txt
     * @param inputStream - the data to populate the resource with
     * @param along - 长度参数不可靠,不使用它
     * @param s - 媒体类型参数不可靠,不使用它
     * is the responsibility of the application to create a resource which also represents those content types, or a subset
     * @return
     * @throws IOException
     * @throws ConflictException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    @Override
    public Resource createNew(String name, InputStream inputStream, Long along, String s) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        //检查是否对这个空间有写入权限
        Request request = HttpManager.request();
        BaseResource parent = (BaseResource) request.getAuthorization().getTag();//这个是父目录,直接从父目录对象中拿到权限与身份信息即可
        if(parent.getRole() != UserSpaceRoleENUM.WRITE){
            throw new NotAuthorizedException("没有这个空间的写入权限",this);
        }
        ResponseHeadDTO<NetdiskDirectoryDTO> urlLast = manageFactory.getDirectoryByUrl(userSpace.getId(),super.getUrl());
        if(!urlLast.isResult()){
            throw new NotAuthorizedException("找不到父级目录",this);
        }
        CoyoteInputStream in = (CoyoteInputStream)inputStream;
        String type = name.lastIndexOf(".") != -1 ? name.substring(name.lastIndexOf(".")) : "";
        String fileMd5 = UUID.randomUUID().toString();//webdav上传的方式导致了它不能正确使用分片计算md5,同时这种上传方式也不能从垃圾箱回收,或者引用其它人上传的资源达到秒传,因为它的md5是随机的
        //预占目录,防止重复发送文件,以及文件流传输结束后,客户端立即请求查询当前目录结果却404这个问题
        NetdiskDirectoryDTO netDto = new NetdiskDirectoryDTO();
        netDto.setType(NetdiskDirectoryENUM.FILE);
        netDto.setSpaceId(userSpace.getId());
        netDto.setName(name);
        netDto.setPid(urlLast.getData().getId());
        netDto.setFileMd5(fileMd5);
        ResponseHeadDTO<NetdiskDirectoryDTO> directory = manageFactory.getNetdiskDirectory().addDirectory(netDto);
        if(!directory.isResult()){
            throw new BadRequestException(this,"建立文件与目录关系失败");
        }
        try {
            long seek = 0;
            while (!in.isFinished()) {
                byte[] b = new byte[8192];
                int rlen = in.read(b);
                if (rlen < b.length) {
                    b = Arrays.copyOf(b, rlen);
                }
                StreamWriteParamDTO swpd = new StreamWriteParamDTO();
                swpd.setOriginalFileName(name);
                swpd.setSource(FileSourceENUM.CLOUDDISK);
                swpd.setSpaceId(userSpace.getId());
                swpd.setTotalSize(Long.MAX_VALUE);//先假定总长度是long的最大值,这样传输时对方不会判断分片是否结束,由发送方来判断,如果读取完毕则发送实际长度
                swpd.setCreateUserId(parent.getUser().getId());
                swpd.setType(type);
                swpd.setFileMd5(fileMd5);
                swpd.setSeek(seek);
                swpd.setStream(b);
                seek += rlen;
                if (in.isFinished()) {//读取完成了,修正文件总长度
                    swpd.setTotalSize(seek);
                }
                ResponseHeadDTO<StreamWriteResultDTO> wh = manageFactory.getFileService().writeByte(swpd);
                if (!wh.isResult()) {
                    throw new RuntimeException(wh.getMessage());
                }
                if (wh.getData() != null) {
                    along = wh.getData().getTotalSize();
                    s = wh.getData().getMediaType();
                }
            }
            //所有分片发送结束,将目录信息补全
            NetdiskDirectoryDTO upDto = directory.getData();
            upDto.setSize(along.toString());
            upDto.setMediaType(s);
            ResponseHeadDTO<NetdiskDirectoryDTO> upDirectory = manageFactory.getNetdiskDirectory().updateDirectory(upDto);
            if (!upDirectory.isResult()) {
                throw new RuntimeException("文件上传失败");
            }
        }catch (Throwable e){
            log.error("发送文件流异常",e);
            //删除预先创建的目录
            manageFactory.getNetdiskDirectory().delDirectory(userSpace.getId(),directory.getData().getId());
            throw new BadRequestException(this,e.getMessage());
        }
        return new MyFolderResource(getUrl() + "/" + name,new FolderVO(directory.getData().getName(),directory.getData().getId(),directory.getData().getCreateTime(),directory.getData().getUpdateTime())
                ,manageFactory,super.getUserSpace());
    }
}
