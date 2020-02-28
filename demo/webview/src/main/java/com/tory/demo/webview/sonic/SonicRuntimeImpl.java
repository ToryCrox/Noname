package com.tory.demo.webview.sonic;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.sonic.sdk.SonicConstants;
import com.tencent.sonic.sdk.SonicRuntime;
import com.tencent.sonic.sdk.SonicSessionClient;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class SonicRuntimeImpl extends SonicRuntime {
    private static final String TAG = "SonicRuntimeImpl";

    public SonicRuntimeImpl(Context context) {
        super(context);
    }

    /**
     * 获取用户UA信息
     * @return
     */
    @Override
    public String getUserAgent() {
        return "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Mobile Safari/537.36";
    }

    /**
     * 获取用户ID信息
     * @return
     */
    @Override
    public String getCurrentUserAccount() {
        return "sonic-demo-master";
    }

    @Override
    public String getCookie(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(url);
    }

    @Override
    public void log(String tag, int level, String message) {
        switch (level) {
            case Log.ERROR:
                Log.e(tag, message);
                break;
            case Log.INFO:
                Log.i(tag, message);
                break;
            default:
                Log.d(tag, message);
        }
    }

    @Override
    public Object createWebResourceResponse(String mimeType, String encoding, InputStream data, Map<String, String> headers) {
        WebResourceResponse resourceResponse =  new WebResourceResponse(mimeType, encoding, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resourceResponse.setResponseHeaders(headers);
        }
        return resourceResponse;
    }

    @Override
    public void showToast(CharSequence text, int duration) {
        Toast.makeText(context, text, duration).show();
    }

    @Override
    public void notifyError(SonicSessionClient client, String url, int errorCode) {
        Log.d(TAG, "notifyError url:" + url + ", errorCode:" + errorCode) ;
    }

    @Override
    public boolean isSonicUrl(String url) {
        return true;
    }

    @Override
    public boolean setCookie(String url, List<String> cookies) {
        if (!TextUtils.isEmpty(url) && cookies != null && cookies.size() > 0) {
            CookieManager cookieManager = CookieManager.getInstance();
            for (String cookie : cookies) {
                cookieManager.setCookie(url, cookie);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isNetworkValid() {
        return true;
    }

    @Override
    public void postTaskToThread(Runnable task, long delayMillis) {
        Thread thread = new Thread(task, "SonicThread");
        thread.start();
    }

    @Override
    public File getSonicCacheDir() {
        if (BuildConfig.DEBUG) {
            //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sonic/";
            String path = context.getExternalFilesDir("sonic").getAbsolutePath();
            File file = new File(path.trim());
            if(!file.exists()){
                file.mkdir();
            }
            return file;
        }
       return super.getSonicCacheDir();
    }

    @Override
    public File getSonicResourceCacheDir() {
        File file = context.getExternalFilesDir("SonicResource");
        if (!file.exists() && !file.mkdir()) {
            log(TAG, Log.ERROR, "getSonicResourceCacheDir error:make dir(" + file.getAbsolutePath() + ") fail!");
            notifyError(null, file.getAbsolutePath(), SonicConstants.ERROR_CODE_MAKE_DIR_ERROR);
        }
        return file;
    }

    @Override
    public String getHostDirectAddress(String url) {
        return null;
    }

    @Override
    public boolean shouldLog(int level) {
        return super.shouldLog(level);
    }
}