package com.jflove.android.user.event;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/11/30 2:28 PM
 * @desc: 长按事件,触发弹出功能按钮选择,单选,通用类
 */
public class LongClickButtonEvent implements View.OnLongClickListener{

    /**
     * json 结构
     * [{name:'',code:''}]
     */
    private List<Map<String,Object>> list;
    /**
     * 当点击ok按钮时,触发的事件回调
     */
    private DialogInterface.OnClickListener okOnClickListener;
    private Context context;

    private int selectd;

    private View view;

    public LongClickButtonEvent(List<Map<String, Object>> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setOkOnClickListener(DialogInterface.OnClickListener okOnClickListener) {
        this.okOnClickListener = okOnClickListener;
    }

    @Override
    public boolean onLongClick(View view) {
        this.view = view;
        String [] select = list.stream().map(e->(String)e.get("name")).toArray(String[]::new);
        new AlertDialog.Builder(context)
                .setTitle(((MaterialTextView) view).getText())
                .setSingleChoiceItems(select, 0, (dialogInterface, i) -> selectd = i)
                .setPositiveButton("确定", (dialog, which) -> {
                    okOnClickListener.onClick(dialog,which);
                    selectd = 0;
                })
                .setNegativeButton("取消", (dialog, which) -> selectd = 0)
                .create().show();
        return true;
    }

    /**
     * 被触发的行
     * @return
     */
    public View getView() {
        return view;
    }

    /**
     * 返回选中的数据对象
     * @return
     */
    public Map<String,Object> getSelectd() {
        return list.get(selectd);
    }
}
