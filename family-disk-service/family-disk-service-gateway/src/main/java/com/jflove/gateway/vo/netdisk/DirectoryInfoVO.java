package com.jflove.gateway.vo.netdisk;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author tanjun
 * @date 2022/12/16 15:52
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("目录信息返回")
public class DirectoryInfoVO implements Serializable {


    @Serial
    private static final long serialVersionUID = -3667386903604708546L;
    @ApiModelProperty(value="主键")
    private long id;

    @ApiModelProperty(value="类型(FOLDER=文件夹,FILE=文件)")
    private String type;

    @ApiModelProperty(value="文件md5值")
    private String fileMd5;

    @ApiModelProperty(value="目录上级ID")
    private long pid;

    @ApiModelProperty(value="所属空间ID")
    private long spaceId;

    @ApiModelProperty(value="创建时间")
    private Date createTime;

    @ApiModelProperty(value="修改时间")
    private Date updateTime;

    @ApiModelProperty(value="目录名称")
    private String name;

    @ApiModelProperty(value="文件多媒体类型")
    private String mediaType;

    @ApiModelProperty(value="子目录")
    private List<DirectoryInfoVO> children;
    
    @ApiModelProperty(value="文件占用大小(MB)")
    private String size;
}
