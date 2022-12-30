package com.jflove.file.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/30 17:33
 * @describe 文件传输响应
 */
@Getter
@Setter
@ToString
public class FileTransmissionRepDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4617833218305094132L;

    public FileTransmissionRepDTO() {
    }

    public FileTransmissionRepDTO(String fileName, String fileMd5, boolean result, String message) {
        this.fileName = fileName;
        this.fileMd5 = fileMd5;
        this.result = result;
        this.message = message;
    }

    private String fileName;

    private String fileMd5;

    private boolean result = true;

    private String message;

}
