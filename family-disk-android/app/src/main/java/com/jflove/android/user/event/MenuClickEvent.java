package com.jflove.android.user.event;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.jflove.android.api.HttpApi.DEL_SHARELINK_URL;
import static com.jflove.android.api.HttpApi.EXIT_SPACE_REL;
import static com.jflove.android.api.HttpApi.GET_SHARELINK_URL;
import static com.jflove.android.api.HttpApi.REGISTER_URL;
import static com.jflove.android.api.HttpApi.SWITCH_SPACE;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.jflove.android.R;
import com.jflove.android.api.HttpApi;
import com.jflove.android.api.SettingsStorageApi;
import com.jflove.android.rewrite.MenuBaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * @author: tanjun
 * @date: 2023/11/28 5:26 PM
 * @desc: 用户模块,菜单点击事件
 */
public class MenuClickEvent implements View.OnClickListener{
    private Fragment fragment;
    private MenuBaseExpandableListAdapter adapter;

    private View parentView;

    private Context context;

    private ClipboardManager mClipboardManager;

    public MenuClickEvent(Fragment fragment, MenuBaseExpandableListAdapter adapter, View parentView, Context context) {
        this.fragment = fragment;
        this.adapter = adapter;
        this.parentView = parentView;
        this.context = context;
        this.mClipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
    }

