package com.jflove.stream.service;

import com.jflove.ResponseHeadDTO;
import com.jflove.po.file.FileDiskConfigPO;
import com.jflove.stream.dto.StreamReadParamDTO;
import com.jflove.stream.dto.StreamReadResultDTO;
import com.jflove.stream.dto.StreamWriteParamDTO;

/**
 * @author tanjun
 * @date 2022/12/14 18:33
 * @describe 文件读写标准类
 */
public interface IFileReadAndWrit {

    String BEAN_PREFIX = "FileReadAndWrit_";

    /**
     * 将存放在临时目录的文件各分片合并成一个文件,写入到磁盘
     * @param dto
     * @param selectd
     * @return
     */
    ResponseHeadDTO<String> writByte(StreamWriteParamDTO dto, FileDiskConfigPO selectd);

    /**
     * 按指定位置读取文件分片字节
     * @param param
     * @param selectd
     */
    ResponseHeadDTO<StreamReadResultDTO> readByte(StreamReadParamDTO param, FileDiskConfigPO selectd);
}
