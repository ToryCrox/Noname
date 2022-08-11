package com.tory.noname.bili;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.List;

import com.google.android.material.tabs.TabLayout;
import com.tory.library.base.BaseActivity;
import com.tory.noname.R;
import com.tory.noname.main.utils.L;

import java.util.Arrays;

/**
 * @Author: Tory
 * Create: 2016/9/16
 * Update: 2016/9/16
 */
public class BiliRankListFragment extends Fragment {
    public static final String FRAGMENT_TAG = "BiliRankListFragment";
    private static final String TAG = "BiliRankListFragment";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private String mTitles[];
    private int[] mRankTabIds;
    private String[] mRankTypes;
    private int[] mRankRangeList;

    private int mRankRange = 30;

    public BiliRankListFragment() {
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
        return inflater.inflate(R.layout.fragment_page_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initData();
        // mTabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        // mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
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
        mRankRangeList = new int[]{1,3,7,30};
        mRankRange = BiliSetting.getInstance().getRankRage();

        L.d(TAG,"bili cat: "+BiliHelper.buildCate(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        onHiddenChanged(isHidden());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        showSpinner(!hidden);
    }

    private void showSpinner(boolean show){
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = activity.getToolbar();
        Spinner rankRangeSpinner = (Spinner) toolbar.findViewById(R.id.simple_spinner_item_in_toolbar);
        if(rankRangeSpinner == null && show){
            rankRangeSpinner = creatSpinner();
            rankRangeSpinner.setId(R.id.simple_spinner_item_in_toolbar);
            Toolbar.LayoutParams lp = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = GravityCompat.END;
            lp.setMarginEnd(20);
            toolbar.addView(rankRangeSpinner,lp);
        }

        if(rankRangeSpinner != null){
            rankRangeSpinner.setVisibility(show?View.VISIBLE:View.GONE);
            if(show){
                rankRangeSpinner.setSelection(Arrays.binarySearch(mRankRangeList,mRankRange));
            }
        }
    }

    /**
     * http://www.dengzhr.com/others/mobile/727
     * @return
     */
    private Spinner creatSpinner(){
        AppCompatSpinner spinner = new AppCompatSpinner(getActivity());

        String[] rankRangeNames = new String[]{"日排行","三日榜","周排行","月排行"};
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.simple_spinner_item_in_toolbar,rankRangeNames);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mRankRangeList[position] != mRankRange){
                    mRankRange= mRankRangeList[position];
                    L.d(TAG,"rankRange change:"+mRankRange);
                    onRankFilterChange();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
            // ...
        });
        ViewCompat.setBackgroundTintList(spinner, ColorStateList.valueOf(Color.WHITE));
        return spinner;
    }

    private void onRankFilterChange() {
        BiliSetting.getInstance().setRankRage(mRankRange);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for(Fragment fragment: fragments){
            if(fragment instanceof BiliRankPageFragment){
                BiliRankPageFragment rankFragment = (BiliRankPageFragment) fragment;
                rankFragment.setRankRange(mRankRange);
            }
        }

    }


    public class BiliFragmentPagerAdapter extends FragmentPagerAdapter {

        private String mTitles[];

        public BiliFragmentPagerAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            this.mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return BiliRankPageFragment.newInstance(mTitles[position],mRankTypes[0],mRankRange,mRankTabIds[position]);
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
