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
 * @date 2023/1/3 17:56
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("文件下载参数")
public class GetFileParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2943243016167301157L;
    @ApiModelProperty(value="文件md5值")
    @NotBlank(message = "文件md5值不能为空")
    private String fileMd5;

    @ApiModelProperty(value="文件来源(NOTEPAD=记事本,CLOUDDISK=云盘,JOURNAL=日记)")
    @NotBlank(message = "文件来源不能为空")
    private String source;

    @ApiModelProperty(value="文件名")
    @NotBlank(message = "文件名称不能为空")
    private String name;
}
