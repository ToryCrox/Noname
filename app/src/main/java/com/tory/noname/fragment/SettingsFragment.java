package com.tory.noname.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.tory.noname.R;
import com.tory.noname.utils.FileUtils;
import com.tory.noname.utils.L;
import com.tory.noname.utils.SettingHelper;
import com.tory.noname.utils.Utilities;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String TAG = "SettingsFragment";
    private SettingHelper mSettingHelper;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
        //getPreferenceManager().setSharedPreferencesName(SettingHelper.SHARED_PATH);
        //onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences());
        mSettingHelper = SettingHelper.getInstance(getActivity());
        onHiddenChanged(false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        L.d(TAG, "onHiddenChanged " + hidden);
        if (!hidden) {
            onCatchSizeChange();
        }
        super.onHiddenChanged(hidden);
    }

    public void onCatchSizeChange() {
        Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        String s = FileUtils.getCacheSize(getContext());
                        String s1 = FileUtils.getFileSizeFormat(Glide.getPhotoCacheDir(getContext()));
                        subscriber.onNext(s);
                        subscriber.onNext("图片缓存:"+s1);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if(s.startsWith("图片缓存:")){
                            findPreference("photo_cache_clear").setSummary(s);
                        }else{
                            findPreference("cache_clear").setSummary("缓存:" + s);
                        }
                        L.d("compute file end :" + Thread.currentThread().getName());
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingHelper.SP_KEY_MODE_NIGHT.equals(key)) {
            L.d(TAG, "onSharedPreferenceChanged key:" + mSettingHelper.isNightMode());
            Utilities.setNightMode(getActivity(), mSettingHelper.isNightMode(), true);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("cache_clear")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage("清除缓存")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Observable.create(new Observable.OnSubscribe<Void>() {
                                        @Override
                                        public void call(Subscriber<? super Void> subscriber) {
                                            FileUtils.clearCache(getContext());
                                            subscriber.onNext(null);
                                            subscriber.onCompleted();
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            onCatchSizeChange();
                                        }
                                    });

                        }
                    });
            builder.show();
        }else if(preference.getKey().equals("photo_cache_clear")){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage("清除图片缓存")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Observable.create(new Observable.OnSubscribe<Void>() {
                                @Override
                                public void call(Subscriber<? super Void> subscriber) {
                                    Glide.get(getContext()).clearDiskCache();
                                    subscriber.onNext(null);
                                    subscriber.onCompleted();
                                }
                            })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            onCatchSizeChange();
                                        }
                                    });

                        }
                    });
            builder.show();
        }
        return super.onPreferenceTreeClick(preference);
    }
}
