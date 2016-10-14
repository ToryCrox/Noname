package com.tory.noname.bili.bgmlist;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tory.noname.R;
import com.tory.noname.main.base.BaseActivity;
import com.tory.noname.main.base.BaseListFragment;

import java.util.Calendar;
import java.util.List;


public class BgmlistFragment extends BaseListFragment {

    public static final String FRAGMENT_TAG = "BgmlistFragment";

    private String mTitles[];

    private boolean mWeekDayFiter = true;


    public static BgmlistFragment newInstance() {
        BgmlistFragment fragment = new BgmlistFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mTitles = new String[]{"周日","周一","周二","周三","周四","周五","周六"};
        BgmPresenter.getInstance().loadData(false);
        mWeekDayFiter = BgmPresenter.getInstance().getFilterState();
    }

    @Override
    protected PagerAdapter createPageAdpater() {
        return new BgmFragmentPagerAdapter(getChildFragmentManager());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        mViewPager.setCurrentItem(dayOfWeek);
    }
    @Override
    public void onResume() {
        super.onResume();
        onHiddenChanged(isHidden());
    }
    @Override
    public void onDestroy() {
        BgmPresenter.getInstance().tearDown();
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        showSpinner(!hidden);
    }

    private void showSpinner(boolean show){
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = activity.getToolbar();
        if(toolbar == null) return;
        Spinner weekFiterSpinner = (Spinner) toolbar.findViewById(R.id.simple_spinner_item_in_toolbar);
        if(weekFiterSpinner == null && show){
            weekFiterSpinner = creatSpinner();
            weekFiterSpinner.setId(R.id.simple_spinner_item_in_toolbar);
            Toolbar.LayoutParams lp = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = GravityCompat.END;
            lp.setMarginEnd(20);
            toolbar.addView(weekFiterSpinner,lp);
        }

        if(weekFiterSpinner != null){
            weekFiterSpinner.setVisibility(show?View.VISIBLE:View.GONE);
            if(show){
                weekFiterSpinner.setSelection(mWeekDayFiter?0:1);
            }
        }
    }

    private Spinner creatSpinner(){
        AppCompatSpinner spinner = new AppCompatSpinner(getActivity());

        final String[] fiters = new String[]{"日本","大陆"};
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.simple_spinner_item_in_toolbar,fiters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSaveFilterChange(position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSupportBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        return spinner;
    }

    private void onSaveFilterChange(boolean b) {
        if(b != mWeekDayFiter){
            mWeekDayFiter = b;
            BgmPresenter.getInstance().saveFilterState(mWeekDayFiter);
            List<Fragment> fragments = getChildFragmentManager().getFragments();
            for(Fragment fragment: fragments){
                if(fragment instanceof BgmPageFragment){
                    BgmPageFragment bgmPageFragment = (BgmPageFragment) fragment;
                    bgmPageFragment.onSaveFilterChange(mWeekDayFiter);
                }
            }
        }
    }

    public class BgmFragmentPagerAdapter extends FragmentPagerAdapter {

        public BgmFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return BgmPageFragment.newInstance(position,mWeekDayFiter);
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
