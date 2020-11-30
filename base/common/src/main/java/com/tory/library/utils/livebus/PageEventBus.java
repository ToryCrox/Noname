package com.tory.library.utils.livebus;

import android.content.Context;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.tory.library.log.LogUtils;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 * Description: 页面内通信，不同activity消息不会混乱
 */
public class PageEventBus extends LiveEventBusCore {

    private static PageEventBus obtain(@NonNull ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner, FACTORY).get(PageEventBus.class);
    }

    /**
     * 作用域为Activity内部
     * @param activity
     * @return
     */
    @NonNull
    public static PageEventBus get(@NonNull ComponentActivity activity) {
        return obtain(activity);
    }

    /**
     * 作用域为Fragment内
     * @param fragment
     * @return
     */
    @NonNull
    public static PageEventBus get(@NonNull Fragment fragment) {
        return obtain(fragment);
    }

    /**
     * context必需为ComponentActivity，activity内部通信
     * @param context must be ComponentActivity, or can throw IllegalArgumentException
     */
    @NonNull
    public static PageEventBus get(@NonNull Context context) {
        if (context instanceof ComponentActivity) {
            return get((ComponentActivity) context);
        } else {
            throw new IllegalArgumentException("context must be FragmentActivity, please check it");
        }
    }

    /**
     * 允许为空，防止在Fragment里面post时获取不到Context
     *
     * @param context
     * @return
     */
    @Nullable
    public static PageEventBus getOrNull(@Nullable Context context) {
        if (context instanceof ComponentActivity) {
            return get((ComponentActivity) context);
        } else {
            Throwable e = new IllegalStateException("PageEventBus can not get, context is " + context);
            LogUtils.e("PageEventBus ", "",  e);
            return null;
        }
    }

    public static ViewModelProvider.Factory FACTORY = new ViewModelProvider.Factory() {
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PageEventBus();
        }
    };
}
