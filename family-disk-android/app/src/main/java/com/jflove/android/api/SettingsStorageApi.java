package com.jflove.android.api;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.Set;

import io.reactivex.rxjava3.core.Single;

/**
 * @author: tanjun
 * @date: 2023/11/27 10:38 AM
 * @desc: 配置数据存储
 */
public class SettingsStorageApi {
    private static RxDataStore<Preferences> dataStore = null;

    /**
     * 用户邮箱(账号)
     */
    public static final Preferences.Key<String> USER_EMAIL = PreferencesKeys.stringKey("USER_EMAIL");
    /**
     * token
     */
    public static final Preferences.Key<String> Authorization = PreferencesKeys.stringKey("Authorization");
    /**
     * 用户ID
     */
    public static final Preferences.Key<Integer> USER_ID = PreferencesKeys.intKey("USER_ID");
    /**
     * 空间ID
     */
    public static final Preferences.Key<Integer> USE_SPACE_ID = PreferencesKeys.intKey("USE_SPACE_ID");
    /**
     * 空间权限
     */
    public static final Preferences.Key<String> USE_SPACE_ROLE = PreferencesKeys.stringKey("USE_SPACE_ROLE");
    /**
     * 用户名称
     */
    public static final Preferences.Key<String> USER_NAME = PreferencesKeys.stringKey("USER_NAME");
    /**
     * 用户所有空间
     */
    public static final Preferences.Key<Set<String>> USER_ALL_SPACE_ROLE = PreferencesKeys.stringSetKey("USER_ALL_SPACE_ROLE");

    /**
     * 初始化存储对象
     * @param context
     */
    public static void init(Context context){
        dataStore = new RxPreferenceDataStoreBuilder(context,"settings").build();
    }

    public static <T> void put(Preferences.Key<T> key, T value){
        Single<Preferences> updateResult = dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key,value);
            return Single.just(mutablePreferences);
        });
        updateResult.blockingSubscribe();
    }

    public static <T> void delete(Preferences.Key<T> key){
        Single<Preferences> updateResult = dataStore.updateDataAsync(e->{
            MutablePreferences mutablePreferences = e.toMutablePreferences();
            mutablePreferences.remove(key);
            return Single.just(mutablePreferences);
        });
        updateResult.blockingSubscribe();
    }

    public static <T> T get(Preferences.Key<T> key){
        try {
            return dataStore.data().map(e -> e.get(key)).blockingFirst();
        }catch (NullPointerException e){
            return null;
        }
    }

    public static <T> boolean isExist(Preferences.Key<T> key){
        return get(key) != null;
    }
}
