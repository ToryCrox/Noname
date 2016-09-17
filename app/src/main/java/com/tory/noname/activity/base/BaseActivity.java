package com.tory.noname.activity.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tory.noname.R;
import com.tory.noname.utils.StatusBarHelper;
import com.tory.noname.utils.Utilities;

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
    }

    public void setToolbarTitle(String title) {
        if (mToolbar != null) {
            this.mTitle = title;
            //  mToolbar.setTitle(title);
            getSupportActionBar().setTitle(mTitle);
        }
    }

    public String getToolbarTitle(){
        return mTitle;
    }

    protected void setToolbarScrolled(boolean scrolled){
        if(mToolbar != null && mToolbar.getLayoutParams() instanceof AppBarLayout.LayoutParams){
            AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
            if(scrolled){
                lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        |AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            }else{
                lp.setScrollFlags(0);
            }

            mToolbar.setLayoutParams(lp);
        }
    }

    protected void setDisplayHomeAsUpEnabled(boolean enable){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enable);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    protected void setStatusBar(){
        if(Utilities.ATLEAST_LOLLIPOP) return;
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        int statusbarColor = ContextCompat.getColor(this,R.color.statusbarColor);
        if(drawerLayout == null){
            StatusBarHelper.setColor(this,statusbarColor);
        }else{
            StatusBarHelper.setColorForDrawerLayout(this,drawerLayout,statusbarColor);
        }
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

    /**
     * Actionbar点击返回键关闭事件
     */

}
