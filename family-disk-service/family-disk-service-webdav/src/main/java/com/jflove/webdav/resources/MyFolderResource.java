package com.jflove.webdav.resources;

import cn.hutool.core.map.MapUtil;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.stream.dto.StreamWriteParamDTO;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.webdav.factory.ManageFactory;
import com.jflove.webdav.vo.BaseVO;
import com.jflove.webdav.vo.FileVO;
import com.jflove.webdav.vo.FolderVO;
import io.milton.http.Auth;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.FolderResource;
import io.milton.resource.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public MyFolderResource(String url, ManageFactory manageFactory, UserSpaceDTO userSpace){
        super(manageFactory,url,userSpace);
        this.manageFactory = manageFactory;
    }




    public MyFolderResource(FolderVO folder,ManageFactory manageFactory,UserSpaceDTO userSpace) {
        super(manageFactory,null,userSpace);
        this.folder = folder;
        super.setBase(folder);
        this.manageFactory = manageFactory;
    }

    @Override
    public BaseVO initBase() {
        //通过url识别出目录信息
        ResponseHeadDTO<NetdiskDirectoryDTO> urlLast = manageFactory.getDirectoryByUrl(super.getUserSpace().getId(),super.getUrl());
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
                list.add(new MyFolderResource(new FolderVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime()),manageFactory,super.getUserSpace()));
            }else if(NetdiskDirectoryENUM.FILE == v.getType()){
                list.add(new MyFileResource(new FileVO(v.getName(),v.getId(),v.getCreateTime(),v.getUpdateTime(),v.getMediaType(),v.getSizeB(),v.getFileMd5()),manageFactory,super.getUserSpace()));
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
        return new MyFolderResource(new FolderVO(result.getData().getName(),result.getData().getId(),result.getData().getCreateTime(),result.getData().getUpdateTime())
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
        log.debug("createNew,s1:{},along:{},s1:{}",name,totalLength,mediaType);
        //检查是否对这个空间有写入权限
        //todo 奇怪,为什么调用创建接口不先调用认证接口,导致这里拿不到一些基本的用户信息?要想想办法
        if(super.role != UserSpaceRoleENUM.WRITE){
            throw new NotAuthorizedException("没有这个空间的写入权限",this);
        }
        ResponseHeadDTO<NetdiskDirectoryDTO> urlLast = manageFactory.getDirectoryByUrl(userSpace.getId(),super.getUrl());
        if(!urlLast.isResult()){
            throw new NotAuthorizedException("找不到父级目录",this);
        }
        //从流中读取文件
        //webdav上传文件不会从垃圾箱回收,也不会直接引用其他人的资源,文件的md5也跟文件内容无关,因为目前没办法实现交互控制,以及分片控制
        Map<String, Long> sliceInfo = countFileSliceInfo(totalLength);
        Long sliceNum = sliceInfo.get("sliceNum");//分片数量
        Long sliceSize = sliceInfo.get("sliceSize");//分片大小
        String fileMd5 = UUID.randomUUID().toString().replaceAll("-","");//todo 这里无法取首位的数据计算MD5值,也不确定能不能做到分片上传,所以暂时生成UUID
        String type = name.lastIndexOf(".") != -1 ? name.substring(name.lastIndexOf(".")) : "";

        //判断用户空间是否存储的下
        ResponseHeadDTO use = manageFactory.getUserSpace().useSpaceByte(userSpace.getId(), DataSize.ofBytes(totalLength).toMegabytes(), true, false);
        if (!use.isResult()) {
            throw new BadRequestException(this,"用户存储空间不足");
        }

        for (int i = 0; i <= sliceNum; i++) {
            long seek = i * sliceSize;
            Long readLength = seek + sliceSize > totalLength ? totalLength - seek : sliceSize;
            inputStream.skip(seek);
            byte [] b = inputStream.readNBytes(readLength.intValue());
            StreamWriteParamDTO swpd = new StreamWriteParamDTO();
            swpd.setOriginalFileName(name);
            swpd.setSource(FileSourceENUM.CLOUDDISK);
            swpd.setSpaceId(userSpace.getId());
            swpd.setTotalSize(totalLength);
            //查询空间ID是哪个用户的
            swpd.setCreateUserId(user.getId());
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
        return new MyFolderResource(new FolderVO(directory.getData().getName(),directory.getData().getId(),directory.getData().getCreateTime(),directory.getData().getUpdateTime())
                ,manageFactory,super.getUserSpace());
    }
}
