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
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
        //如果被分片,每一片大小最多是最大允许上传的10%
        DataSize ds = DataSize.of((long)(maxFileSize.toMegabytes() * 0.1), DataUnit.MEGABYTES);
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
                    ab.set(true);
                    log.error("读取文件异常",throwable);
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
                TimeUnit.SECONDS.sleep(1);
                if(ab.get()){
                    break;
                }
            }
            request.setAttribute(CONTENT_TYPE,dto.getMediaType());
            request.setAttribute(MAX_SIZE,dto.getTotalSize());
            request.setAttribute(RANGE_LEN,(int)dto.getReadLength());
            Resource file = new ByteArrayResource(dto.getShardingStream());
            return file;
        }catch (Exception e){
            throw new IOException(e);
        }
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rangeString = request.getHeader(HttpHeaders.RANGE);
        if(!StringUtils.hasLength(rangeString)){
            throw new ServletException("Range 不能为空");
        }
        long rangeStart = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
        request.setAttribute(RANGE_START,rangeStart);
        Resource resource = this.getResource(request);
        if (resource == null) {
            response.sendError(404);
        } else if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setHeader("Allow", this.getAllowHeader());
        } else {
            response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
            if(request.getAttribute(CONTENT_TYPE) != null) {
                response.setContentType((String) request.getAttribute(CONTENT_TYPE));
            }
            response.setHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %s-%s/%s",rangeStart,
                    (rangeStart + (int)request.getAttribute(RANGE_LEN)-1),
                    (long)request.getAttribute(MAX_SIZE)));
            response.setContentLength((int)request.getAttribute(RANGE_LEN));
            response.getOutputStream().write(resource.getInputStream().readAllBytes());
        }
    }
}
