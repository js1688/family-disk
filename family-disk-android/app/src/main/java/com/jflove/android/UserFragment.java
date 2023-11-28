package com.jflove.android;

import static com.jflove.android.user.event.MenuClickEvent.createMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;

import androidx.fragment.app.Fragment;

import com.jflove.android.rewrite.MenuBaseExpandableListAdapter;
import com.jflove.android.user.event.LogonExitClickEvent;
import com.jflove.android.user.event.LogonSubmitClickEvent;
import com.jflove.android.user.event.MenuClickEvent;

public class UserFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        //设置菜单列表功能, 登录和未登录菜单不一样
        ExpandableListView expandableListView = view.findViewById(R.id.listView_menu);
        //创建菜单,以及点击事件回调
        MenuBaseExpandableListAdapter adapter = new MenuBaseExpandableListAdapter(getActivity(), createMenu());
        adapter.setClickListener(new MenuClickEvent(this,adapter,view));
        expandableListView.setAdapter(adapter);
        //设置登陆弹框,关闭按钮点击监听事件
        Button logonExit =  view.findViewById(R.id.logon_exit);
        LogonExitClickEvent lece = new LogonExitClickEvent(view);
        logonExit.setOnClickListener(lece);
        //设置登陆按钮点击事件
        Button logonSubmit = view.findViewById(R.id.logon_submit);
        logonSubmit.setOnClickListener(new LogonSubmitClickEvent(getActivity(),adapter,lece,view));
        return view;
    }
}