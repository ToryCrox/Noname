package com.tory.noname.main.base;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: 2016/9/11
 */
public abstract class BaseFragment extends Fragment {

    private static final String KEY_FRAGMENT_HIDDEN = "key_fragment_hidden";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(KEY_FRAGMENT_HIDDEN)) {
                getFragmentManager().beginTransaction().hide(this).commit();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FRAGMENT_HIDDEN, isHidden());
    }
}
