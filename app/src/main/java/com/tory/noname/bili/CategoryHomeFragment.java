package com.tory.noname.bili;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.tory.noname.bili.bean.CategoryMeta;
import com.tory.noname.fragment.BaseListFragment;
import com.tory.noname.utils.L;

import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/9/25
 * Update: 2016/9/25
 */
public class CategoryHomeFragment extends BaseListFragment{
    public static final String FRAGMENT_TAG = "CategoryHomeFragment";

    private static final String TAG = "CategoryHomeFragment";
    
    public static final String ARG_CATE = "arg_cate";

    CategoryMeta mCate;
    List<CategoryMeta> mChildCates;

    public CategoryHomeFragment(){

    }

    public static CategoryHomeFragment newInstance(CategoryMeta categoryMeta){
        CategoryHomeFragment fragment = new CategoryHomeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_CATE,categoryMeta);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            mCate = getArguments().getParcelable(ARG_CATE);
            L.d(TAG,"cate:"+mCate);
            buildChildCate(mCate);
        }
    }

    private void buildChildCate(CategoryMeta cate) {
        CategoryMeta cateInfo = BiliHelper.getCategoryInfo(cate.tid);
        mChildCates = cateInfo.child;
    }

    @Override
    protected PagerAdapter createPageAdpater() {
        return new BiliHomePagerAdpager(getChildFragmentManager());
    }

    protected class BiliHomePagerAdpager extends FragmentPagerAdapter {

        public BiliHomePagerAdpager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CategoryPageFragment.newInstance(mChildCates.get(position));
        }

        @Override
        public int getCount() {
            return mChildCates.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mChildCates.get(position).typename;
        }
    }
}
