package com.jflove.share.dto;

import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/2/13 6:06 PM
 * @desc:
 */
@Getter
@Setter
@ToString
public class NetdiskShareDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7385742788364969509L;
    /**
     * 到期时间戳,10位
     */
    private long invalidTime;

    /**
     * 目录
     */
    List<NetdiskDirectoryDTO> list;
}
