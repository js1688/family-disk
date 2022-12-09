package com.jflove;

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
public class ResponseHeadDTO<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 4433817472458651366L;

    public ResponseHeadDTO() {
    }

    public ResponseHeadDTO(T data) {
        this.data = data;
    }

    public ResponseHeadDTO(List<T> datas) {
        this.datas = datas;
    }

    public ResponseHeadDTO(String message) {
        this.message = message;
    }

    public ResponseHeadDTO(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public ResponseHeadDTO(boolean result, T data, String message) {
        this.result = result;
        this.data = data;
        this.message = message;
    }

    public ResponseHeadDTO(boolean result, List<T> datas, String message) {
        this.result = result;
        this.datas = datas;
        this.message = message;
    }

    private boolean result = true;

    private T data;

    private List<T> datas;

    private String message;
}
