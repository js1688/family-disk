package com.jflove.manage.service.impl;

import com.jflove.manage.config.Aria2cJsonRpcConfig;
import com.jflove.manage.service.IAria2c;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/7/20 3:33 PM
 * @desc: 普通的下载(HTTP/FTP/SFTP/BitTorrent)
 */
@Service("aria2cSimple")
@Log4j2
public class Aria2cSimpleImpl implements IAria2c {

    @Autowired
    private Aria2cJsonRpcConfig jsonRpcHttpClient;


    @Override
    public String pause(String gid) {
        try {
            String retGid = jsonRpcHttpClient.invoke("aria2.pause", rpcParam(jsonRpcHttpClient.getToken(),gid), String.class);
            return retGid;
        }catch (Throwable e){
            log.error("aria2cSimple 暂停下载任务异常",e);
        }
        return null;
    }

    @Override
    public String unpause(String gid) {
        try {
            String retGid = jsonRpcHttpClient.invoke("aria2.unpause", rpcParam(jsonRpcHttpClient.getToken(),gid), String.class);
            return retGid;
        }catch (Throwable e){
            log.error("aria2cSimple 取消暂停下载任务异常",e);
        }
        return null;
    }

    @Override
    public String addUri(String uri){
        try {
            String gid = jsonRpcHttpClient.invoke("aria2.addUri", rpcParam(jsonRpcHttpClient.getToken(),new String[]{uri}), String.class);
            return gid;
        }catch (Throwable e){
            log.error("aria2cSimple 添加下载任务异常",e);
        }
        return null;
    }

    @Override
    public String remove(String gid) {
        try {
            String retGid = jsonRpcHttpClient.invoke("aria2.remove", rpcParam(jsonRpcHttpClient.getToken(),gid), String.class);
            return retGid;
        }catch (Throwable e){
            log.error("aria2cSimple 删除下载任务异常",e);
        }
        return null;
    }

    @Override
    public Map tellStatus(String gid) {
        try {
            Map result = jsonRpcHttpClient.invoke("aria2.tellStatus", rpcParam(jsonRpcHttpClient.getToken(),gid), Map.class);
            return result;
        }catch (Throwable e){
            log.error("aria2cSimple 查询下载任务异常",e);
        }
        return null;
    }

    @Override
    public List getFiles(String gid) {
        try {
            List result = jsonRpcHttpClient.invoke("aria2.getFiles", rpcParam(jsonRpcHttpClient.getToken(),gid), List.class);
            return result;
        }catch (Throwable e){
            log.error("aria2cSimple 查询下载任务列表异常",e);
        }
        return null;
    }
}
