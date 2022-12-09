package com.jflove.email.dto;

import com.jflove.email.em.EmailSubjectCodeENUM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author tanjun
 * @date 2022/12/9 9:54
 * @describe
 */
@Getter
@Setter
@ToString
public class EmailDetailsDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5624105615008875258L;

    private String recipient;//接受者
    private String msgBody;//内容
    private EmailSubjectCodeENUM subject;//主题
    private String attachment;//附件,文件地址
}
