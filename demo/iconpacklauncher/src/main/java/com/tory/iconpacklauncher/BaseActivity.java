package com.tory.iconpacklauncher;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.os.TraceCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

public abstract class BaseActivity extends AppCompatActivity  {

    protected final String TAG = this.getClass().getSimpleName();

    protected Toolbar mToolbar;
    protected String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layoutId = bindLayout();
        if(layoutId>0){
            setContentView(layoutId);
        }
        setThemeColor();
        initToolbar();
        initView();
        doBusiness();
    }

    protected void setThemeColor(){
        setStatusBar();
    }

    protected void initToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar != null){
            setSupportActionBar(mToolbar);
        }
        setDisplayHomeAsUpEnabled(true);
        setToolbarBackpress();
    }

    //监听toolbar左上角后退按钮
    public void setToolbarBackpress() {
        if(mToolbar!=null){
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

    }

    protected void setToolbarScrolled(boolean scrolled){
        if(mToolbar != null && mToolbar.getLayoutParams() instanceof AppBarLayout.LayoutParams){
            AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
            if(scrolled){
                lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        |AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                        |AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            }else{
                lp.setScrollFlags(0);
            }

            mToolbar.setLayoutParams(lp);
        }
    }

    public void setToolbarTitle(String title) {
        if (mToolbar != null) {
            this.mTitle = title;
            //  mToolbar.setTitle(title);
            getSupportActionBar().setTitle(mTitle);
        }
    }

    public Toolbar getToolbar(){
        return mToolbar;
    }

    public String getToolbarTitle(){
        return mTitle;
    }

    protected void setDisplayHomeAsUpEnabled(boolean enable){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enable);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    protected void setStatusBar(){
    }


    public void showFragment(String tag, boolean show, boolean executeImmediately) {
        TraceCompat.beginSection("showFragment - " + tag);
        FragmentManager fm = getSupportFragmentManager();
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
                fragment = createNewFragmentForTag(tag);
                transaction.add(getFragmentContainer(tag), fragment, tag);
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
        TraceCompat.endSection();
    }

    private void showOsFragment(String tag, boolean show, boolean executeImmediately){

    }

    public Object getFragmentManagerByTag(String tag){
       return getSupportFragmentManager();
    }

    public int getFragmentContainer(String tag){
        throw new IllegalStateException("Unexpected fragmentContainer: " + tag);
    }

    public Fragment createNewFragmentForTag(String tag) {
        throw new IllegalStateException("Unexpected fragment: " + tag);
    }

    /**
     * 绑定渲染视图的布局文件
     *
     * @return 布局文件资源id
     */
    @LayoutRes
    public abstract int bindLayout();

    /**
     * 初始化控件
     */
    public abstract void initView();

    /**
     * 业务处理操作（onCreate方法中调用）
     *
     */
    public abstract void doBusiness();
}
