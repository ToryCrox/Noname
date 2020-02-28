package com.tory.demo.webview

import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.tencent.sonic.sdk.SonicConfig
import com.tencent.sonic.sdk.SonicEngine
import com.tencent.sonic.sdk.SonicSession
import com.tory.demo.webview.sonic.SonicRuntimeImpl
import com.tory.library.webview.X5WebViewActivity
import kotlinx.android.synthetic.main.activity_web_view.view.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020-01-12
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020-01-12 xutao 1.0
 * Why & What is modified:
 */
class CutsomWebViewActivity: X5WebViewActivity() {

    private var sonicSession: SonicSession? = null

    override fun initView() {
        super.initView()
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(SonicRuntimeImpl(application), SonicConfig.Builder().build())
        }
        mWebView.webViewClient = SonicWebClient()

    }

    override fun initWebViewSetting(webView: WebView?) {
        super.initWebViewSetting(webView)

    }


    class SonicWebClient: WebViewClient() {

    }
}