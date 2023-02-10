package com.jflove.vo.share;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
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
@ApiModel("获取笔记分享内容参数")
public class NoteShareGetBodyParamVO implements Serializable {


    @Serial
    private static final long serialVersionUID = -7303752074336942264L;
    @ApiModelProperty(value="链接id")
    @NotBlank(message = "链接id不能为空")
    private String uuid;

    @ApiModelProperty(value="解锁密码")
    @NotBlank(message = "解锁密码不能为空")
    private String password;
}