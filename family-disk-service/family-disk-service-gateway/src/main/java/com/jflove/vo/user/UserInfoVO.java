package com.jflove.vo.user;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tanjun
 * @date 2022/12/8 15:59
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("用户信息")
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 514153394574279630L;

    @ApiModelProperty(value="主键")
    private long id;
    @ApiModelProperty(value="用户邮箱")
    private String email;
    @ApiModelProperty(value="用户名称")
    private String name;
    @DateTimeFormat(pattern=DatePattern.NORM_DATETIME_PATTERN)//主要是限制前台的时间格式
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN,timezone = "GMT+8")//主要是控制后台到前台的时间格式
    @ApiModelProperty(value="创建日期")
    private Date createTime;
    @DateTimeFormat(pattern= DatePattern.NORM_DATETIME_PATTERN)//主要是限制前台的时间格式
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN,timezone = "GMT+8")//主要是控制后台到前台的时间格式
    @ApiModelProperty(value="修改日期")
    private Date updateTime;
}
