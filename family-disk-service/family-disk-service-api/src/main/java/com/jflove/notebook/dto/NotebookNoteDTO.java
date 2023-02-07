package com.jflove.notebook.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author: tanjun
 * @date: 2023/2/6 4:53 PM
 * @desc:
 */
@Getter
@Setter
@ToString
public class NotebookNoteDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 69234793038315036L;

    /**
     * null
     */
    private long id;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 所属空间id
     */
    private long spaceId;
    /**
     * markdown内容
     */
    private String text;
    /**
     * markdown生成的html预览
     */
    private String html;
    /**
     * 关键字,截取markdown前部分
     */
    private String keyword;
    /**
     * 标签
     */
    private long tag;

    /**
     * 创建用户id
     */
    private long createUserId;
}
