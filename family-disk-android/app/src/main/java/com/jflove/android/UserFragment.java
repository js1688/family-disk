package com.jflove.android;

import static com.jflove.android.api.HttpApi.GET_USERINFO_URL;
import static com.jflove.android.api.HttpApi.LOGON_URL;
import static com.jflove.android.api.HttpApi.REGISTER_URL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
import com.jflove.android.api.HttpApi;
import com.jflove.android.api.SettingsStorageApi;
import com.jflove.android.rewrite.MenuBaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class UserFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        //设置菜单列表功能, 登录和未登录菜单不一样
        ExpandableListView expandableListView = view.findViewById(R.id.listView_menu);
        //创建菜单,以及点击事件回调
        MenuBaseExpandableListAdapter adapter = new MenuBaseExpandableListAdapter(getActivity(), createMenu());
        adapter.setClickListener(v -> {
            String name = ((MaterialTextView) v).getText().toString();
            switch (name){
                case "注册":
                    //不重复开发了,注册时直接弹到网页版去注册
                    Uri uri = Uri.parse(REGISTER_URL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
                case "登录":
                    //弹出登录面板
                    view.findViewById(R.id.linearLayout_logon).setVisibility(View.VISIBLE);
                    break;
                case "退出登录":
                    SettingsStorageApi.delete(SettingsStorageApi.Authorization);
                    SettingsStorageApi.delete(SettingsStorageApi.USER_NAME);
                    SettingsStorageApi.delete(SettingsStorageApi.USER_EMAIL);
                    SettingsStorageApi.delete(SettingsStorageApi.USER_ID);
                    SettingsStorageApi.delete(SettingsStorageApi.USE_SPACE_ROLE);
                    SettingsStorageApi.delete(SettingsStorageApi.USER_ALL_SPACE_ROLE);
                    SettingsStorageApi.delete(SettingsStorageApi.USE_SPACE_ID);
                    adapter.reFreshData(createMenu());//刷新菜单
                    break;
                default:
                    break;
            }
        });
        expandableListView.setAdapter(adapter);
        //设置登陆弹框,关闭按钮点击监听事件
        Button logonExit =  view.findViewById(R.id.logon_exit);
        View.OnClickListener logonExitOnCheck = v -> {
            //同时关闭软键盘
            InputMethodManager inputMethodManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.findViewById(R.id.linearLayout_logon).setVisibility(View.INVISIBLE);
        };
        logonExit.setOnClickListener(logonExitOnCheck);
        //设置登陆按钮点击事件
        Button logonSubmit = view.findViewById(R.id.logon_submit);
        logonSubmit.setOnClickListener(view13 -> {
            EditText logonEmail = view.findViewById(R.id.logon_email);
            EditText logonPassword = view.findViewById(R.id.logon_password);
            if(StrUtil.isEmpty(logonEmail.getText()) || StrUtil.isEmpty(logonPassword.getText())){
                Toast.makeText(getActivity(),"请输入邮箱和密码",Toast.LENGTH_SHORT).show();
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
                            logonExitOnCheck.onClick(null);
                            logonEmail.setText("");
                            logonPassword.setText("");
                        }else {
                            Toast.makeText(getActivity(), userInfo.getStr("message"), Toast.LENGTH_SHORT).show();
                        }
                    }).get(GET_USERINFO_URL,view);
                }
                Toast.makeText(getActivity(), response.getStr("message"), Toast.LENGTH_SHORT).show();
            }).post(LOGON_URL,param,view);
        });
        return view;
    }

    /**
     * 根据登录情况创建菜单数据
     * @return
     */
    private List<Map<String,Object>> createMenu(){
        List<Map<String,Object>> menu = new ArrayList<>();
        if(SettingsStorageApi.isExist(SettingsStorageApi.Authorization)){//已登录时使用的菜单
            menu.add(Map.of("name","退出登录","id",3l));
            List<Map> menu4 = new ArrayList<>();
            menu4.add(Map.of("name","加入我的空间","id",41l));
            menu4.add(Map.of("name","查看空间已使用量","id",42l));
            menu.add(Map.of("name","空间信息","id",4l,"child",menu4));
            menu.add(Map.of("name","分享管理","id",5l));
        }else {//未登录时使用的菜单
            menu.add(Map.of("name","登录","id",1l));
            menu.add(Map.of("name","注册","id",2l));
        }
        return menu;
    }
}