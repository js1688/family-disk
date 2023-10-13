package com.jflove.webdav.resources;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.stream.dto.StreamWriteParamDTO;
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
import org.springframework.http.HttpHeaders;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * 计算文件对象分片信息,可分多少片
     * @param totalLength
     * @constructor
     */
    private Map<String,Long> countFileSliceInfo(long totalLength){
        int sliceSize = 1024 * 1024 * 3;//每片的大小
        if(totalLength < sliceSize){
            sliceSize = (int)totalLength;
        }
        double sliceNum = Math.ceil(totalLength/sliceSize);//分片数量
        return MapUtil.builder("sliceNum",Double.valueOf(sliceNum).longValue())
                .put("sliceSize",Integer.valueOf(sliceSize).longValue())
                .put("totalSize",totalLength)
                .build();
    }

    @Override
    public Resource createNew(String name, InputStream inputStream, Long totalLength, String mediaType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
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
        mediaType = Optional.ofNullable(mediaType).orElse(request.getHeaders().get(HttpHeaders.CONTENT_TYPE.toLowerCase()));//如果从参数中拿不到就从请求头拿

        //从流中读取文件
        //webdav上传文件不会从垃圾箱回收,也不会直接引用其他人的资源,文件的md5也跟文件内容无关,因为目前没办法实现交互控制,以及分片控制
        Map<String, Long> sliceInfo = countFileSliceInfo(totalLength);
        Long sliceNum = sliceInfo.get("sliceNum");//分片数量
        Long sliceSize = sliceInfo.get("sliceSize");//分片大小
        String type = name.lastIndexOf(".") != -1 ? name.substring(name.lastIndexOf(".")) : "";

        //判断用户空间是否存储的下
        ResponseHeadDTO use = manageFactory.getUserSpace().useSpaceByte(userSpace.getId(), DataSize.ofBytes(totalLength).toMegabytes(), true, false);
        if (!use.isResult()) {
            throw new BadRequestException(this,"用户存储空间不足");
        }
        //持续读取每片数据,做分片读取与传输,主要是防止大文件的情况下内存溢出
        String fileMd5 = null;
        for (int i = 0; i <= sliceNum; i++) {
            long seek = i * sliceSize;
            Long readLength = seek + sliceSize > totalLength ? totalLength - seek : sliceSize;
            byte [] b = inputStream.readNBytes(readLength.intValue());
            if(fileMd5 == null){
                fileMd5 = SecureUtil.md5(new String(b));//这里计算MD5值与其它地方上传文件不一样,区别是,这里无法直接得到尾部的一块分片,所以只把首部的分片用于计算MD5值
            }
            StreamWriteParamDTO swpd = new StreamWriteParamDTO();
            swpd.setOriginalFileName(name);
            swpd.setSource(FileSourceENUM.CLOUDDISK);
            swpd.setSpaceId(userSpace.getId());
            swpd.setTotalSize(totalLength);
            //查询空间ID是哪个用户的
            swpd.setCreateUserId(parent.getUser().getId());
            swpd.setType(type);
            swpd.setMediaType(mediaType);
            swpd.setShardingSort(i + 1);
            swpd.setShardingNum(sliceNum.intValue());
            swpd.setFileMd5(fileMd5);
            swpd.setSeek(seek);
            swpd.setStream(b);
            ResponseHeadDTO<String> wh = manageFactory.getFileService().writeByte(swpd);
            if (!wh.isResult()) {
                throw new BadRequestException(this,"文件流写入失败");
            }
        }
        //所有分片发送结束,开始建立网盘目录与文件的关系
        NetdiskDirectoryDTO netDto = new NetdiskDirectoryDTO();
        netDto.setSize(totalLength.toString());
        netDto.setType(NetdiskDirectoryENUM.FILE);
        netDto.setSpaceId(userSpace.getId());
        netDto.setMediaType(mediaType);
        netDto.setName(name);
        netDto.setPid(urlLast.getData().getId());
        netDto.setFileMd5(fileMd5);
        ResponseHeadDTO<NetdiskDirectoryDTO> directory = manageFactory.getNetdiskDirectory().addDirectory(netDto);
        if(!directory.isResult()){
            throw new BadRequestException(this,"文件创建上传失败");
        }
        return new MyFolderResource(getUrl(),new FolderVO(directory.getData().getName(),directory.getData().getId(),directory.getData().getCreateTime(),directory.getData().getUpdateTime())
                ,manageFactory,super.getUserSpace());
    }
}
