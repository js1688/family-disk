package com.jflove.webdav.resources;

import cn.hutool.json.JSONUtil;
import com.jflove.ResponseHeadDTO;
import com.jflove.stream.dto.StreamReadResultDTO;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.webdav.factory.ManageFactory;
import com.jflove.webdav.vo.FileVO;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.FileResource;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
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
    private ManageFactory manageFactory;

    public MyFileResource(String url,FileVO file, ManageFactory manageFactory,UserSpaceDTO userSpace) {
        super(manageFactory,url,userSpace);
        this.file = file;
        super.setBase(file);
        this.manageFactory = manageFactory;
    }

    @Override
    public void sendContent(OutputStream outputStream, Range range, Map<String, String> map, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        log.debug("读取文件:{}", JSONUtil.toJsonStr(file));
        if(range == null || range.getLength() == null){
            long start = Optional.ofNullable(Optional.ofNullable(range).orElse(new Range(0,0)).getStart()).orElse(0l);
            long maxLen = 1024*1024*3;//3mb
            while (start < file.getContentLength()){
                long end = Math.min(start + maxLen,file.getContentLength());
                long len = (int)(end - start);
                ResponseHeadDTO<StreamReadResultDTO> result = manageFactory.readFile(super.getUserSpace().getId(),start,len,file.getMd5());
                if(result.isResult()){
                    outputStream.write(result.getData().getStream());
                    outputStream.flush();
                }
                start = end;
            }
        }else{//指定了访问长度与结束位置
            long len = range.getLength().intValue();
            ResponseHeadDTO<StreamReadResultDTO> result = manageFactory.readFile(super.getUserSpace().getId(),range.getStart(),len,file.getMd5());
            if(result.isResult()){
                outputStream.write(result.getData().getStream());
            }
        }
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return 86400l;//24h
    }
}
