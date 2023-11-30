package com.jflove.android.user.event;

import static com.jflove.android.api.HttpApi.GET_USERINFO_URL;
import static com.jflove.android.api.HttpApi.LOGON_URL;
import static com.jflove.android.user.event.MenuClickEvent.createMenu;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jflove.android.R;
import com.jflove.android.api.HttpApi;
import com.jflove.android.api.SettingsStorageApi;
import com.jflove.android.rewrite.MenuBaseExpandableListAdapter;

import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * @author: tanjun
 * @date: 2023/11/28 5:37 PM
 * @desc: 用户模块,登录事件
 */
public class LogonSubmitClickEvent implements View.OnClickListener{
    private Context context;
    private MenuBaseExpandableListAdapter adapter;

    private LinearLayoutCloseClickEvent lece;

    private View parentView;

    public LogonSubmitClickEvent(Context context, MenuBaseExpandableListAdapter adapter, LinearLayoutCloseClickEvent lece, View parentView) {
        this.context = context;
        this.adapter = adapter;
        this.lece = lece;
        this.parentView = parentView;
    }

    @Override
    public void onClick(View view) {
        EditText logonEmail = parentView.findViewById(R.id.logon_email);
        EditText logonPassword = parentView.findViewById(R.id.logon_password);
        if(StrUtil.isEmpty(logonEmail.getText()) || StrUtil.isEmpty(logonPassword.getText())){
            Toast.makeText(context,"请输入邮箱和密码",Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject param = JSONUtil.createObj().set("email", logonEmail.getText()).set("password", logonPassword.getText());
        ((HttpApi) response -> {
            if (response.getBool("result")) {//登录成功,查询用户信息缓存一下
                String token = response.getStr("data");
                SettingsStorageApi.put(SettingsStorageApi.Authorization, token);
                //获取当前登录账号信息
                ((HttpApi) userInfo -> {
                    if(userInfo.getBool("result")){
                        JSONObject data = userInfo.getJSONObject("data");
                        SettingsStorageApi.put(SettingsStorageApi.USER_ID,data.getInt("id"));
                        SettingsStorageApi.put(SettingsStorageApi.USER_EMAIL, data.getStr("email"));
                        SettingsStorageApi.put(SettingsStorageApi.USER_NAME, data.getStr("name"));
                        JSONArray spaces = data.getJSONArray("spaces");
                        //找到正在使用的空间
                        Optional<JSONObject> use = spaces.stream().map(e->(JSONObject) e).filter(e->"USE".equals(e.getStr("state"))).findFirst();
                        SettingsStorageApi.put(SettingsStorageApi.USE_SPACE_ID, use.get().getInt("spaceId"));
                        SettingsStorageApi.put(SettingsStorageApi.USE_SPACE_ROLE, use.get().getStr("role"));
                        SettingsStorageApi.put(SettingsStorageApi.USER_ALL_SPACE_ROLE, spaces.stream().map(JSONUtil::toJsonStr).collect(Collectors.toSet()));
                        adapter.reFreshData(createMenu());//刷新菜单
                        lece.onClick(parentView);
                        ((TextView)parentView.findViewById(R.id.textView_name)).setText(data.getStr("name"));
                        logonEmail.setText("");
                        logonPassword.setText("");
                    }else {
                        Toast.makeText(context, userInfo.getStr("message"), Toast.LENGTH_SHORT).show();
                    }
                }).get(GET_USERINFO_URL,parentView);
            }
            Toast.makeText(context, response.getStr("message"), Toast.LENGTH_SHORT).show();
        }).post(LOGON_URL,param,parentView);
    }
}
