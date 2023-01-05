package com.jflove.vo.netdisk;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tanjun
 * @date 2022/12/16 15:52
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("添加目录参数")
public class AddDirectoryParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5099078248599225553L;

    @ApiModelProperty(value="主键")
    private long id;

    @ApiModelProperty(value="类型(FOLDER=文件夹,FILE=文件)")
    @NotBlank(message = "目录类型不能为空")
    private String type;

    @ApiModelProperty(value="文件md5值")
    private String fileMd5;

    @ApiModelProperty(value="目录上级ID")
    @NotNull(message = "目录上级ID不能为空")
    private long pid;

    @ApiModelProperty(value="所属空间ID")
    private long spaceId;

    @ApiModelProperty(value="创建时间")
    private Date createTime;

    @ApiModelProperty(value="修改时间")
    private Date updateTime;

    @ApiModelProperty(value="目录名称")
    @NotBlank(message = "目录名称不能为空")
    private String name;

    @ApiModelProperty(value="文件多媒体类型")
    private String mediaType;
}
