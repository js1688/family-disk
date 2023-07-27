package com.jflove.scheduling.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.download.api.IOfflineDownloadService;
import com.jflove.download.em.DownloadStatusENUM;
import com.jflove.file.api.IFileAdministration;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.mapper.download.OdRecordMapper;
import com.jflove.netdisk.api.INetdiskDirectory;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.po.download.OdRecordPO;
import com.jflove.stream.api.IFileStreamService;
import com.jflove.stream.dto.StreamWriteParamDTO;
import com.jflove.user.api.IUserSpace;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author: tanjun
 * @date: 2023/4/23 11:36 AM
 * @desc: 离线下载,扫描已经完成的任务,进行转存
 */
@Service
@Log4j2
@EnableAsync
public class OfflineDownloadService {
    @DubboReference
    private IOfflineDownloadService offlineDownloadService;
    @DubboReference
    private IFileStreamService fileStreamService;
    @DubboReference
    private INetdiskDirectory netdiskDirectory;
    @DubboReference
    private IFileAdministration fileAdministration;
    @DubboReference
    private IUserSpace userSpace;

    @Autowired
    private OdRecordMapper odRecordMapper;

    private Lock lock = new ReentrantLock();//此任务执行频率高,但执行过程也许会比较慢,所以需要加锁, 保证同时只有一个过程在执行

