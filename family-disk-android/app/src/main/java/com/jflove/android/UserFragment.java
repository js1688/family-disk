package com.jflove.android;

import static com.jflove.android.user.event.MenuClickEvent.createMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.jflove.android.api.SettingsStorageApi;
import com.jflove.android.rewrite.MenuBaseExpandableListAdapter;
import com.jflove.android.user.event.LinearLayoutCloseClickEvent;
import com.jflove.android.user.event.LogonSubmitClickEvent;
import com.jflove.android.user.event.MenuClickEvent;

import java.util.Optional;

public class UserFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        //设置已登录账号的名称显示
        String name = Optional.ofNullable(SettingsStorageApi.get(SettingsStorageApi.USER_NAME)).orElse("请先登录");
        ((TextView)view.findViewById(R.id.textView_name)).setText(name);

        //创建菜单,以及点击事件回调
        ExpandableListView expandableListView = view.findViewById(R.id.listView_menu);
        MenuBaseExpandableListAdapter adapter = new MenuBaseExpandableListAdapter(getActivity(), createMenu());
        adapter.setOnClickListener(new MenuClickEvent(this,adapter,view,getActivity()));
        expandableListView.setAdapter(adapter);

        //设置登陆弹框,关闭按钮点击监听事件
        Button logonExit =  view.findViewById(R.id.logon_exit);
        LinearLayoutCloseClickEvent lece = new LinearLayoutCloseClickEvent(view,R.id.linearLayout_logon);
        logonExit.setOnClickListener(lece);

        //设置登陆按钮点击事件
        Button logonSubmit = view.findViewById(R.id.logon_submit);
        logonSubmit.setOnClickListener(new LogonSubmitClickEvent(getActivity(),adapter,lece,view));

        //设置我的空间信息弹框,关闭按钮点击监听事件
        Button spaceInfoExit = view.findViewById(R.id.shareRel_exit);
        spaceInfoExit.setOnClickListener(new LinearLayoutCloseClickEvent(view,R.id.linearLayout_shareRel));

        return view;
    }
}