    @Override
    public void onClick(View view) {
        Map<String,Object> mapData = adapter.getDataById(view.getId());
        String code = mapData.get("code").toString();
        switch (code){
            case "register":
                //不重复开发了,注册时直接弹到网页版去注册
                Uri uri = Uri.parse(REGISTER_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                fragment.startActivity(intent);
                break;
            case "logon":
                //弹出登录面板
                parentView.findViewById(R.id.linearLayout_logon).setVisibility(View.VISIBLE);
                break;
            case "exitLogon":
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("是否退出登录")
                        .setPositiveButton("确定", (dialog1, which1) -> {
                            SettingsStorageApi.delete(SettingsStorageApi.Authorization);
                            SettingsStorageApi.delete(SettingsStorageApi.USER_NAME);
                            SettingsStorageApi.delete(SettingsStorageApi.USER_EMAIL);
                            SettingsStorageApi.delete(SettingsStorageApi.USER_ID);
                            SettingsStorageApi.delete(SettingsStorageApi.USE_SPACE_ROLE);
                            SettingsStorageApi.delete(SettingsStorageApi.USER_ALL_SPACE_ROLE);
                            SettingsStorageApi.delete(SettingsStorageApi.USE_SPACE_ID);
                            ((TextView)parentView.findViewById(R.id.textView_name)).setText("请先登录");
                            adapter.reFreshData(createMenu());//刷新菜单
                        })
                        .setNegativeButton("取消",null)
                        .create().show();
                break;
            case "shareRel":
                //弹出分享信息面板
                parentView.findViewById(R.id.linearLayout_publiclist).setVisibility(View.VISIBLE);
                //调用接口获取分享列表
                ((HttpApi) shareLink -> {
                    if(shareLink.getBool("result")){
                        JSONArray datas = shareLink.getJSONArray("datas");
                        List list = datas.stream().map(e->{
                            JSONObject jo = (JSONObject) e;
                            jo.putOpt("name",jo.getStr("keyword"));
                            jo.putOpt("code",jo.getInt("id").toString());
                            jo.putOpt("id",jo.getInt("id").longValue());
                            return jo.toBean(Map.class);
                        }).collect(Collectors.toList());
                        //组装列表数据,以及对象
                        ExpandableListView expandableListView = parentView.findViewById(R.id.listView_share);
                        MenuBaseExpandableListAdapter adapter = new MenuBaseExpandableListAdapter(context, list);
                        List select = new ArrayList();
                        select.add(Map.of("name", "复制密码", "code", "copyPassword"));
                        select.add(Map.of("name", "复制地址", "code", "copyUrl"));
                        //有写的权限,才出现移除功能
                        if("WRITE".equals(SettingsStorageApi.get(SettingsStorageApi.USE_SPACE_ROLE))){
                            select.add(Map.of("name", "移除", "code", "remove"));
                        }
                        LongClickButtonEvent lcbe = new LongClickButtonEvent(select,context);
                        //添加触发确定按钮的事件
                        lcbe.setOkOnClickListener((dialog, which) -> {
                            Map<String,Object> selectd = lcbe.getSelectd();//选中的操作按钮
                            Map<String,Object> rowData = adapter.getDataById(lcbe.getView().getId());//需要操作的行数据
                            switch ((String)selectd.get("code")){
                                case "remove":
                                    new AlertDialog.Builder(context)
                                            .setTitle("提示")
                                            .setMessage("是否删除分享:" + rowData.get("keyword") + ",删除后不可恢复!")
                                            .setPositiveButton("确定", (dialog1, which1) -> {
                                                ((HttpApi) delLink ->{
                                                    if(delLink.getBool("result")){
                                                        adapter.removeRowById(lcbe.getView().getId());
                                                    }else {
                                                        Toast.makeText(context, delLink.getStr("message"), Toast.LENGTH_SHORT).show();
                                                    }
                                                }).post(DEL_SHARELINK_URL, JSONUtil.createObj().putOpt("bodyId",rowData.get("id")),parentView);
                                            })
                                            .setNegativeButton("取消",null)
                                            .create().show();
                                    break;
                                case "copyPassword":
                                    Object password = rowData.get("password");
                                    if(StrUtil.isEmptyIfStr(password)){
                                        Toast.makeText(context, "该链接没有设置密码",Toast.LENGTH_SHORT).show();
                                    }else {
                                        mClipboardManager.setPrimaryClip(ClipData.newPlainText("Simple text",password.toString()));
                                        Toast.makeText(context, "复制密码成功！", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case "copyUrl":
                                    String link = HttpApi.WWW_HOST;
                                    switch ((String)rowData.get("bodyType")){
                                        case "NOTE":
                                            link += "/#/share/notepad/?";
                                            break;
                                        case "NETDISK":
                                            link += "#/share/netdisk/?";
                                            break;
                                    }
                                    link += rowData.get("url");
                                    mClipboardManager.setPrimaryClip(ClipData.newPlainText("Simple text",link));
                                    Toast.makeText(context, "复制地址成功！",Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        });
                        adapter.setOnLongClickListener(lcbe);//当列表被长按后触发的事件处理
                        expandableListView.setAdapter(adapter);
                    }else {
                        Toast.makeText(context, shareLink.getStr("message"), Toast.LENGTH_SHORT).show();
                    }
                }).get(GET_SHARELINK_URL,parentView);
                break;
            case "iSpaceRel"://我加入的空间
                //弹出面板我加入的空间面板
                parentView.findViewById(R.id.linearLayout_publiclist).setVisibility(View.VISIBLE);
                //从缓存中拿到这个数据
                Set<String> datas = SettingsStorageApi.get(SettingsStorageApi.USER_ALL_SPACE_ROLE);
                List list = datas.stream().map(e->{
                    JSONObject jo = JSONUtil.parseObj(e);
                    jo.putOpt("name",jo.getStr("title") + (jo.getInt("spaceId") == SettingsStorageApi.get(SettingsStorageApi.USE_SPACE_ID) ? "(当前空间)" : ""));
                    jo.putOpt("code",jo.getInt("spaceId").toString());
                    jo.putOpt("id",jo.getInt("spaceId").longValue());
                    return jo.toBean(Map.class);
                }).collect(Collectors.toList());
                //组装列表数据,以及对象
                ExpandableListView expandableListView = parentView.findViewById(R.id.listView_share);
                MenuBaseExpandableListAdapter adapter = new MenuBaseExpandableListAdapter(context, list);
                List select = new ArrayList();
                select.add(Map.of("name", "切换", "code", "switch"));
                select.add(Map.of("name", "退出", "code", "exit"));
                LongClickButtonEvent lcbe = new LongClickButtonEvent(select,context);
                //添加触发确定按钮的事件
                lcbe.setOkOnClickListener((dialog, which) -> {
                    Map<String,Object> selectd = lcbe.getSelectd();//选中的操作按钮
                    Map<String,Object> rowData = adapter.getDataById(lcbe.getView().getId());//需要操作的行数据
                    switch ((String)selectd.get("code")){
                        case "exit":
                            new AlertDialog.Builder(context)
                                    .setTitle("提示")
                                    .setMessage("确定退出空间:" + rowData.get("title") + "?")
                                    .setPositiveButton("确定", (dialog1, which1) -> {
                                        ((HttpApi) exitRel ->{
                                            if(exitRel.getBool("result")){
                                                //退出空间后重新刷新一下用户信息
                                                ((HttpApi) r -> {
                                                    new LinearLayoutCloseClickEvent(parentView,R.id.linearLayout_publiclist).onClick(null);
                                                }).getUserInfo(parentView);
                                            }else {
                                                Toast.makeText(context, exitRel.getStr("message"), Toast.LENGTH_SHORT).show();
                                            }
                                        }).post(EXIT_SPACE_REL, JSONUtil.createObj().putOpt("spaceId",rowData.get("id")),parentView);
                                    })
                                    .setNegativeButton("取消",null)
                                    .create().show();
                            break;
                        case "switch":
                            ((HttpApi) response ->{
                                if(response.getBool("result")){
                                    ((HttpApi) r -> {
                                        if(r.getBool("result")) {
                                            new LinearLayoutCloseClickEvent(parentView,R.id.linearLayout_publiclist).onClick(null);
                                        }else{
                                            Toast.makeText(context, r.getStr("message"), Toast.LENGTH_SHORT).show();
                                        }
                                    }).getUserInfo(parentView);
                                }
                                Toast.makeText(context, response.getStr("message"), Toast.LENGTH_SHORT).show();
                            }).post(SWITCH_SPACE,JSONUtil.createObj().putOpt("targetSpaceId",rowData.get("id")),parentView);
                            break;
                    }
                });
                adapter.setOnLongClickListener(lcbe);//当列表被长按后触发的事件处理
                expandableListView.setAdapter(adapter);
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
            menu.add(Map.of("name","退出登录","id",3l,"code","exitLogon"));
            List<Map> menu4 = new ArrayList<>();
            menu4.add(Map.of("name","我加入的空间","id",41l,"code","iSpaceRel"));
            menu4.add(Map.of("name","查看空间已使用量","id",42l,"code","useSpace"));
            menu4.add(Map.of("name","我的空间人员管理","id",43l,"code","spaceUserRel"));
            menu4.add(Map.of("name","申请加入其他人的空间","id",44l,"code","joinOther"));
            menu4.add(Map.of("name","邀请其他人加入我的空间","id",45l,"code","inviteJoin"));
            menu.add(Map.of("name","空间信息","id",4l,"child",menu4,"code","spaceInfo"));
            menu.add(Map.of("name","分享管理","id",5l,"code","shareRel"));
        }else {//未登录时使用的菜单
            menu.add(Map.of("name","登录","id",1l,"code","logon"));
            menu.add(Map.of("name","注册","id",2l,"code","register"));
        }
        return menu;
    }
}
