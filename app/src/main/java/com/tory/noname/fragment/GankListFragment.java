package com.tory.noname.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tory.noname.R;
import com.tory.noname.utils.Constance;

public class GankListFragment extends BaseFragment {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private String mTitles[];

    public GankListFragment() {
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
        return inflater.inflate(R.layout.fragment_gank_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTitles = Constance.Gank.TAGS.clone();
        mTabLayout = (TabLayout) view.findViewById(R.id.tl_gank);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        PagerAdapter tabPageAdapter = new TabFragmentPagerAdapter(getChildFragmentManager(),mTitles);
        mViewPager.setAdapter(tabPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public static class TabFragmentPagerAdapter extends FragmentPagerAdapter {

        private String mTitles[];

        public TabFragmentPagerAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            this.mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return GankPageFragment.newInstance(mTitles[position]);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }
}
