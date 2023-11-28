package com.jflove.android.user.event;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.jflove.android.R;

/**
 * @author: tanjun
 * @date: 2023/11/28 5:33 PM
 * @desc: 用户模块,退出登录点击事件
 */
public class LogonExitClickEvent implements View.OnClickListener{
    private View parentView;

    public LogonExitClickEvent(View parentView) {
        this.parentView = parentView;
    }

    @Override
    public void onClick(View view) {
        //同时关闭软键盘
        InputMethodManager inputMethodManager = (InputMethodManager)parentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(parentView.getWindowToken(), 0);
        }
        parentView.findViewById(R.id.linearLayout_logon).setVisibility(View.INVISIBLE);
    }
}
