package com.jflove.webdav.resources;

import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
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
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    /**
     * 计算文件的md5值
     * @param sliceNum
     * @param sliceSize
     * @param totalSize
     * @param raf
     * @return
     */
    private String fileMd5(Long sliceNum,Long sliceSize,Long totalSize,RandomAccessFile raf){
        //拿第一个分片与最后一个分片的字节组成md5值,如果分片不大于1,则取完整的md5值
        try{
            raf.seek(0);
            byte[] c = null;
            if(sliceNum > 1) {
                byte[] a = new byte[sliceSize.intValue()];
                raf.read(a);
                raf.seek(totalSize.intValue() - sliceSize.intValue());
                byte[] b = new byte[sliceSize.intValue()];
                raf.read(b);
                c = new byte[a.length + b.length];
                for (int i = 0; i < c.length; i++) {
                    c[i] = i >= a.length ? b[i - a.length] : a[i];
                }
            }else {
                c = new byte[totalSize.intValue()];
                raf.read(c);
            }
            return SecureUtil.md5(new String(c));
        }catch (IOException e){
            log.error("计算文件md5值发生异常",e);
        }
        return null;
    }

    /**
     * 注意:如果使用了nginx代理服务,一定要注意如下配置
     * client_max_body_size 0;#不检查文件流大小
     * proxy_ignore_client_abort on;#当客户端主动断开时,不切断nginx与服务端的联系,nginx等待服务端执行完毕
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
        Path path = Path.of(String.format("%s/%s%s", manageFactory.getFileTempPath(), SecureUtil.md5(name), type));
        if(Files.exists(path)){//这个文件已存在,已经在接收文件流了
            throw new BadRequestException("文件正在写入,不需要重复请求");
        }

        //判断要上传的文件目录是否已存在,如果已存在则先删除
        ResponseHeadDTO<NetdiskDirectoryDTO> urlThis = manageFactory.getDirectoryByUrl(userSpace.getId(),super.getUrl() + "/" + name);
        if(urlThis.isResult()){
            //删除,达到覆盖目录的目的
            manageFactory.getNetdiskDirectory().delDirectory(userSpace.getId(),urlThis.getData().getId());
        }
        Long totalLength = null;
        String fileMd5 = null;
        Long sliceNum = null;
        Long sliceSize = null;
        try(RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
            while (!in.isFinished()) {
                byte[] b = new byte[8192];
                int rlen = in.read(b);
                raf.write(b, 0, rlen);
            }
            //写盘结束,从文件中读取一些必要的信息提高兼容性
            //因为webdav上传文件时,totalLength 和 mediaType 无法保证一定存在,所以增加缓存目录,用于对文件信息的读取
            totalLength = raf.length();
            //计算可传输分片大小
            Map<String, Long> sliceInfo = countFileSliceInfo(totalLength);
            sliceNum = sliceInfo.get("sliceNum");//分片数量
            sliceSize = sliceInfo.get("sliceSize");//分片大小
            fileMd5 = fileMd5(sliceNum, sliceSize, totalLength, raf);
        }catch (Throwable e) {
            throw new BadRequestException("网络文件缓存到服务器缓存目录发生错误",e);
        }
        String mediaType = Files.probeContentType(path);
        //尝试是否可以从垃圾箱恢复
        ResponseHeadDTO dr = manageFactory.getFileAdministration().dustbinRecovery(fileMd5,userSpace.getId(), FileSourceENUM.CLOUDDISK);
        if(!dr.isResult()) {//如果没有从垃圾箱恢复
            //判断用户空间是否存储的下
            ResponseHeadDTO use = manageFactory.getUserSpace().useSpaceByte(userSpace.getId(), DataSize.ofBytes(totalLength).toMegabytes(), true, false);
            if (!use.isResult()) {
                throw new BadRequestException(this, "用户存储空间不足");
            }
            //尝试是否可以直接引用其它人上传的文件
            if (!manageFactory.getFileAdministration().checkDuplicate(name, type, mediaType,
                    fileMd5, userSpace.getId(), FileSourceENUM.CLOUDDISK, totalLength, userSpace.getCreateUserId()).isResult()) {//需要发送文件
                //使用异步的方式去发送,防止整个过程太慢长导致客户端主动断开连接
                Long finalSliceNum = sliceNum;
                Long finalSliceSize = sliceSize;
                Long finalTotalLength = totalLength;
                String finalFileMd = fileMd5;
                new Thread(()->{
                    //持续读取每片数据,做分片读取与传输,主要是防止大文件的情况下内存溢出
                    try(RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")){
                        for (int i = 0; i <= finalSliceNum; i++) {
                            long seek = i * finalSliceSize;
                            Long readLength = seek + finalSliceSize > finalTotalLength ? finalTotalLength - seek : finalSliceSize;
                            byte[] b = new byte[readLength.intValue()];
                            //从本地磁盘中读取文件
                            raf.seek(seek);
                            raf.read(b);
                            StreamWriteParamDTO swpd = new StreamWriteParamDTO();
                            swpd.setOriginalFileName(name);
                            swpd.setSource(FileSourceENUM.CLOUDDISK);
                            swpd.setSpaceId(userSpace.getId());
                            swpd.setTotalSize(finalTotalLength);
                            //查询空间ID是哪个用户的
                            swpd.setCreateUserId(parent.getUser().getId());
                            swpd.setType(type);
                            swpd.setMediaType(mediaType);
                            swpd.setShardingSort(i + 1);
                            swpd.setShardingNum(finalSliceNum.intValue());
                            swpd.setFileMd5(finalFileMd);
                            swpd.setSeek(seek);
                            swpd.setStream(b);
                            ResponseHeadDTO<StreamWriteResultDTO> wh = manageFactory.getFileService().writeByte(swpd);
                            if (!wh.isResult()) {
                                throw new RuntimeException(wh.getMessage());
                            }
                        }
                    }catch (Throwable e) {
                        log.error("异步发送文件流到文件存储模块失败,{}", e);
                        //将建立起的临时关系删除掉,因为文件没有写盘成功
                        ResponseHeadDTO<NetdiskDirectoryDTO> lsNd = manageFactory.getDirectoryByUrl(userSpace.getId(),super.getUrl() + "/" + name);
                        if(lsNd.isResult()){
                            manageFactory.getNetdiskDirectory().delDirectory(lsNd.getData().getSpaceId(),lsNd.getData().getId());
                        }
                    }finally {
                        //将临时文件删除
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                        }
                    }
                }).start();
            }
        }

        //所有分片发送结束或无需发送分片,开始建立网盘目录与文件的关系,此时关系是临时的,不代表真的把文件写盘了
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
