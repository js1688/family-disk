package com.jflove.manage.config;

import com.googlecode.jsonrpc4j.IJsonRpcClient;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/7/7 4:40 PM
 * @desc: aria2c 下载服务配置
 */
@Component
@Log4j2
@Getter
public class Aria2cJsonRpcConfig implements IJsonRpcClient {
    @Value("${aria2c.service.url:http://localhost:6800/jsonrpc}")
    private String aria2cUrl;
    @Value("${aria2c.service.token}")
    private String token;

    private JsonRpcHttpClient jsonRpcHttpClient;

    @PostConstruct
    public void init(){
        try {
            Map<String,String> headers = new HashMap<>();
            jsonRpcHttpClient = new JsonRpcHttpClient(new URL(aria2cUrl), headers);
        }catch (Exception e){
            log.error("初始化aria2c客户端发生异常",e);
        }
    }

    @Override
    public void invoke(String s, Object o) throws Throwable {
        jsonRpcHttpClient.invoke(s,o);
    }

    @Override
    public Object invoke(String s, Object o, Type type) throws Throwable {
        return jsonRpcHttpClient.invoke(s,o,type);
    }

    @Override
    public Object invoke(String s, Object o, Type type, Map<String, String> map) throws Throwable {
        return jsonRpcHttpClient.invoke(s,o,type,map);
    }

    @Override
    public <T> T invoke(String s, Object o, Class<T> aClass) throws Throwable {
        return jsonRpcHttpClient.invoke(s,o,aClass);
    }

    @Override
    public <T> T invoke(String s, Object o, Class<T> aClass, Map<String, String> map) throws Throwable {
        return jsonRpcHttpClient.invoke(s,o,aClass,map);
    }
}
