package com.jflove.android.user.event;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @author: tanjun
 * @date: 2023/11/28 5:33 PM
 * @desc: 关闭面板点击事件, 通用类
 */
public class LinearLayoutCloseClickEvent implements View.OnClickListener{
    private View parentView;
    private int id;

    public LinearLayoutCloseClickEvent(View parentView,int id) {
        this.parentView = parentView;
        this.id = id;
    }

    @Override
    public void onClick(View view) {
        //同时关闭软键盘
        InputMethodManager inputMethodManager = (InputMethodManager)parentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(parentView.getWindowToken(), 0);
        }
        parentView.findViewById(id).setVisibility(View.INVISIBLE);
    }
}
