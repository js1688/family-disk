package com.jflove.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UserFragment extends Fragment {
    private static final int ldId = 10000;
    private static final int zcId = 11000;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ListView listView = view.findViewById(R.id.listView_menu);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(getActivity()
                ,android.R.layout.simple_list_item_1
                ,new String[]{"登陆","注册"});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            MaterialTextView v = (MaterialTextView) view1;
            Toast.makeText(getActivity(),String.format("选择的功能%s",v.getText()), Toast.LENGTH_SHORT).show();
            if("注册".equals(v.getText())){
                Uri uri = Uri.parse("https://m.jflove.cn");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return view;
    }
}