package com.jflove.webdav.resources;

import com.jflove.webdav.vo.FileVO;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.FileResource;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Optional;

/**
 * @author: tanjun
 * @date: 2023/9/15 10:24 AM
 * @desc:
 */
@Log4j2
public class MyFileResource extends BaseResource implements FileResource {
    private FileVO file;

    public MyFileResource(FileVO file) {
        super(file);
        this.file = file;
    }

    @Override
    public void sendContent(OutputStream outputStream, Range range, Map<String, String> map, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        try(
                RandomAccessFile raf = new RandomAccessFile(new File(file.getDiskPath()), "r");
        ) {
            log.debug("请求访问资源:{}",file.getName());
            //如果没有指定范围或者指定了开始,但没有指定结束位置,则全部响应回去,分多次刷,避免内存溢出
            if(range == null || range.getLength() == null){
                long start = Optional.ofNullable(Optional.ofNullable(range).orElse(new Range(0,0)).getStart()).orElse(0l);
                long maxLen = 1024*1024*3;//3mb
                while (start < raf.length()){
                    long end = Math.min(start + maxLen,raf.length());
                    byte [] b = new byte[(int)(end - start)];
                    raf.seek(start);
                    raf.read(b);
                    outputStream.write(b);
                    outputStream.flush();
                    start = end;
                }
            }else{//指定了访问长度与结束位置
                byte [] b = new byte[range.getLength().intValue()];
                raf.seek(range.getStart());
                raf.read(b);
                outputStream.write(b);
            }
        }
    }
}
