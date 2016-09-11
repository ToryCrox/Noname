package com.tory.noname.fragment;

import android.support.v4.app.Fragment;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: ${UPDATE}
 */
public abstract class BaseFragment extends Fragment {

    public static final String TAG_GANK_LIST_FRAGMENT = "tag_gank_list_fragment";


    public static Fragment createNewFragmentForTag(String tag) {
        if (TAG_GANK_LIST_FRAGMENT.equals(tag)) {
            return new GankListFragment();
        }
        throw new IllegalStateException("Unexpected fragment: " + tag);
    }
}
