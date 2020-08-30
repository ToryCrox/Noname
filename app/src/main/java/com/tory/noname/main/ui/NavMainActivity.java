package com.tory.noname.main.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.tory.library.base.BaseActivity;
import com.tory.library.base.WeekHandler;
import com.tory.library.utils.DensityUtils;
import com.tory.library.utils.SystemBarUtils;
import com.tory.noname.R;
import com.tory.noname.bili.PartitionListFragment;
import com.tory.noname.gank.GankListFragment;
import com.tory.noname.main.utils.GlideEngine;
import com.tory.noname.main.utils.L;
import com.tory.library.utils.SettingHelper;
import com.tory.library.utils.Utilities;
import com.tory.noname.ss.SsListFragment;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NavMainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private SettingHelper mSettingHelper;

    public static final String KEY_SHOW_TAG = "key_show_tag";
    private String mShowingFragmentTag;

    private SparseArray<String> mTagMenuIds;


    public static final int MSG_WHAT_SHOW_FRAGMENT = 1;

    private static class MainActivityHandler extends WeekHandler<NavMainActivity>{

        public MainActivityHandler(NavMainActivity activity) {
            super(activity);
        }

        @Override
        public void handleMessage(NavMainActivity activity, Message msg) {
            switch (msg.what){
                case MSG_WHAT_SHOW_FRAGMENT:{
                    int id = msg.arg1;
                    activity.showFragmentById(id);
                }
            }
        }
    }

    private Handler mHandler = new MainActivityHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState!= null){
            mShowingFragmentTag = savedInstanceState.getString(KEY_SHOW_TAG);
            L.d(TAG,"onCreate mShowingFragmentTag:"+mShowingFragmentTag);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setThemeColor() {
        SystemBarUtils.translucentStatusBar(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_nav_main;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, getToolbar(), R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getHeaderView(0).findViewById(R.id.avatar).setOnClickListener(v -> {
            PictureSelector.create(NavMainActivity.this)
                    .openGallery(PictureMimeType.ofImage())
                    .isCamera(false)
                    .loadImageEngine(GlideEngine.createGlideEngine())
                    .enableCrop(true)
                    .cropWH(DensityUtils.getScreenW(this), DensityUtils.getScreenH(this))
                    .rotateEnabled(false)
                    .freeStyleCropEnabled(true)
                    .forResult(PictureConfig.CHOOSE_REQUEST);
        });
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mSettingHelper = SettingHelper.getInstance(this);
        initMenu();
        initTagMenuIds();
        initDefalutFragment();
    }

    protected void setToolbarScrolled(boolean scrolled){

        if(getToolbar() != null && getToolbar().getLayoutParams() instanceof AppBarLayout.LayoutParams){
            AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) getToolbar().getLayoutParams();
            if(scrolled){
                lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        |AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            }else{
                lp.setScrollFlags(0);
            }

            getToolbar().setLayoutParams(lp);
        }
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
            case R.id.nav_bili:
            case R.id.nav_setting:
            case R.id.nav_gallery:
                Message msg = mHandler.obtainMessage(MSG_WHAT_SHOW_FRAGMENT,id,0);
                mHandler.sendMessageDelayed(msg,250);
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
        mTagMenuIds.put(R.id.nav_ganhuo,GankListFragment.FRAGMENT_TAG);
        mTagMenuIds.put(R.id.nav_bili,PartitionListFragment.FRAGMENT_TAG);
        mTagMenuIds.put(R.id.nav_setting,SettingsFragment.FRAGMENT_TAG);
        mTagMenuIds.put(R.id.nav_gallery, SsListFragment.FRAGMENT_TAG);
    }

    private void initDefalutFragment(){
        if(mShowingFragmentTag == null){
            mShowingFragmentTag = GankListFragment.FRAGMENT_TAG;
        }

        int size = mTagMenuIds.size();
        for(int i = 0 ; i < size; i++){
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

    public void showFragmentById(int id){
        String tag = mTagMenuIds.get(id);
        showFragmentAndHideOther(tag);
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
    @Override
    public int getFragmentContainer(String tag) {
        return R.id.frame_content;
    }

    public Fragment createNewFragmentForTag(String tag){
        if (GankListFragment.FRAGMENT_TAG.equals(tag)) {
            return new GankListFragment();
        }else if(SettingsFragment.FRAGMENT_TAG.equals(tag)) {
            return SettingsFragment.newInstance();
        }else if(PartitionListFragment.FRAGMENT_TAG.equals(tag)){
            return new PartitionListFragment();
        }else if(SsListFragment.FRAGMENT_TAG.equals(tag)){
            return new SsListFragment();
        }
        throw new IllegalStateException("Unexpected fragment: " + tag);
    }

    protected void startActivity(Class cls){

        Intent intent = new Intent(this,cls);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回五种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 4.media.getOriginalPath()); media.isOriginal());为true时此字段才有值
                    // 5.media.getAndroidQToPath();为Android Q版本特有返回的字段，此字段有值就用来做上传使用
                    // 如果同时开启裁剪和压缩，则取压缩路径为准因为是先裁剪后压缩
                    for (LocalMedia media : selectList) {
                        Log.i(TAG, "压缩::" + media.getCompressPath());
                        Log.i(TAG, "原图::" + media.getPath());
                        Log.i(TAG, "裁剪::" + media.getCutPath());
                        Log.i(TAG, "是否开启原图::" + media.isOriginal());
                        Log.i(TAG, "原图路径::" + media.getOriginalPath());
                        Log.i(TAG, "Android Q 特有Path::" + media.getAndroidQToPath());
                    }
                    break;
            }
        }
    }
}
