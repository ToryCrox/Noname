package com.tory.library.utils.livebus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 * Description: 页面内通信
 */
public class PageEventBus extends LiveEventBusCore {

    private static PageEventBus obtain(@NonNull ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner, FACTORY).get(PageEventBus.class);
    }

    @NonNull
    public static PageEventBus get(@NonNull FragmentActivity activity) {
        return obtain(activity);
    }

    @NonNull
    public static PageEventBus get(@NonNull Fragment fragment) {
        return obtain(fragment);
    }

    /**
     * @param context must be FragmentActivity
     * @return
     */
    @NonNull
    public static PageEventBus get(@NonNull Context context) {
        if (context instanceof FragmentActivity) {
            return get((FragmentActivity)context);
        } else {
            throw new IllegalArgumentException("context must be FragmentActivity, please check it");
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
