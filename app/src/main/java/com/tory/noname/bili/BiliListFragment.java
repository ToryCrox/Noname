package com.tory.noname.bili;

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
import com.tory.noname.utils.L;

/**
 * @Author: Tory
 * Create: 2016/9/16
 * Update: 2016/9/16
 */
public class BiliListFragment extends Fragment{
    private static final String TAG = "BiliListFragment";
    
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private String mTitles[];
    private int[] mRankTabIds;
    private String[] mRankTypes;

    public BiliListFragment() {
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
        initData();
        mTabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        PagerAdapter tabPageAdapter = new BiliFragmentPagerAdapter(getChildFragmentManager(),mTitles);
        mViewPager.setAdapter(tabPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initData() {
        mTitles = new String[]{
                "全站","番剧","动画","音乐","舞蹈","游戏","科技","生活","电影","鬼畜","电视剧","时尚","娱乐"
        };
        mRankTabIds = new int[]{
                0,33,1,3,129,4,36,160,23,119,11,155,5};
        mRankTypes = new String[]{"all","origin","rookie"};

        L.d(TAG,"bili cat: "+BiliHelper.buildCate(getActivity()));
    }

    public class BiliFragmentPagerAdapter extends FragmentPagerAdapter {

        private String mTitles[];

        public BiliFragmentPagerAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            this.mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return BiliRankPageFragment.newInstance(mTitles[position],mRankTypes[0],mRankTabIds[position]);
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
