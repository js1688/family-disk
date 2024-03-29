package com.jflove.android.api;

import android.view.View;

import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * @author: tanjun
 * @date: 2023/11/22 2:16 PM
 * @desc: 访问后端接口
 */
public interface HttpApi {
    /**
     * 后端 接口服务地址
     */
    String API_HOST = "https://api.jflove.cn";

    /**
     * 网页版主站地址
     */
    String WWW_HOST = "https://www.jflove.cn";

    /**
     * 注册地址
     */
    String REGISTER_URL = "https://m.jflove.cn";
    /**
     * 登录地址
     */
    String LOGON_URL = API_HOST + "/user/info/emailPasswordLogin";
    /**
     * 获取当前登录账号的用户信息
     */
    String GET_USERINFO_URL = API_HOST + "/user/info/getUserInfo";
    /**
     * 获取分享列表
     */
    String GET_SHARELINK_URL = API_HOST + "/share/admin/getLinkList";
    /**
     * 删除分享链接
     */
    String DEL_SHARELINK_URL = API_HOST + "/share/admin/delLink";
    /**
     * 查找用户创建的空间下有多少关联用户
     */
    String GET_I_SPACE_USER = API_HOST + "/user/space/getUserInfoBySpaceId";
    /**
     * 切换空间
     */
    String SWITCH_SPACE = API_HOST + "/user/space/switchSpace";

    /**
     * 退出空间
     */
    String EXIT_SPACE_REL = API_HOST + "/user/space/exitRel";




    /**
     * 调用接口后的回调处理
     * @param response
     */
    void callback(JSONObject response);

    /**
     * 发送post请求,会自动附带token,如果有的话
     * 安卓要求不能在主线程中访问Http,防止主线程UI卡住
     * @param url
     * @param param
     * @param view 回调方法在哪个页面执行,主要目的是将http响应结果放到主线程去执行
     * @return
     */
    default void post(String url, JSON param, View view){
        //todo 创建一个新的线程执行 http,有响应结果了,再调用主线程处理结果,  后面考虑使用线程池, 因为这个app调用http比较频繁
        new Thread(() -> {
            String token = SettingsStorageApi.get(SettingsStorageApi.Authorization);
            HttpResponse response = HttpRequest.post(url)
                    .header(Header.CONTENT_TYPE,"application/json")
                    .header(Header.AUTHORIZATION,token)
                    .body(param.toString())
                    .execute();
            JSONObject jo = JSONUtil.parseObj(response.body());
            view.post(() -> callback(jo));
        }).start();
    }
    /**
     * 发送get请求,会自动附带token,如果有的话
     * 安卓要求不能在主线程中访问Http,防止主线程UI卡住
     * @param url
     * @param view 回调方法在哪个页面执行,主要目的是将http响应结果放到主线程去执行
     * @return
     */
    default void get(String url,View view){
        //todo 创建一个新的线程执行 http,有响应结果了,再调用主线程处理结果,  后面考虑使用线程池, 因为这个app调用http比较频繁
        new Thread(() -> {
            String token = SettingsStorageApi.get(SettingsStorageApi.Authorization);
            HttpResponse response = HttpRequest.get(url)
                    .header(Header.CONTENT_TYPE,"application/json")
                    .header(Header.AUTHORIZATION,token)
                    .execute();
            JSONObject jo = JSONUtil.parseObj(response.body());
            view.post(() -> callback(jo));
        }).start();
    }

    /**
     * 获取用户信息并刷新缓存,
     * 因为这个接口比较常用,所以放在这里
     * @param view
     */
    default void getUserInfo(View view){
        //todo 创建一个新的线程执行 http,有响应结果了,再调用主线程处理结果,  后面考虑使用线程池, 因为这个app调用http比较频繁
        new Thread(() -> {
            String token = SettingsStorageApi.get(SettingsStorageApi.Authorization);
            HttpResponse response = HttpRequest.get(GET_USERINFO_URL)
                    .header(Header.CONTENT_TYPE,"application/json")
                    .header(Header.AUTHORIZATION,token)
                    .execute();
            JSONObject jo = JSONUtil.parseObj(response.body());
            if(jo.getBool("result")){
                JSONObject data = jo.getJSONObject("data");
                SettingsStorageApi.put(SettingsStorageApi.USER_ID,data.getInt("id"));
                SettingsStorageApi.put(SettingsStorageApi.USER_EMAIL, data.getStr("email"));
                SettingsStorageApi.put(SettingsStorageApi.USER_NAME, data.getStr("name"));
                JSONArray spaces = data.getJSONArray("spaces");
                //找到正在使用的空间
                Optional<JSONObject> use = spaces.stream().map(e->(JSONObject) e).filter(e->"USE".equals(e.getStr("state"))).findFirst();
                SettingsStorageApi.put(SettingsStorageApi.USE_SPACE_ID, use.get().getInt("spaceId"));
                SettingsStorageApi.put(SettingsStorageApi.USE_SPACE_ROLE, use.get().getStr("role"));
                SettingsStorageApi.put(SettingsStorageApi.USER_ALL_SPACE_ROLE, spaces.stream().map(JSONUtil::toJsonStr).collect(Collectors.toSet()));
            }
            if(view != null) {
                view.post(() -> callback(jo));
            }
        }).start();
    }
}