    @Async("myTaskExecutor")
    @Transactional
    public void run() {
        boolean is = false;
        try{
            is = lock.tryLock();//不能阻塞线程;
            if(!is){//未拿到锁
                return;
            }
            List<OdRecordPO> list = odRecordMapper.selectList(new LambdaQueryWrapper<OdRecordPO>().select(OdRecordPO::getSpaceId));
            Set<Long> spaceIds = list.stream().map(e->e.getSpaceId()).collect(Collectors.toSet());
            spaceIds.forEach(v->{
                //查询这个空间下每个任务的下载进度
                ResponseHeadDTO result = offlineDownloadService.getFiles(v,null);
                if(result.getDatas() != null) {
                    List array = result.getDatas();
                    array.forEach(t->{
                        JSONObject jo = (JSONObject) t;
                        if(DownloadStatusENUM.complete == DownloadStatusENUM.valueOf(jo.getStr("status"))){//下载完成了,需要执行转存
                            try {
                                String fileName = jo.getStr("fileName");
                                String filePath = jo.getStr("dir") + "/" + fileName;
                                Path of = Path.of(filePath);
                                DataSize totalLength = DataSize.ofBytes(Files.size(of));
                                OdRecordPO upPo = jo.toBean(OdRecordPO.class);
                                //开始转存文件
                                String type = fileName.lastIndexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".")) : "";
                                String mediaType = Files.probeContentType(of);
                                Map<String, Long> sliceInfo = countFileSliceInfo(totalLength.toBytes());
                                Long sliceNum = sliceInfo.get("sliceNum");//分片数量
                                Long sliceSize = sliceInfo.get("sliceSize");//分片大小
                                String md5 = fileMd5(sliceNum,sliceSize,totalLength.toBytes(),filePath);
                                //尝试是否可以从垃圾箱恢复
                                if(fileAdministration.dustbinRecovery(md5,v,FileSourceENUM.CLOUDDISK).isResult()){
                                    appendRel(upPo,totalLength,v,mediaType,fileName,jo.getInt("targetId"),md5,jo.getStr("gid"));
                                    return;
                                }
                                //判断用户空间是否存储的下
                                ResponseHeadDTO use = userSpace.useSpaceByte(v, totalLength.toMegabytes(), true, false);
                                if (!use.isResult()) {
                                    upPo.setMsg("转存失败,用户存储空间不足");
                                    odRecordMapper.updateById(upPo);
                                    return;
                                }
                                //尝试是否可以直接引用其它人上传的文件
                                if(fileAdministration.checkDuplicate(fileName,type,mediaType,
                                        md5,v,FileSourceENUM.CLOUDDISK,totalLength.toBytes(),(Long)use.getData()).isResult()){
                                    appendRel(upPo,totalLength,v,mediaType,fileName,jo.getInt("targetId"),md5,jo.getStr("gid"));
                                    return;
                                }
                                //无法从其它地方直接引用,将文件执行转存
                                try(RandomAccessFile raf = new RandomAccessFile(new File(filePath), "r")) {
                                    for (int i = 0; i <= sliceNum; i++) {
                                        byte[] a = null;
                                        long seek = i * sliceSize;
                                        Long readLength = seek + sliceSize > totalLength.toBytes() ? totalLength.toBytes() - seek : sliceSize;

                                        raf.seek(seek);
                                        a = new byte[readLength.intValue()];
                                        raf.read(a);

                                        ResponseHeadDTO<String> wr = sendStream(v, totalLength.toBytes(), fileName, (Long) use.getData(),
                                                seek, a, sliceNum.intValue(), i + 1, md5, mediaType, type);
                                        if (!wr.isResult()) {
                                            upPo.setMsg(wr.getMessage());
                                            odRecordMapper.updateById(upPo);
                                            return;
                                        }
                                    }
                                }
                                appendRel(upPo,totalLength,v,mediaType,fileName,jo.getInt("targetId"),md5,jo.getStr("gid"));
                            }catch (Throwable e){
                                log.error("发送文件流异常",e);
                            }
                        }
                    });
                }
            });
        }catch (Exception e){
            log.error("离线下载转存异常",e);
        }finally {
            if(is){
                lock.unlock();
            }
        }
    }

    /**
     * 将文件md5与网盘建立起关系
     * @param upPo
     * @param totalLength
     * @param spaceId
     * @param mediaType
     * @param fileName
     * @param pid
     * @param md5
     * @param gid
     */
    private void appendRel(OdRecordPO upPo,DataSize totalLength,Long spaceId,String mediaType,String fileName,int pid,String md5,String gid){
        //所有分片发送结束,开始建立网盘目录与文件的关系
        NetdiskDirectoryDTO netDto = new NetdiskDirectoryDTO();
        netDto.setSize(String.valueOf(totalLength.toMegabytes()));
        netDto.setType(NetdiskDirectoryENUM.FILE);
        netDto.setSpaceId(spaceId);
        netDto.setMediaType(mediaType);
        netDto.setName(fileName);
        netDto.setPid(pid);
        netDto.setFileMd5(md5);
        ResponseHeadDTO<NetdiskDirectoryDTO> directory = netdiskDirectory.addDirectory(netDto);
        if(!directory.isResult()){
            upPo.setMsg(directory.getMessage());
            odRecordMapper.updateById(upPo);
            return;
        }
        //文件关联成功,将下载任务设置为删除
        ResponseHeadDTO dresult = offlineDownloadService.remove(spaceId,gid);
        if(!dresult.isResult()){
            upPo.setMsg(dresult.getMessage());
            odRecordMapper.updateById(upPo);
            return;
        }
        //任务成功转存到网盘,删除记录
        odRecordMapper.deleteById(upPo.getId());
    }

    /**
     * 计算文件的md5值
     * @param sliceNum
     * @param sliceSize
     * @param totalSize
     * @param filePath
     * @return
     */
    private String fileMd5(Long sliceNum,Long sliceSize,Long totalSize,String filePath){

        //拿第一个分片与最后一个分片的字节组成md5值,如果分片不大于1,则取完整的md5值
        try(RandomAccessFile raf = new RandomAccessFile(new File(filePath), "r")){
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
     * 发送文件流
     * @param spaceId 文件存储空间
     * @param totalLength 文件总大小
     * @param fileName 文件名称
     * @param createUserId 文件所属用户id
     * @param seek 写盘起始位置
     * @param stream 本次写入字节流
     * @param shardingSort 当前第几片
     * @param shardingNum 总共分多少片
     * @param fileMd5 文件md5值
     * @param mediaType 文件媒体类型
     * @param type 文件后缀类型
     */
    private ResponseHeadDTO<String> sendStream(Long spaceId,Long totalLength,String fileName,Long createUserId,
                            long seek,byte [] stream,Integer shardingSort,Integer shardingNum,
                            String fileMd5,String mediaType,String type){
        StreamWriteParamDTO swpd = new StreamWriteParamDTO();
        swpd.setOriginalFileName(fileName);
        swpd.setSource(FileSourceENUM.CLOUDDISK);
        swpd.setSpaceId(spaceId);
        swpd.setTotalSize(totalLength);
        //查询空间ID是哪个用户的
        swpd.setCreateUserId(createUserId);
        swpd.setType(type);
        swpd.setMediaType(mediaType);
        swpd.setShardingSort(shardingSort);
        swpd.setShardingNum(shardingNum);
        swpd.setFileMd5(fileMd5);
        swpd.setSeek(seek);
        swpd.setStream(stream);
        return fileStreamService.writeByte(swpd);
    }
}
