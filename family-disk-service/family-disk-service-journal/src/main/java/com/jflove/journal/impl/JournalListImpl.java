package com.jflove.journal.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.api.IFileAdministration;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.journal.JournalListFilesPO;
import com.jflove.journal.JournalListPO;
import com.jflove.journal.api.IJournalList;
import com.jflove.journal.dto.JournalListDTO;
import com.jflove.journal.dto.JournalListFilesDTO;
import com.jflove.journal.mapper.JournalListFilesMapper;
import com.jflove.journal.mapper.JournalListMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/1/17 9:51 AM
 * @desc:
 */
@DubboService
@Log4j2
public class JournalListImpl implements IJournalList {

    @Autowired
    private JournalListMapper journalListMapper;
    @Autowired
    private JournalListFilesMapper journalListFilesMapper;
    @DubboReference
    private IFileAdministration fileAdministration;

    @Override
    public ResponseHeadDTO<JournalListDTO> getList(long spaceId, String keyword) {
        String date = null;
        String title = null;
        //判断关键字是筛选日期,还是标题
        if(StringUtils.hasLength(keyword)){
            try {
                DateUtil.parse(keyword, DatePattern.NORM_DATE_PATTERN);
                date = keyword;
            }catch (Exception e){
                title = keyword;
            }
        }
        List<JournalListPO> listPO = journalListMapper.selectList(new LambdaQueryWrapper<JournalListPO>()
                .eq(JournalListPO::getSpaceId,spaceId)
                .eq(date != null,JournalListPO::getHappenTime,date)
                .like(title != null,JournalListPO::getTitle,title)
                .orderByDesc(JournalListPO::getHappenTime)
        );
        List<JournalListDTO> dtoList = new ArrayList<>(listPO.size());
        listPO.forEach(v->{
            JournalListDTO listDTO = new JournalListDTO();
            BeanUtils.copyProperties(v,listDTO);
            List<JournalListFilesPO> filesPO = journalListFilesMapper.selectList(new LambdaQueryWrapper<JournalListFilesPO>()
                    .eq(JournalListFilesPO::getJournalId,v.getId())
            );
            List<JournalListFilesDTO> filesDTOS = new ArrayList<>(filesPO.size());
            filesPO.forEach(v2->{
                JournalListFilesDTO filesDTO = new JournalListFilesDTO();
                BeanUtils.copyProperties(v2,filesDTO);
                filesDTOS.add(filesDTO);
            });
            listDTO.setFiles(filesDTOS);
            dtoList.add(listDTO);
        });
        return new ResponseHeadDTO<>(true,dtoList,"查询成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO<Long> add(JournalListDTO dto) {
        if(!StringUtils.hasLength(dto.getTitle()) && !StringUtils.hasLength(dto.getBody())){
            return new ResponseHeadDTO(false,"日记标题和内容不能同时为空");
        }else if(!StringUtils.hasLength(dto.getTitle()) && StringUtils.hasLength(dto.getBody())){
            //日记标题为空,则最长取日记内容的前10个字当做标题
            String title = dto.getBody().substring(0,dto.getBody().length() < 10 ? dto.getBody().length() : 10);
            dto.setTitle(title);
        }
        JournalListPO listPO = new JournalListPO();
        BeanUtils.copyProperties(dto,listPO);
        journalListMapper.insert(listPO);
        if(dto.getFiles() != null){
            dto.getFiles().forEach(v->{
                JournalListFilesPO filesPO = new JournalListFilesPO();
                BeanUtils.copyProperties(v,filesPO);
                filesPO.setJournalId(listPO.getId());
                journalListFilesMapper.insert(filesPO);
            });
        }
        return new ResponseHeadDTO<>(true,listPO.getId(),"记录日记成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO del(long spaceId, long id) {
        JournalListPO listPO = journalListMapper.selectOne(new LambdaQueryWrapper<JournalListPO>()
                .eq(JournalListPO::getSpaceId,spaceId)
                .eq(JournalListPO::getId,id)
        );
        if(listPO == null){
            return new ResponseHeadDTO(false,"删除失败,没有这个日记");
        }

        List<JournalListFilesPO> filesPOList = journalListFilesMapper.selectList(new LambdaQueryWrapper<JournalListFilesPO>()
                .eq(JournalListFilesPO::getJournalId,listPO.getId())
        );

        filesPOList.forEach(v->{
            fileAdministration.delFile(v.getFileMd5(),spaceId, FileSourceENUM.JOURNAL);
        });
        journalListFilesMapper.delete(new LambdaQueryWrapper<JournalListFilesPO>()
                .eq(JournalListFilesPO::getJournalId,listPO.getId())
        );
        journalListMapper.deleteById(listPO.getId());
        return new ResponseHeadDTO(true,"删除日记成功");
    }
}
