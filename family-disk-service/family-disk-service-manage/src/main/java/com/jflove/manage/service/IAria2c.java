package com.jflove.manage.service;

import cn.hutool.json.JSONArray;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/7/20 3:30 PM
 * @desc: aria2方式下载
 */
public interface IAria2c {

    /**
     * 下载接口标准
     * @param uri
     * @return gid
     */
    String addUri(String uri);

    /**
     * 下载状态
     * @param gid
     * @return
     */
    Map tellStatus(String gid);

    /**
     * 下载列表
     * @param gid
     * @return
     */
    List getFiles(String gid);

    /**
     * 删除一个任务
     * @param gid
     * @return
     */
    String remove(String gid);

    /**
     * 暂停任务
     * @param gid
     * @return
     */
    String pause(String gid);

    /**
     * 取消暂停
     * @param gid
     * @return
     */
    String unpause(String gid);

    /**
     * 返回rpc接口发送时的参数
     * @param token 身份验证,占第0个位置,是身份验证
     * @param param 占第1个位置,是具体的参数
     * @return
     */
    default Object [] rpcParam(String token,Object param){
        JSONArray array = new JSONArray();
        if(StringUtils.hasLength(token)) {
            array.put("token:" + token);
        }
        if(param != null) {
            array.put(param);
        }
        return array.stream().toArray();
    }
}
