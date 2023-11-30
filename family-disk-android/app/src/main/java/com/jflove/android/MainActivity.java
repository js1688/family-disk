package com.jflove.android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jflove.android.api.HttpApi;
import com.jflove.android.api.SettingsStorageApi;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //初始化主页面
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化各业务面板对象
        DiskFragment diskFragment = new DiskFragment();//网盘
        JournalFragment journalFragment = new JournalFragment();//日记
        NotepadFragment notepadFragment = new NotepadFragment();//备忘录
        UserFragment userFragment = new UserFragment();//用户

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view_formal);//获取底部菜单对象
        //底部菜单添加选择事件
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(R.id.action_disk == item.getItemId()){
                switchFragment(diskFragment);
                return true;
            }else if(R.id.action_riji == item.getItemId()){
                switchFragment(journalFragment);
                return true;
            }else if(R.id.action_notepad == item.getItemId()){
                switchFragment(notepadFragment);
                return true;
            }else if(R.id.action_yonghu == item.getItemId()){
                switchFragment(userFragment);
                return true;
            }

            else{
                return false;
            }
        });
        SettingsStorageApi.init(this);
        //设置默认打开的业务面板
        //如果未登录就默认打开用户登录面板,否则打开网盘
        if(SettingsStorageApi.isExist(SettingsStorageApi.Authorization)) {
            //每次重新打开APP都刷新一下用户信息
            ((HttpApi) r->{}).getUserInfo(null);
            switchFragment(diskFragment);
        }else{
            switchFragment(userFragment);
        }
    }

    private void switchFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout_formal, fragment).commitNow();
    }
}