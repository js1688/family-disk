package com.jflove.gateway.vo;

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
 * @date 2022/12/8 15:03
 * @describe 统一的响应类泛型
 */
@Getter
@Setter
@ToString
@ApiModel(value="统一的响应对象")
public class ResponseHeadVO<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 4433817472458651366L;

    public ResponseHeadVO() {
    }

    public ResponseHeadVO(T data) {
        this.data = data;
    }

    public ResponseHeadVO(List<T> datas) {
        this.datas = datas;
    }

    public ResponseHeadVO(String message) {
        this.message = message;
    }

    public ResponseHeadVO(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public ResponseHeadVO(boolean result, T data, String message) {
        this.result = result;
        this.data = data;
        this.message = message;
    }

    public ResponseHeadVO(boolean result, List<T> datas, String message) {
        this.result = result;
        this.datas = datas;
        this.message = message;
    }

    @ApiModelProperty(value="处理结果")
    private boolean result = true;

    @ApiModelProperty(value="响应数据(单条)")
    private T data;

    @ApiModelProperty(value="响应数据(多条)")
    private List<T> datas;

    @ApiModelProperty(value="响应的提示消息")
    private String message;
}
