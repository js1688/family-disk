package com.jflove.webdav.factory;

import com.jflove.ResponseHeadDTO;
import com.jflove.file.api.IFileAdministration;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.netdisk.api.INetdiskDirectory;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.stream.api.IFileStreamService;
import com.jflove.stream.dto.StreamReadParamDTO;
import com.jflove.stream.dto.StreamReadResultDTO;
import com.jflove.user.api.IUserInfo;
import com.jflove.user.api.IUserSpace;
import com.jflove.user.dto.UserInfoDTO;
import com.jflove.user.dto.UserSpaceDTO;
import com.jflove.webdav.vo.FolderVO;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author: tanjun
 * @date: 2023/9/21 2:39 PM
 * @desc:
 */
@Component
@CacheConfig
@Log4j2
@Getter
public class ManageFactory {

    @DubboReference
    private IUserInfo userInfo;
    @DubboReference
    private IUserSpace userSpace;

    @DubboReference
    private INetdiskDirectory netdiskDirectory;

    @DubboReference
    private IFileStreamService fileService;

    @DubboReference
    private IFileAdministration fileAdministration;

    /**
     * 读取文件
     * @param spaceId
     * @param start
     * @param len
     * @param md5
     * @return
     */
    public ResponseHeadDTO<StreamReadResultDTO> readFile(long spaceId,long start,long len, String md5){
        StreamReadParamDTO param = new StreamReadParamDTO();
        param.setFileMd5(md5);
        param.setReadLength(len);
        param.setRangeStart(start);
        param.setSource(FileSourceENUM.CLOUDDISK);
        param.setSpaceId(spaceId);
        ResponseHeadDTO<StreamReadResultDTO> result = fileService.readByte(param);
        return result;
    }

    /**
     * 获取用户信息
     * @param email
     * @return
     */
    @Cacheable(value = {"def"},key = "#email.concat('getUserInfoByEmail')")
    public ResponseHeadDTO<UserInfoDTO> getUserInfoByEmail(String email){
        return userInfo.getUserInfoByEmail(email);
    }

    /**
     * 根据访问url查询到末尾的文件信息
     * @param spaceId
     * @param url
     * @param spaceCode
     * @return
     */
    public ResponseHeadDTO<NetdiskDirectoryDTO> getDirectoryByUrl(long spaceId,String url,String spaceCode){
        if("/".equals(url)){
            NetdiskDirectoryDTO dto = new NetdiskDirectoryDTO();
            dto.setType(NetdiskDirectoryENUM.FOLDER);
            dto.setName(spaceCode);
            return new ResponseHeadDTO<>(dto);
        }
        return netdiskDirectory.findLastDirectoryByUrl(spaceId,url);
    }

    /**
     * 查找子目录
     * @param spaceId
     * @param folder
     * @return
     */
    public ResponseHeadDTO<NetdiskDirectoryDTO> getChildren(long spaceId, FolderVO folder){
        ResponseHeadDTO<NetdiskDirectoryDTO> dto = netdiskDirectory.findDirectory(spaceId,folder.getId(),null,null);
        return dto;
    }

    /**
     * 根据空间编码查询空间信息
     * @param code
     * @return
     */
    @Cacheable(value = {"def"},key = "#code.concat('getSpaceByCode')")
    public ResponseHeadDTO<UserSpaceDTO> getSpaceByCode(String code){
        ResponseHeadDTO<UserSpaceDTO> dto = userSpace.getSpaceByCode(code);
        return dto;
    }


}
