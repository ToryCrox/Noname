package com.tory.library.base;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/**
 * from : http://www.jianshu.com/p/c5d29a0c3f4c
 */
public abstract class BasePageFragment extends Fragment {
    public static final String KEY_ARG_RECREAT = "key_arg_recreat";

    protected boolean isRecreated;

    protected boolean isViewInitiated;
    protected boolean isVisibleToUser;
    protected boolean isDataInitiated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null){
            isRecreated = savedInstanceState.getBoolean(KEY_ARG_RECREAT);
        }
        isViewInitiated = true;
        prepareFetchData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        isRecreated = true;
        outState.putBoolean(KEY_ARG_RECREAT,isRecreated);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        prepareFetchData();
    }

    public abstract void fetchData();

    public boolean prepareFetchData() {
        return prepareFetchData(false);
    }

    /**
     *
     * @param forceUpdate
     * @return
     */
    public boolean prepareFetchData(boolean forceUpdate) {
        if (isVisibleToUser && isViewInitiated && (!isDataInitiated || forceUpdate)) {
            fetchData();
            isDataInitiated = true;
            return true;
        }
        return false;
    }
}