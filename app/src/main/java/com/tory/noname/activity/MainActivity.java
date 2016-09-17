package com.tory.noname.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.MenuItem;

import com.tory.noname.R;
import com.tory.noname.activity.base.BaseActivity;
import com.tory.noname.fragment.BaseFragment;
import com.tory.noname.utils.L;
import com.tory.noname.utils.SettingHelper;
import com.tory.noname.utils.Utilities;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private SettingHelper mSettingHelper;

    public static final String KEY_SHOW_TAG = "key_show_tag";
    private String mShowingFragmentTag;

    private SparseArray<String> mTagMenuIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState!= null){
            mShowingFragmentTag = savedInstanceState.getString(KEY_SHOW_TAG);
            L.d(TAG,"onCreate mShowingFragmentTag:"+mShowingFragmentTag);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        initToolbar();
        setToolbarScrolled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void doBusiness() {
        mSettingHelper = SettingHelper.getInstance(this);
        initMenu();
        initTagMenuIds();
        initDefalutFragment();
    }

    private void initMenu() {
        boolean nightMode = mSettingHelper.isNightModeNow();
        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.nav_mode_change);
        changeNightModeMenu(menuItem, nightMode);
    }

    private void changeNightModeMenu(MenuItem menuItem, boolean nightMode) {
        int modeResId = nightMode ? R.drawable.ic_mode_night : R.drawable.ic_mode_day;
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), modeResId, getTheme());
        menuItem.setIcon(drawable);
        menuItem.setTitle(nightMode ? R.string.nav_mode_night : R.string.nav_mode_day);
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_ganhuo:
            case R.id.nav_setting:
            case R.id.nav_bili:
                String tag = mTagMenuIds.get(id);
                showFragmentAndHideOther(tag);
                break;
            case R.id.nav_mode_change:
                boolean nightMode = mSettingHelper.isNightModeNow();
                Utilities.setNightMode(this, !nightMode, false);
                changeNightModeMenu(item, !nightMode);
                break;
        }

        item.setChecked(true);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        L.d(TAG,"onSaveInstanceState mShowingFragmentTag: "+mShowingFragmentTag);
        outState.putString(KEY_SHOW_TAG,mShowingFragmentTag);
        super.onSaveInstanceState(outState);
    }

    private void initTagMenuIds() {
        mTagMenuIds = new SparseArray<>();
        mTagMenuIds.put(R.id.nav_ganhuo,BaseFragment.TAG_GANK_LIST_FRAGMENT);
        mTagMenuIds.put(R.id.nav_setting,BaseFragment.TAG_SETTING_FRAGMENT);
        mTagMenuIds.put(R.id.nav_bili,BaseFragment.TAG_BILI_FRAGMENT);
    }


    private void initDefalutFragment(){
        if(mShowingFragmentTag == null){
            mShowingFragmentTag = BaseFragment.TAG_GANK_LIST_FRAGMENT;
        }

        int size = mTagMenuIds.size();
        for(int i = 0 ; i <= size; i++){
            int menuId = mTagMenuIds.keyAt(i);
            String tag = mTagMenuIds.valueAt(i);
            if(mShowingFragmentTag.equals(tag)){
                showFragment(tag,true,false);
                mNavigationView.setCheckedItem(menuId);
            }else{
                showFragment(tag,false,false);
            }
        }
    }

    public void showGankListFragment() {
        showFragmentAndHideOther(BaseFragment.TAG_GANK_LIST_FRAGMENT);
    }

    private void showSettingFragment() {
        showFragmentAndHideOther(BaseFragment.TAG_SETTING_FRAGMENT);
    }

    public void showFragmentAndHideOther(String tag){
        hideShowingFragment(tag);
        showFragment(tag,true,false);
        mShowingFragmentTag = tag;
    }

    private void hideShowingFragment(String tag){
        if(mShowingFragmentTag != null && !TextUtils.equals(tag,mShowingFragmentTag)){
            L.d(TAG,"hideShowingFragment: "+mShowingFragmentTag);
            showFragment(mShowingFragmentTag,false,false);
        }
    }

    public void showFragment(String tag,boolean show){
        showFragment(tag,show,false);
    }

    public void showFragment(String tag, boolean show, boolean executeImmediately) {
        //Trace.beginSection("showFragment - " + tag);
        final FragmentManager fm = getSupportFragmentManager();

        if (fm == null) {
            L.w(TAG, "Fragment manager is null for : " + tag);
            return;
        }

        Fragment fragment = fm.findFragmentByTag(tag);
        if (!show && fragment == null) {
            // Nothing to show, so bail early.
            return;
        }

        final FragmentTransaction transaction = fm.beginTransaction();
        if (show) {
            if (fragment == null) {
                L.d(TAG, "showFragment: fragment need create: " + tag);
                fragment = BaseFragment.createNewFragmentForTag(tag);
                transaction.add(R.id.frame_content, fragment, tag);
            } else {
                L.d(TAG, "showFragment: fragment is all ready created " + tag);
                transaction.show(fragment);
            }
        } else {
            transaction.hide(fragment);
        }

        transaction.commitAllowingStateLoss();
        if (executeImmediately) {
            fm.executePendingTransactions();
        }
        //Trace.endSection();
    }
}
