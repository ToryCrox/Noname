package com.tory.noname.activity.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tory.noname.R;

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
        boolean preL = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;

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

    /**
     * 绑定渲染视图的布局文件
     *
     * @return 布局文件资源id
     */
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
