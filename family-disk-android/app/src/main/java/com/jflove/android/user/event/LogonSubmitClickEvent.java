package com.jflove.android.user.event;

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

import cn.hutool.core.util.StrUtil;
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


    private View parentView;

    public LogonSubmitClickEvent(Context context, MenuBaseExpandableListAdapter adapter, View parentView) {
        this.context = context;
        this.adapter = adapter;
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
                ((HttpApi) r -> {
                    if(r.getBool("result")) {
                        adapter.reFreshData(createMenu());//刷新菜单
                        new LinearLayoutCloseClickEvent(parentView,R.id.linearLayout_logon).onClick(null);
                        ((TextView) parentView.findViewById(R.id.textView_name)).setText(r.getJSONObject("data").getStr("name"));
                        logonEmail.setText("");
                        logonPassword.setText("");
                    }else{
                        Toast.makeText(context, r.getStr("message"), Toast.LENGTH_SHORT).show();
                    }
                }).getUserInfo(parentView);
            }
            Toast.makeText(context, response.getStr("message"), Toast.LENGTH_SHORT).show();
        }).post(LOGON_URL,param,parentView);
    }


}
