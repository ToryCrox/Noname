package com.tory.noname.utils.http;

import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.tory.library.utils.FileUtils;
import com.tory.library.utils.NetUtils;
import com.tory.noname.MApplication;
import com.tory.noname.utils.L;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.DiskLruCache;
import okhttp3.internal.Util;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSource;
import okio.Okio;
/**
 * Author: XT
 *http://www.jianshu.com/p/4c17956fe3b4
 */
public class XOkHttpUtils {
    public static final int DEFAULT_MILLISECONDS = 5000; //默认的超时时间
    public static final String CACHE_DIR_NAME = "HttpCache";
    public static final long SIZE_OF_CACHE =  10*10240*1024;//缓存10M

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "XOkHttpUtils";
    private Handler mHandler;
    private static volatile XOkHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;

    public interface HttpCallBack {
        void onLoadStart();

        void onSuccess(String result);

        void onError(Exception e);
    }

    private XOkHttpUtils() {
        mHandler = new Handler(Looper.getMainLooper());
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .cache(provideCache()) //设置缓存
                //.addNetworkInterceptor(new CacheInterceptor())
                .addNetworkInterceptor(new StethoInterceptor())
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR);
                //.build();
        mOkHttpClient = builder.build();
    }

    public OkHttpClient getOkHttpClient(){
        return mOkHttpClient;
    }

    public static XOkHttpUtils getInstance() {
        if (mInstance == null)
            synchronized (XOkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new XOkHttpUtils();
                }
            }
        return mInstance;
    }

    public String getCacheDirName(){
        return FileUtils.getCacheDir(MApplication.getInstance()).getAbsolutePath()
                + File.separator + CACHE_DIR_NAME;
    }
    /**
     * 缓存
     * http://mushuichuan.com/2016/03/01/okhttpcache/
     */
    public Cache provideCache() {
        return new Cache(new File(getCacheDirName()), SIZE_OF_CACHE);
    }

    //服务器不支持缓存情况下
    public class CacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            Response response1 = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    //cache for 30 days
                    .header("Cache-Control", "max-age=" + 3600 * 24 * 30)
                    .build();
            return response1;
        }
    }

    /**
     * http://werb.github.io/2016/07/29/%E4%BD%BF%E7%94%A8Retrofit2+OkHttp3%E5%AE%9E%E7%8E%B0%E7%BC%93%E5%AD%98%E5%A4%84%E7%90%86/
     */
    //cache
    Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR =new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            CacheControl.Builder cacheBuilder = new CacheControl.Builder();
            cacheBuilder.maxAge(0, TimeUnit.SECONDS);
            cacheBuilder.maxStale(365,TimeUnit.DAYS);
            CacheControl cacheControl = cacheBuilder.build();
            Request request = chain.request();
            if(!NetUtils.isNetworkAvailable(MApplication.getInstance())){
                request = request.newBuilder()
                        .cacheControl(cacheControl)
                        .build();
            }
            Response originalResponse = chain.proceed(request);
            if (NetUtils.isNetworkAvailable(MApplication.getInstance())) {
                int maxAge = 10; // read from cache
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public ,max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }

    };

    /**
     * 不开启异步线程
     *
     * @param request
     * @return
     * @throws IOException
     */
    public Response execute(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     *
     * @param request
     * @param responseCallBack
     */
    public void enqueue(Request request, okhttp3.Callback responseCallBack) {
        mOkHttpClient.newCall(request).enqueue(responseCallBack);

    }

    public void enqueue(Request request,final HttpCallBack callBack){
        if(callBack != null) sendStartLoadingCallback(callBack);
        enqueue(request, new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                if(call.isCanceled()) return;
                if(callBack != null) sendFailureResultCallback(callBack,e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(call.isCanceled()) return;
                final String result = response.body().string();
                if(callBack != null) sendSuccessResultCallback(callBack,result);
            }
        });
    }

    private void sendStartLoadingCallback(final HttpCallBack callBack){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onLoadStart();
            }
        });
    }

    private void sendSuccessResultCallback(final HttpCallBack callBack, final String result){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onSuccess(result);
            }
        });
    }

    private void sendFailureResultCallback(final HttpCallBack callBack,Exception e){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onError(new Exception("请求失败"));
            }
        });
    }

    /** 根据Tag取消请求 */
    public void cancelTag(Object tag) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }



    public static BaseRequest get(String url){
        return new BaseRequest(url);
    }

    /**
     * 将传递进来的参数拼接成 url
     */
    public static String createUrlFromParams(String url, Map<String, String> params) {
        if(params == null || params.isEmpty()) return url;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            if (url.indexOf('&') > 0 || url.indexOf('?') > 0) sb.append("&");
            else sb.append("?");
            for (Map.Entry<String, String> urlParams : params.entrySet()) {
                String value = urlParams.getValue();
                //对参数进行 utf-8 编码,防止头信息传中文
                String urlValue = URLEncoder.encode(value, "UTF-8");
                sb.append(urlParams.getKey()).append("=").append(urlValue).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
            L.d("createUrl:"+sb.toString());
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            L.e(TAG,"createUrlFromParams "+e.toString());
        }
        return url;
    }

    //copy from Cache
    private static final int VERSION = 201105;
    private static final int ENTRY_METADATA = 0;
    private static final int ENTRY_BODY = 1;
    private static final int ENTRY_COUNT = 2;
    /*
    *手动取缓存
    */
    public FilterInputStream getFromCache(String url) throws Exception {
        File cacheDirectory = new File(getCacheDirName());
        DiskLruCache cache = DiskLruCache.create(FileSystem.SYSTEM, cacheDirectory,
                VERSION, ENTRY_COUNT, SIZE_OF_CACHE);
        cache.flush();
        String key = Util.md5Hex(url);
        final DiskLruCache.Snapshot snapshot;
        try {
            snapshot = cache.get(key);
            if (snapshot == null) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        okio.Source source = snapshot.getSource(1) ;
        BufferedSource metadata = Okio.buffer(source);
        FilterInputStream bodyIn = new FilterInputStream(metadata.inputStream()) {
            @Override
            public void close() throws IOException {
                snapshot.close();
                super.close();
            }
        };
        return bodyIn ;
    }

    public String getStringFromCatch(String url){
        String str = null;
        try {
            str = FileUtils.readString(getFromCache(url));
        } catch (Exception e) {
            L.w("get Catch exception :" + e.toString());
        }
        return str;
    }
}
