package com.tory.library.webview;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.WindowManager;

import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tory.library.R;
import com.tory.library.base.BaseWebViewActivity;

import org.jetbrains.annotations.Nullable;

public class X5WebViewActivity extends BaseWebViewActivity<WebView> {

    @Override
    public int getLayoutId() {
        return R.layout.activity_web_view_x5;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);//（这个对宿主没什么影响，建议声明）
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mWebView.setWebViewClient(new MWebViewClient());
        mWebView.setWebChromeClient(new MWebChromeClient());
        mSwipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    protected boolean canGoBanck() {
        return mWebView.canGoBack();
    }

    @Override
    protected String getWebUrl() {
        return mWebView.getUrl();
    }

    @Override
    protected void goBack() {
        mWebView.goBack();
    }

    @Override
    protected void initWebViewSetting(WebView webView) {
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        IX5WebViewExtension settingsExtension = webView.getX5WebViewExtension();
        if (settingsExtension != null) {
            settingsExtension.setScrollBarFadingEnabled(false);
        }
    }

    protected class MWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            return interceptUrlLoading(url);
        }
    }

    protected class MWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressChanged(newProgress);
        }

        //获取Web页中的title用来设置自己界面中的title
        //当加载出错的时候，比如无网络，这时onReceiveTitle中获取的标题为 找不到该网页,
        //因此建议当触发onReceiveError时，不要使用获取到的title
        @Override
        public void onReceivedTitle(WebView view, String title) {
            //super.onReceivedTitle(view, title);
            receivedTitle(title);

        }

    }
}
