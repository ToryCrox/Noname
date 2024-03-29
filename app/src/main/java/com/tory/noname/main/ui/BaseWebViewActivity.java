package com.tory.noname.main.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.CallSuper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.tory.library.base.BaseActivity;
import com.tory.library.log.LogUtils;
import com.tory.noname.R;
import com.tory.noname.main.utils.L;
import com.tory.library.utils.Utilities;

/**
 * @Author: Tory
 * Create: 2016/9/30
 */
public abstract class BaseWebViewActivity<V extends View> extends BaseActivity {

    protected V mWebView;
    protected ProgressBar mProgressBar;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected String mUrl;


    @SuppressLint("WrongViewCast")
    @CallSuper
    @Override
    public void initView(Bundle bundle) {
        mUrl = getIntent().getStringExtra(Utilities.WEB_URL);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressBar.setMax(100);
        mWebView = (V) findViewById(R.id.webView);
        initWebViewSetting(mWebView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        Utilities.initSwipeRefresh(mSwipeRefreshLayout);

        initNavIcon();

        loadUrl(mUrl);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUrl(getWebUrl());
            }
        });
    }

    protected abstract void loadUrl(String url);

    protected abstract boolean canGoBack();

    protected abstract String getWebUrl();

    protected abstract void goBack();

    protected abstract void initWebViewSetting(V webView) ;

    protected void initNavIcon() {
        /*if (canGoBanck()) {
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        } else {
            mToolbar.setNavigationIcon(R.drawable.abc_);
        }*/
    }

    protected void progressChanged(int newProgress){
        //super.onProgressChanged(view, newProgress);
        mProgressBar.setProgress(newProgress);
        if (newProgress == 100) {
            mSwipeRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void receivedTitle(String title) {
        initNavIcon();
        getToolbar().setTitle(title);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        if (canGoBack()) {
            goBack();
            initNavIcon();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_in_brower:
                Utilities.openInBrowser(this,getWebUrl());
                break;
            case R.id.action_url_copy:
                Utilities.copyToClipboar(this,getWebUrl());
                break;
            case R.id.action_share:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean interceptUrlLoading(String url){
        if (url.startsWith("http")) {
            loadUrl(url);
            return true;
        } else {
            return startActionView(this, url);
        }

    }

    public boolean startActionView(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            compeletIntentWithUrl(intent, url);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            L.e(TAG, "open url error:" + url);
            return false;
        }
    }

    private void compeletIntentWithUrl(Intent intent, String url) {
        if(url.startsWith("intent://")){
            LogUtils.d(TAG,"compeletIntentWithUrl ");
            String[] strs = url.split(";");
            String uri = null;
            String scheme=null;
            for (String str :  strs){
                if(str.startsWith("intent://")){
                    uri = str;
                }else if(str.startsWith("package=")){
                    intent.setPackage(str.split("=")[1]);
                }else if(str.startsWith("scheme=")){
                    scheme = str.split("=")[1];
                }
            }
            if(scheme != null && uri != null){
                uri = uri.replaceFirst("intent",scheme);
            }
            LogUtils.d(TAG,"compeletIntentWithUrl:"+uri);
            intent.setData(Uri.parse(uri));
        }else{
            intent.setData(Uri.parse(url));
        }
    }
}
