package com.tory.noname.fragment;

import android.support.v4.app.Fragment;

import com.tory.noname.bili.BiliListFragment;
import com.tory.noname.gank.GankListFragment;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: 2016/9/11
 */
public abstract class BaseFragment extends Fragment {

    public static final String TAG_GANK_LIST_FRAGMENT = "tag_gank_list_fragment";
    public static final String TAG_SETTING_FRAGMENT = "tag_setting_fragment";
    public static final String TAG_BILI_FRAGMENT = "tag_bili_fragment";


    public static Fragment createNewFragmentForTag(String tag) {
        if (TAG_GANK_LIST_FRAGMENT.equals(tag)) {
            return new GankListFragment();
        }else if(TAG_SETTING_FRAGMENT.equals(tag)) {
            return SettingsFragment.newInstance();
        }else if(TAG_BILI_FRAGMENT.equals(tag)){
            return new BiliListFragment();
        }
        throw new IllegalStateException("Unexpected fragment: " + tag);
    }
}
