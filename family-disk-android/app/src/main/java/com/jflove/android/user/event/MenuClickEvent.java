package com.jflove.android.user.event;

import static com.jflove.android.api.HttpApi.REGISTER_URL;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
import com.jflove.android.R;
import com.jflove.android.api.SettingsStorageApi;
import com.jflove.android.rewrite.MenuBaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/11/28 5:26 PM
 * @desc: 用户模块,菜单点击事件
 */
public class MenuClickEvent implements View.OnClickListener{
    private Fragment fragment;
    private MenuBaseExpandableListAdapter adapter;

    private View parentView;

    public MenuClickEvent(Fragment fragment, MenuBaseExpandableListAdapter adapter, View parentView) {
        this.fragment = fragment;
        this.adapter = adapter;
        this.parentView = parentView;
    }

    @Override
    public void onClick(View view) {
        String name = ((MaterialTextView) view).getText().toString();
        switch (name){
            case "注册":
                //不重复开发了,注册时直接弹到网页版去注册
                Uri uri = Uri.parse(REGISTER_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                fragment.startActivity(intent);
                break;
            case "登录":
                //弹出登录面板
                parentView.findViewById(R.id.linearLayout_logon).setVisibility(View.VISIBLE);
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
    }

    /**
     * 根据登录情况创建菜单数据
     * @return
     */
    public static List<Map<String,Object>> createMenu(){
        List<Map<String,Object>> menu = new ArrayList<>();
        if(SettingsStorageApi.isExist(SettingsStorageApi.Authorization)){//已登录时使用的菜单
            menu.add(Map.of("name","退出登录","id",3l));
            List<Map> menu4 = new ArrayList<>();
            menu4.add(Map.of("name","加入我的空间","id",41l));
            menu4.add(Map.of("name","查看空间已使用量","id",42l));
            menu4.add(Map.of("name","我的空间人员管理","id",43l));
            menu4.add(Map.of("name","申请加入其他人的空间","id",44l));
            menu4.add(Map.of("name","邀请其他人加入我的空间","id",45l));
            menu.add(Map.of("name","空间信息","id",4l,"child",menu4));
            menu.add(Map.of("name","分享管理","id",5l));
        }else {//未登录时使用的菜单
            menu.add(Map.of("name","登录","id",1l));
            menu.add(Map.of("name","注册","id",2l));
        }
        return menu;
    }
}
