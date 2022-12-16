package com.jflove.netdisk.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;

/**
 * @author tanjun
 * @date 2022/12/16 14:32
 * @describe 网盘目录
 */
public interface INetdiskDirectory {

    /**
     * 添加目录
     * @param dto
     * @return
     */
    ResponseHeadDTO<NetdiskDirectoryDTO> addDirectory(NetdiskDirectoryDTO dto);

    /**
     * 删除目录
     * @param spaceId
     * @param dirId
     * @return
     */
    ResponseHeadDTO<Integer> delDirectory(Long spaceId,Long dirId);

    /**
     * 将目录移动到另一个目录下
     * @param spaceId
     * @param dirId
     * @param targetDirId
     * @return
     */
    ResponseHeadDTO<NetdiskDirectoryDTO> moveDirectory(Long spaceId,Long dirId,Long targetDirId);
}
