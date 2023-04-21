package com.jflove.stream.api;

import com.jflove.ResponseHeadDTO;
import com.jflove.stream.dto.StreamReadParamDTO;
import com.jflove.stream.dto.StreamReadResultDTO;
import com.jflove.stream.dto.StreamWriteParamDTO;
import com.jflove.stream.dto.StreamWriteResultDTO;

/**
 * @author tanjun
 * @date 2022/12/12 15:17
 * @describe 文件流读写
 */
public interface IFileStreamService {

    /**
     * 读取文件
     * 分片方式
     * @param dto
     * @return
     */
    ResponseHeadDTO<StreamReadResultDTO> readByte(StreamReadParamDTO dto);

    /**
     * 写入文件
     * 分片方式
     * @param dto
     * @return
     */
    ResponseHeadDTO<StreamWriteResultDTO> writeByte(StreamWriteParamDTO dto);
}
