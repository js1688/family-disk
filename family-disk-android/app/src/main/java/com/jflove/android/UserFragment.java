package com.jflove.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;

public class UserFragment extends Fragment {
    private static final int ldId = 10000;
    private static final int zcId = 11000;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        //设置菜单列表功能, 登录和未登录菜单不一样
        ListView listView = view.findViewById(R.id.listView_menu);
        //未登录时使用的菜单
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity()
                //,android.R.layout.simple_list_item_1
                ,R.layout.listview_item
                ,new String[]{"登录","注册"});
        //todo 已登录时使用的菜单
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            MaterialTextView v = (MaterialTextView) view1;
            //Toast.makeText(getActivity(),String.format("选择的功能%s",v.getText()), Toast.LENGTH_SHORT).show();
            if("注册".equals(v.getText())){
                //不重复开发了,注册时直接弹到网页版去注册
                Uri uri = Uri.parse("https://m.jflove.cn");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }else if("登录".equals(v.getText())){
                //弹出登录面板
                view.findViewById(R.id.linearLayout_logon).setVisibility(View.VISIBLE);
            }
        });
        //设置登陆弹框,关闭按钮点击监听事件
        Button logonExit =  view.findViewById(R.id.logon_exit);
        logonExit.setOnClickListener(view12 -> {
            //同时关闭软键盘
            InputMethodManager inputMethodManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.findViewById(R.id.linearLayout_logon).setVisibility(View.INVISIBLE);
        });
        //设置登陆按钮点击事件
        Button logonSubmit = view.findViewById(R.id.logon_submit);
        logonSubmit.setOnClickListener(view13 -> {
            EditText logonEmail = view.findViewById(R.id.logon_email);
            EditText logonPassword = view.findViewById(R.id.logon_password);

            Toast.makeText(getActivity(),String.format("登录邮箱%s,密码:%s",logonEmail.getText(),logonPassword.getText()), Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}