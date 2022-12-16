package com.jflove.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tanjun
 * @date 2022/12/14 11:13
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("空间信息")
public class UserSpaceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8095270169054895952L;

    @ApiModelProperty(value="主键")
    private long id;

    @ApiModelProperty(value="最大空间大小(MB)")
    private long maxSize;

    @ApiModelProperty(value="空间编码")
    private String code;

    @ApiModelProperty(value="已使用空间大小(MB)")
    private long useSize;

    @ApiModelProperty(value="空间主题")
    private String title;

    @ApiModelProperty(value="空间创建用户id")
    private long createUserId;

    @ApiModelProperty(value="创建时间")
    private Date createTime;

    @ApiModelProperty(value="修改时间")
    private Date updateTime;
}
