package com.tory.noname.main.base;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.tory.noname.R;

/**
 * @Author: Tory
 * Create: 2016/9/25
 * Update: 2016/9/25
 */
public abstract class BaseListFragment extends BaseFragment {

    protected TabLayout mTabLayout;
    protected ViewPager mViewPager;
    protected String mTitles[];


    public BaseListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_common_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mTabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        PagerAdapter tabPageAdapter = createPageAdpater();
        mViewPager.setAdapter(tabPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    protected abstract PagerAdapter createPageAdpater();
}
