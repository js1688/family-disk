package com.jflove.controller.journal;

import com.jflove.ResponseHeadDTO;
import com.jflove.config.HttpConstantConfig;
import com.jflove.journal.api.IJournalList;
import com.jflove.journal.dto.JournalListDTO;
import com.jflove.journal.dto.JournalListFilesDTO;
import com.jflove.user.em.UserSpaceRoleENUM;
import com.jflove.vo.ResponseHeadVO;
import com.jflove.vo.journal.DelJournalListParamVO;
import com.jflove.vo.journal.GetJournalListParamVO;
import com.jflove.vo.journal.JournalListFilesVO;
import com.jflove.vo.journal.JournalListVO;
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
@RequestMapping("/journal")
@Api(tags = "日记")
@Log4j2
public class JournalListController {

    @DubboReference
    private IJournalList journalList;

    @Autowired
    private HttpServletRequest autowiredRequest;


    @ApiOperation(value = "查询日记")
    @PostMapping("/getJournalList")
    public ResponseHeadVO<JournalListVO> getJournalList(@RequestBody @Valid GetJournalListParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        ResponseHeadDTO<JournalListDTO> dto = journalList.getList(useSpaceId,param.getKeyword());
        if(dto.isResult()){
            List<JournalListVO> vos = new ArrayList<>(dto.getDatas().size());
            dto.getDatas().forEach(v->{
                JournalListVO vo = new JournalListVO();
                BeanUtils.copyProperties(v,vo);
                if(v.getFiles() != null){
                    List<JournalListFilesVO> filesVOS = new ArrayList<>(v.getFiles().size());
                    v.getFiles().forEach(v2->{
                        JournalListFilesVO fv = new JournalListFilesVO();
                        BeanUtils.copyProperties(v2,fv);
                        filesVOS.add(fv);
                    });
                    vo.setFiles(filesVOS);
                }
                vos.add(vo);
            });
            return new ResponseHeadVO<>(dto.isResult(),vos,dto.getMessage());
        }
        return new ResponseHeadVO<>(dto.isResult(),dto.getMessage());
    }

    @ApiOperation(value = "删除日记")
    @PostMapping("/delJournalList")
    public ResponseHeadVO<Integer> delJournalList(@RequestBody @Valid DelJournalListParamVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有删除权限");
        }
        ResponseHeadDTO dto = journalList.del(useSpaceId,param.getId());
        ResponseHeadVO retVo = new ResponseHeadVO();
        BeanUtils.copyProperties(dto,retVo);
        return retVo;
    }

    @ApiOperation(value = "添加日记")
    @PostMapping("/addJournalList")
    public ResponseHeadVO addJournalList(@RequestBody @Valid JournalListVO param){
        Long useSpaceId = (Long)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ID);
        UserSpaceRoleENUM useSpacerRole = (UserSpaceRoleENUM)autowiredRequest.getAttribute(HttpConstantConfig.USE_SPACE_ROLE);
        Assert.notNull(useSpaceId,"错误的请求:正在使用的空间ID不能为空");
        Assert.notNull(useSpacerRole,"错误的请求:正在使用的空间权限不能为空");
        if(useSpacerRole != UserSpaceRoleENUM.WRITE){
            throw new SecurityException("用户对该空间没有添加权限");
        }
        JournalListDTO dto = new JournalListDTO();
        BeanUtils.copyProperties(param,dto);
        if(param.getFiles() != null){
            List<JournalListFilesDTO> filesDTOS = new ArrayList<>(param.getFiles().size());
            param.getFiles().forEach(v->{
                JournalListFilesDTO fd = new JournalListFilesDTO();
                BeanUtils.copyProperties(v,fd);
                filesDTOS.add(fd);
            });
            dto.setFiles(filesDTOS);
        }
        dto.setSpaceId(useSpaceId);
        dto.setId(0);
        dto.setCreateUserId(useSpaceId);
        ResponseHeadDTO<Long> retDto = journalList.add(dto);
        ResponseHeadVO<Long> retVo = new ResponseHeadVO<Long>();
        BeanUtils.copyProperties(retDto,retVo);
        return retVo;
    }
}
