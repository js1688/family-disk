package com.jflove.config;

import com.jflove.file.api.IFileService;
import com.jflove.file.dto.FileTransmissionDTO;
import com.jflove.file.em.FileSourceENUM;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: tanjun
 * @date: 2023/1/9 10:31 AM
 * @desc: 大文件静态资源请求响应重写,从字节中获取数据流
 */
@Component
@Log4j2
public class ByteResourceHttpRequestHandlerConfig extends ResourceHttpRequestHandler {

    public static final String RANGE_START = "BYTE_RANGE_START";
    public static final String RANGE_END = "BYTE_RANGE_END";
    public static final String MAX_SIZE = "BYTE_MAX_SIZE";
    public static final String RANGE_LEN = "BYTE_RANGE_LEN";
    public static final String SOURCE = "BYTE_SOURCE";
    public static final String SPACE_ID = "BYTE_SPACE_ID";
    public static final String CONTENT_TYPE = "BYTE_CONTENTTYPE";


    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    @DubboReference
    private IFileService fileService;

    /**
     * 重写获取静态资源方法,不从默认的项目resource获取
     * 重写为绝对路径获取
     * @param request
     * @return
     * @throws IOException
     */
    @Override
    protected Resource getResource(HttpServletRequest request) throws IOException {
        String path = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        long rangeStart = (long)request.getAttribute(RANGE_START);
        Long rangeEnd = (Long)request.getAttribute(RANGE_END);
        DataSize ds = null;
        if(rangeEnd == null) {//如果请求没有指定结束长度,则自己设置
            //如果被分片,每一片大小最多是最大允许上传的10%
            ds = DataSize.of((long) (maxFileSize.toMegabytes() * 0.1), DataUnit.MEGABYTES);
        }else{//如果设置了结束长度,则以设置为准
            ds = DataSize.of(rangeEnd,DataUnit.BYTES);
        }
        try{
            AtomicBoolean ab = new AtomicBoolean(false);
            FileTransmissionDTO dto = new FileTransmissionDTO();
            StreamObserver<FileTransmissionDTO> repStream = fileService.readByte(new StreamObserver<FileTransmissionDTO>() {
                @Override
                public void onNext(FileTransmissionDTO data) {
                    BeanUtils.copyProperties(data,dto);
                    ab.set(true);
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("文件读写服务异常",throwable);
                    ab.set(true);
                }

                @Override
                public void onCompleted() {
                }
            });
            dto.setFileMd5(path);
            dto.setReadLength(ds.toBytes());
            dto.setRangeStart(rangeStart);
            dto.setRangeEnd(rangeStart + dto.getReadLength());
            dto.setSource(FileSourceENUM.valueOf((String)request.getAttribute(SOURCE)));
            dto.setSpaceId((Long)request.getAttribute(SPACE_ID));
            repStream.onNext(dto);
            while (true){
                TimeUnit.MILLISECONDS.sleep(200);
                if(ab.get()){
                    break;
                }
            }
            repStream.onCompleted();//重要,当不需要再请求了,必须要调这个函数通知对方,对方也调用这个函数通知请求方,如果不这么做,会导致不会释放,最终内存崩溃
            request.setAttribute(CONTENT_TYPE,dto.getMediaType());
            request.setAttribute(MAX_SIZE,(int)dto.getTotalSize());
            request.setAttribute(RANGE_LEN,(int)dto.getReadLength());
            ByteArrayResource file = new ByteArrayResource(dto.getShardingStream());
            return file;
        }catch (Exception e){
            log.error("错误",e);
            throw new IOException(e);
        }
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rangeString = request.getHeader(HttpHeaders.RANGE);
        if(!StringUtils.hasLength(rangeString)){
            throw new ServletException("Range 不能为空");
        }
        //苹果公司的设备,会先发一个 bytes=0-1 探测一下视频总共有多大,与其它浏览器,比如谷歌内核的不同,
        //谷歌内核的请求时会发送 bytes=0- , 返回多少自己设置即可,可以响应206要求继续请求,兼容性比较强
        //safari的必须先返回总长度,和200响应码
        long rangeStart = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
        String [] sp = rangeString.split("-");
        Long rangeEnd = null;
        if(sp.length == 2) {//有发送结束位置
            rangeEnd = Long.parseLong(sp[1]);
        }
        request.setAttribute(RANGE_START,rangeStart);
        request.setAttribute(RANGE_END,rangeEnd);
        ByteArrayResource resource = (ByteArrayResource)this.getResource(request);
        if (resource == null) {
            response.sendError(404);
        } else if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setHeader("Allow", this.getAllowHeader());
        } else {
            if(rangeEnd != null && rangeEnd.longValue() == 1l){//探测请求,返回200
                response.setStatus(HttpStatus.OK.value());
                response.setContentLength((int) request.getAttribute(MAX_SIZE));
            }else {
                response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
                response.setContentLength((int) request.getAttribute(RANGE_LEN));
            }
            if(request.getAttribute(CONTENT_TYPE) != null) {
                response.setContentType((String) request.getAttribute(CONTENT_TYPE));
            }
            response.setHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %s-%s/%s",rangeStart,
                    (rangeStart + (int)request.getAttribute(RANGE_LEN)-1),
                    (int)request.getAttribute(MAX_SIZE)));
            try(ServletOutputStream sos = response.getOutputStream()){
                sos.write(resource.getByteArray());
            }
        }
    }
}
