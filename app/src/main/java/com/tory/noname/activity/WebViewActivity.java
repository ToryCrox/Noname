package com.tory.noname.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tory.noname.R;
import com.tory.noname.activity.base.BaseActivity;
import com.tory.noname.utils.L;
import com.tory.noname.utils.Utilities;

public class WebViewActivity extends BaseActivity {

    public static final String WEB_URL = "web_url";
    public static final String ACTION = "com.tory.action.WEB_VIEW";

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mUrl;

    @Override
    public int bindLayout() {
        return R.layout.activity_web_view;
    }

    @Override
    public void initView() {
        initToolbar();
        setDisplayHomeAsUpEnabled(true);
        setToolbarScrolled(true);
        mUrl = getIntent().getStringExtra(WEB_URL);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressBar.setMax(100);
        mWebView = (WebView) findViewById(R.id.webView);
        initWebViewSetting(mWebView);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        Utilities.initSwipeRefresh(mSwipeRefreshLayout);

        setToolbarTitle("页面加载中……");
        //监听toolbar左上角后退按钮
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initNavIcon();
    }

    @Override
    public void doBusiness() {
        mWebView.loadUrl(mUrl);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.loadUrl(mWebView.getUrl());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (canGoBanck()) {
            goBack();
            initNavIcon();
        } else {
            finish();
        }
    }

    private void goBack() {
        if (mWebView != null) {
            mWebView.goBack();
        }
    }

    private boolean canGoBanck() {
        return mWebView != null && mWebView.canGoBack();
    }

    private void initWebViewSetting(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webView.requestFocusFromTouch();//支持获取手势焦点，输入用户名、密码或其他
        webSettings.setJavaScriptEnabled(true);//支持js
        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true);  //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setDefaultFontSize(18);


        webSettings.setSupportZoom(true);  //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。
        //若上面是false，则该WebView不可缩放，这个不管设置什么都不能缩放。

        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
        webSettings.supportMultipleWindows();  //多窗口
        // webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //关闭webview中缓存
        webSettings.setAllowFileAccess(true);  //设置可以访问文件
        webSettings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true);  //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

    }

    //WebViewClient就是帮助WebView处理各种通知、请求事件的。
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http")) {
                view.loadUrl(url);
            } else {
                return startActionView(WebViewActivity.this, url);
            }
            return true;

        }
    }

    //WebChromeClient是辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //super.onProgressChanged(view, newProgress);
            mProgressBar.setProgress(newProgress);
            if (newProgress == 100) {
                mSwipeRefreshLayout.setRefreshing(false);
                mProgressBar.setVisibility(View.GONE);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        //获取Web页中的title用来设置自己界面中的title
        //当加载出错的时候，比如无网络，这时onReceiveTitle中获取的标题为 找不到该网页,
        //因此建议当触发onReceiveError时，不要使用获取到的title
        @Override
        public void onReceivedTitle(WebView view, String title) {
            //super.onReceivedTitle(view, title);
            initNavIcon();
            mToolbar.setTitle(title);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


    private void initNavIcon() {
        if (canGoBanck()) {
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        } else {
            mToolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_open_in_brower:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mWebView.getUrl()));
                startActivity(intent);
                break;
            case R.id.action_url_copy:
                Utilities.copyToClipboar(this, mUrl);
                break;
            case R.id.action_share:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean startActionView(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            compeletIntentWithUrl(intent, url);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            L.w(TAG, "open url error:" + url);
            return false;
        }
    }

    private void compeletIntentWithUrl(Intent intent, String url) {
        if(url.startsWith("intent://")){
            L.d(TAG,"compeletIntentWithUrl ");
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
            L.d(TAG,"compeletIntentWithUrl:"+uri);
            intent.setData(Uri.parse(uri));
        }else{
            intent.setData(Uri.parse(url));
        }
    }
}
