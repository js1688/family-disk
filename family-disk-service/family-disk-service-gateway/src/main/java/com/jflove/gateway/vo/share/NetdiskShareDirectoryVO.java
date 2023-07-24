package com.jflove.gateway.vo.share;

import com.jflove.gateway.vo.netdisk.DirectoryInfoVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


/**
 * @author tanjun
 * @date Thu Feb 09 17:43:13 CST 2023
 * @describe
 */
@Getter
@Setter
@ToString
@ApiModel("网盘分享目录列表")
public class NetdiskShareDirectoryVO implements Serializable{


  @Serial
  private static final long serialVersionUID = -9045926284625734281L;

  /**
   * 签发的临时token,控制有效时长,将可读取的文件写入到负载信息中以便校验文件权限范围
   */
  @ApiModelProperty(value="临时token")
  private String tempToken;

  @ApiModelProperty(value="网盘目录")
  private List<DirectoryInfoVO> list;
}
