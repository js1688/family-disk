package com.jflove.controller.stream;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.file.api.IFileAdministration;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.handler.ByteResourceHttpRequestHandler;
import com.jflove.stream.api.IFileStreamService;
import com.jflove.stream.dto.StreamWriteParamDTO;
import com.jflove.tool.JJwtTool;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceRelDTO;
import com.jflove.user.em.UserRelStateENUM;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.file.GetFileParamVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

/**
 * @author: tanjun
 * @date: 2023/4/21 3:49 PM
 * @desc: 文件流读写
 */
@RestController
@RequestMapping("/stream")
@Api(tags = "文件流读写")
@Log4j2
public class FileStreamController {

    @Autowired
    private HttpServletRequest autowiredRequest;

    @Autowired
    private JJwtTool jJwtTool;

    @DubboReference
    private IUserInfo userInfo;

    @DubboReference
    private IUserSpace userSpace;

    @DubboReference
    private IFileStreamService fileService;

    @DubboReference
    private IFileAdministration fileAdministration;

    @Autowired
    private ByteResourceHttpRequestHandler resourceHttpRequestHandle;

    @ApiOperation(value = "媒体资源边播边下")
    @GetMapping("/media/play/{source}/{token}/{fileMd5}")
    public void mediaPlay(HttpServletRequest request, HttpServletResponse response,
                          @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,JOURNAL=日记)") @PathVariable("source") String source,
                          @ApiParam("文件md5值") @PathVariable("fileMd5") String fileMd5,
                          @ApiParam("token") @PathVariable("token") String token
    )throws IOException, ServletException {
        Assert.hasLength(fileMd5,"错误的请求:文件md5不能为空");
        Assert.hasLength(source,"错误的请求:文件来源不能为空");
        Assert.hasLength(token,"错误的请求:token不能为空");
        Jws<Claims> jws = jJwtTool.parseJwt(token);
        Claims claims = jws.getBody();
        //token验证通过,返回用户信息
        ResponseHeadDTO<UserInfoDTO> dto = userInfo.getUserInfoByEmail(claims.getId());
        Assert.notNull(dto.getData(),dto.getMessage());
        Optional<UserSpaceRelDTO> rel = dto.getData().getSpaces().stream()
                .filter(e->e.getState() == UserRelStateENUM.USE).findFirst();
        if(rel.isEmpty()){
            throw new SecurityException("请先切换到空间");
        }
        Assert.notNull(rel.get().getRole(),"错误的请求:正在使用的空间权限不能为空");
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,fileMd5);
        request.setAttribute(ByteResourceHttpRequestHandler.SOURCE,source);
        request.setAttribute(ByteResourceHttpRequestHandler.SPACE_ID,rel.get().getSpaceId());
        resourceHttpRequestHandle.handleRequest(request, response);
    }

    @ApiOperation(value = "下载文件(分片下载方式)")
    @PostMapping("/slice/getFile")
    public void sliceGetFile(
            HttpServletRequest request,HttpServletResponse response,
            @RequestBody @Valid GetFileParamVO param) throws Exception{
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,param.getFileMd5());
        request.setAttribute(ByteResourceHttpRequestHandler.SOURCE,param.getSource());
        request.setAttribute(ByteResourceHttpRequestHandler.SPACE_ID,useSpaceId);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,HttpHeaders.CONTENT_RANGE);
        resourceHttpRequestHandle.handleRequest(request, response);
    }

    @ApiOperation(value = "上传文件(大文件,分片),只能按分片顺序同步上传,不可以异步上传")
    @PostMapping("/slice/addFile")
    public ResponseHeadVO<String> sliceAddFile(@ApiParam("文件流") @RequestPart("f") MultipartFile f,
                                               @ApiParam("文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,JOURNAL=日记)") @RequestParam("s") String s,
                                               @ApiParam("文件多媒体类型") @RequestParam("m") String m,
                                               @ApiParam("文件分片数量") @RequestParam("n") Integer n,
                                               @ApiParam("开始位置") @RequestParam("start") Long start,
                                               @ApiParam("结束位置") @RequestParam("end") Long end,
                                               @ApiParam("总大小") @RequestParam("totalLength") Long totalLength,
                                               @ApiParam("文件真实名称") @RequestParam("originalFileName") String originalFileName,
                                               @ApiParam("前端计算的文件md5") @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"请先切换到空间");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        Long useUserId = (Long) autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        if (useSpacerRole != UserSpaceRoleENUM.WRITE) {
            throw new SecurityException("用户对该空间没有写入权限");
        }
        StreamWriteParamDTO param = new StreamWriteParamDTO();
        param.setTotalSize(totalLength);
        param.setFileMd5(fileMd5);
        param.setShardingNum(n);
        param.setOriginalFileName(originalFileName);
        param.setMediaType(m);
        param.setCreateUserId(useUserId);
        byte[] total = f.getBytes();
        f.getInputStream().close();//读取完字节后,将流关闭掉
        param.setStream(total);
        param.setType(originalFileName.lastIndexOf(".") != -1 ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "");
        int sort = Integer.parseInt(f.getOriginalFilename().lastIndexOf("-") != -1 ? f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf("-") + 1) : "0");
        param.setShardingSort(sort);
        FileSourceENUM source = FileSourceENUM.valueOf(s);

        //尝试是否可以从垃圾箱恢复
        if(fileAdministration.dustbinRecovery(param.getFileMd5(),useSpaceId,source).isResult()){
            return new ResponseHeadVO<>(true,param.getFileMd5(),"已经从垃圾箱恢复文件,不需要重复上传.");
        }
        //是否还存的下
        DataSize ds = DataSize.ofBytes(param.getTotalSize());
        ResponseHeadDTO use = userSpace.useSpaceByte(useSpaceId,ds.toMegabytes(),true,false);
        if(!use.isResult()){
            return new ResponseHeadVO(false,"用户存储空间不足");
        }
        //尝试是否可以直接引用其它人上传的文件
        if(fileAdministration.checkDuplicate(originalFileName,param.getType(),m,
                param.getFileMd5(),useSpaceId,source,param.getTotalSize(),useUserId).isResult()){
            return new ResponseHeadVO<>(true,param.getFileMd5(),"上传成功,触发了秒传");//second 触发了秒传的标记
        }
        ResponseHeadDTO<String> result = fileService.writeByte(param);//如果完整文件合并写盘成功,就会在data字段返回 md5值,否则只是分片写盘成功
        return new ResponseHeadVO<>(result.isResult(),result.getMessage());
    }
}
