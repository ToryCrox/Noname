package com.tory.noname.main.ui;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tory.noname.R;
import com.tory.noname.main.base.BaseWebViewActivity;

public class WebViewActivity extends BaseWebViewActivity<WebView> {

    @Override
    public int bindLayout() {
        return R.layout.activity_web_view;
    }

    @Override
    public void initView() {
        super.initView();
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        setToolbarTitle("页面加载中……");
    }

    @Override
    protected void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    protected void goBack() {
        if (mWebView != null) {
            mWebView.goBack();
        }
    }

    @Override
    protected boolean canGoBanck() {
        return mWebView != null && mWebView.canGoBack();
    }

    @Override
    protected String getWebUrl() {
        return mWebView.getUrl();
    }

    @Override
    protected void initWebViewSetting(WebView webView) {
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
            return interceptUrlLoading(url);
        }
    }

    //WebChromeClient是辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
    private class MyWebChromeClient extends WebChromeClient {
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
