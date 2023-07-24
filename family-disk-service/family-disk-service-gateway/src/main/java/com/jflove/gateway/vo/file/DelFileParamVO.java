package com.jflove.gateway.vo.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/16 14:21
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("文件删除参数")
public class DelFileParamVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8070172221024252563L;

    @ApiModelProperty(value="文件md5值")
    @NotBlank(message = "文件md5值不能为空")
    private String fileMd5;

    @ApiModelProperty(value="文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,JOURNAL=日记)")
    @NotBlank(message = "文件来源不能为空")
    private String source;

}
