package com.tory.noname.gank;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.tory.noname.fragment.BaseListFragment;
import com.tory.noname.utils.Constance;

public class GankListFragment extends BaseListFragment {

    public static final String FRAGMENT_TAG = "tag_gank_list_fragment";

    public GankListFragment() {
        // Required empty public constructor
    }

    @Override
    protected PagerAdapter createPageAdpater() {
        mTitles = Constance.Gank.TAGS.clone();
        return new TabFragmentPagerAdapter(getChildFragmentManager(),mTitles);
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
