package com.tory.noname.bili;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.tory.noname.R;
import com.tory.noname.main.base.BaseActivity;
import com.tory.noname.bili.bean.CategoryMeta;
import com.tory.noname.bili.bgmlist.BgmlistFragment;
import com.tory.noname.main.utils.L;

public class CategoryHomeActivity extends BaseActivity {

    public static final String ARG_CATE_ID = "arg_cate_id";
    public static final String ARG_CATE_TYPE = "arg_cate_type";
    public static final String ARG_CATE = "arg_cate";

    private int mTid;
    private int mType;

    private CategoryMeta mCate;

    @Override
    public int bindLayout() {
        return R.layout.activity_content_main;
    }

    @Override
    public void initView() {
        setToolbarScrolled(true);
        setDisplayHomeAsUpEnabled(true);
        setToolbarBackpress();

        Intent intent = getIntent();
        mCate = intent.getParcelableExtra(ARG_CATE);
        L.d(TAG,"cate: "+mCate);
        if(mCate.type == CategoryMeta.TYPE_RANK) {
            showFragment(BiliRankListFragment.FRAGMENT_TAG, true, false);
        }else if(mCate.type == CategoryMeta.TYPE_NOMAL){
            showFragment(CategoryHomeFragment.FRAGMENT_TAG,true,false);
        }else if(mCate.type == CategoryMeta.TYPE_BGM_LIST){
            showFragment(BgmlistFragment.FRAGMENT_TAG,true,false);
        }
        setToolbarTitle(mCate.typename);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public int getFragmentContainer(String tag) {
        return R.id.frame_content;
    }

    public Fragment createNewFragmentForTag(String tag){
        if (BiliRankListFragment.FRAGMENT_TAG.equals(tag)) {
            return new BiliRankListFragment();
        }else if(CategoryHomeFragment.FRAGMENT_TAG.equals(tag)){
            return CategoryHomeFragment.newInstance(mCate);
        }else if(BgmlistFragment.FRAGMENT_TAG.equals(tag)){
            return BgmlistFragment.newInstance();
        }
        throw new IllegalStateException("Unexpected fragment: " + tag);
    }
}
