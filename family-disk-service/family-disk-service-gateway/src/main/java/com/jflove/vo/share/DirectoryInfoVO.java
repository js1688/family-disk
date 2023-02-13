package com.jflove.vo.share;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/2/13 5:33 PM
 * @desc:
 */
@Getter
@Setter
@ToString
@ApiModel("网盘分享目录信息")
public class DirectoryInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2919425396934084106L;

    @ApiModelProperty(value="主键")
    private long id;

    @ApiModelProperty(value="类型(FOLDER=文件夹,FILE=文件)")
    private String type;

    @ApiModelProperty(value="文件md5值")
    private String fileMd5;

    @ApiModelProperty(value="目录上级ID")
    @NotNull(message = "目录上级ID不能为空")
    private long pid;

    @ApiModelProperty(value="所属空间ID")
    private long spaceId;

    @ApiModelProperty(value="目录名称")
    private String name;

    @ApiModelProperty(value="文件多媒体类型")
    private String mediaType;

    @ApiModelProperty(value="子目录")
    private List<DirectoryInfoVO> child;
}
