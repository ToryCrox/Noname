package com.tory.noname.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.tory.library.utils.FileUtils;
import com.tory.noname.R;
import com.tory.noname.dialog.ExpendDialog;
import com.tory.noname.main.utils.L;
import com.tory.library.utils.SettingHelper;
import com.tory.library.utils.Utilities;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * https://developer.android.com/guide/topics/ui/settings.html#Activity
 * https://developer.android.com/reference/android/support/v14/preference/PreferenceFragment.html
 */
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String FRAGMENT_TAG = "TAG_SETTING_FRAGMENT";

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
        final Activity activity = getActivity();

        Observable.create(subscriber -> {
                    String s = FileUtils.getCacheSize(activity);
                    String s1 = FileUtils.getFileSizeFormat(Glide.getPhotoCacheDir(activity));
                    subscriber.onNext(s);
                    subscriber.onNext("图片缓存:" + s1);
                    subscriber.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (s.toString().startsWith("图片缓存:")) {
                        findPreference("photo_cache_clear").setSummary(s.toString());
                    } else {
                        findPreference("cache_clear").setSummary("缓存:" + s);
                    }
                    L.d("compute file end :" + Thread.currentThread().getName());
                }, e -> e.printStackTrace());

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
            Utilities.setNightMode(getActivity(), mSettingHelper.isNightMode(), false);
        }
    }


    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("cache_clear")) {
            DialogUtils.create(getActivity(), "清除缓存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Observable.create( subscriber -> {
                            FileUtils.clearCache(getActivity());
                            subscriber.onNext(null);
                            subscriber.onComplete();
                        })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe( aVoid -> onCatchSizeChange()
                            );

                }
            }).show();
        } else if (preference.getKey().equals("photo_cache_clear")) {
            DialogUtils.create(getActivity(), "清除图片缓存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Observable.create(subscriber -> {
                            Glide.get(getActivity()).clearDiskCache();
                            subscriber.onNext(null);
                            subscriber.onComplete();
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe( a -> {
                                onCatchSizeChange();
                            });

                }
            }).show();
        } else if ("empty_dir_delete".equals(preference.getKey())) {
            final File file = Environment.getExternalStorageDirectory();
            DialogUtils.create(getActivity(), "删除所有空目录" + file.getAbsolutePath(),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            L.d(TAG, "目录：" + file.getAbsolutePath());
                            FileUtils.deleteEmptyDir(file, false);

                        }
                    }).show();
        }else if("test_dialog".equals(preference.getKey())){
            test_dialog();
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void test_dialog() {
        ExpendDialog dialog = new ExpendDialog(getActivity());
        dialog.show();

    }

    public static class DialogUtils {

        public static AlertDialog.Builder create(Context context, String msg,
                                                 final DialogInterface.OnClickListener listener) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setMessage(msg)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listener != null) {
                                listener.onClick(dialog, which);
                            }
                        }
                    });
            return builder;
        }

    }
}
