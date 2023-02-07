package com.jflove.controller.notebook;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.notebook.api.INoteService;
import com.jflove.notebook.dto.NotebookNoteDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.notebook.GetListParamVO;
import com.jflove.vo.notebook.NotebookListVO;
import com.jflove.vo.notebook.NotebookNoteVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/1/17 10:12 AM
 * @desc:
 */
@RestController
@RequestMapping("/notebook")
@Api(tags = "备忘录")
@Log4j2
public class NoteBookController {

    @DubboReference
    private INoteService noteService;

    @Autowired
    private HttpServletRequest autowiredRequest;


    @ApiOperation(value = "查询备忘录列表")
    @PostMapping("/getList")
    public ResponseHeadVO<NotebookListVO> getList(@RequestBody @Valid GetListParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        ResponseHeadDTO<NotebookNoteDTO> dto = noteService.getList(useSpaceId,param.getKeyword(),param.getTag());
        if(dto.isResult()){
            List<NotebookListVO> vos = new ArrayList<>(dto.getDatas().size());
            dto.getDatas().forEach(v->{
                NotebookListVO vo = new NotebookListVO();
                BeanUtils.copyProperties(v,vo);
                vo.setType(param.getType());
                vos.add(vo);
            });
            return new ResponseHeadVO<>(dto.isResult(),vos,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "保存笔记")
    @PostMapping("/saveNote")
    public ResponseHeadVO saveNote(@RequestBody @Valid NotebookNoteVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Long useUserId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_USER_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有添加权限");
        }
        if(param.getTag() == 0){
           throw new NullPointerException("标签不能为空");
        }
        NotebookNoteDTO dto = new NotebookNoteDTO();
        BeanUtils.copyProperties(param,dto);
        dto.setSpaceId(useSpaceId);
        dto.setId(param.getId());
        dto.setCreateUserId(useUserId);
        ResponseHeadDTO<Long> retDto = noteService.saveNote(dto);
        ResponseHeadVO<Long> retVo = new ResponseHeadVO<Long>();
        BeanUtils.copyProperties(retDto,retVo);
        return retVo;
    }
}
