package com.jflove.stream.service;

/**
 * @author: tanjun
 * @date: 2023/1/13 9:25 AM
 * @desc: 文件清理,清理那些临时文件和已经没有关联的文件
 */
public interface IFileClear {

    /**
     * 清理临时目录中的临时文件
     */
    void clearTemp();

    /**
     * 清理回收站
     */
    void clearDustbin();
}
