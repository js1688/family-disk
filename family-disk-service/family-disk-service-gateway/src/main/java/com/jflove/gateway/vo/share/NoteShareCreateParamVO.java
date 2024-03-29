package com.jflove.gateway.vo.share;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author: tanjun
 * @date: 2023/2/10 9:38 AM
 * @desc:
 */
@Getter
@Setter
@ToString
@ApiModel("笔记创建分享参数")
public class NoteShareCreateParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 907916017650182397L;

    @ApiModelProperty(value="内容id")
    @NotNull(message = "内容id不能为空")
    private Long bodyId;

    @ApiModelProperty(value="解锁密码")
    private String password;

    @ApiModelProperty(value="失效时间(yyyy-MM-dd HH:mm:ss)")
    @NotBlank(message = "失效时间不能为空")
    private String invalidTime;

    @ApiModelProperty(value="内容类型")
    @NotBlank(message = "内容类型不能为空")
    private String bodyType;
}