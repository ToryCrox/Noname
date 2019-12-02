package com.tory.netexe;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.tory.library.utils.IoUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author tory
 * @date 2019/3/28
 * @des:
 */
public class DiskCacheManager {

    private static final String TAG = "DiskCacheManager";
    public static final String CACHE_DIR = "LruCache";
    private static final int MAX_SIZE = 20 * 1024 * 1024;//10MB

    private static DiskCacheManager sInstance;
    private ExecutorService mExecutor;
    private Scheduler mScheduler;


    public static DiskCacheManager getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (DiskCacheManager.class) {
                if (sInstance == null) {
                    sInstance = new DiskCacheManager(context);
                }
            }
        }
        return sInstance;
    }


    private DiskLruCache mDiskLruCache;

    private DiskCacheManager(Context context) {
        File cacheDir = Utils.getCacheDir(context, CACHE_DIR);
        try {Executors.newSingleThreadExecutor();
            mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, MAX_SIZE);
        } catch (IOException e) {
            Log.e(TAG, "init", e);
            mDiskLruCache = null;
        }
    }

    private ExecutorService getExecutor() {
        if (mExecutor == null){
            mExecutor = Executors.newSingleThreadExecutor();
        }
        return mExecutor;
    }

    private Scheduler getScheduler() {
        if (mScheduler == null){
            mScheduler = Schedulers.from(getExecutor());
        }
        return mScheduler;
    }

    /**
     * 获取缓存 editor
     *
     * @param key 缓存的key
     * @return editor
     * @throws IOException
     */
    private DiskLruCache.Editor edit(String key) throws IOException {
        key = Utils.hashKeyForDisk(key); //存取的 key
        DiskLruCache.Editor editor = null;
        if (mDiskLruCache != null) {
            editor = mDiskLruCache.edit(key);
        }
        return editor;
    }

    private void safeAbort(@Nullable DiskLruCache.Editor editor){
        if (editor != null){
            try {
                editor.abort();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void safeFlush() {
        try {
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(@NonNull String key, String value) {
        if (mDiskLruCache == null) return;
        DiskLruCache.Editor editor = null;
        BufferedWriter writer = null;
        OutputStream os = null;
        try {
            editor = edit(key);
            if (editor == null){
                return;
            }
            //os不能关闭
            os = editor.newOutputStream(0);
            writer = new BufferedWriter(new OutputStreamWriter(os) );
            writer.write(value);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            safeAbort(editor);
        } finally {
            IoUtils.closeSilently(writer);
            safeFlush();
        }
    }

    public void put(@NonNull String key, Object obj){
        put(key, Utils.toJson(obj));
    }


    /**
     * 根据 key 获取缓存缩略
     *
     * @param key 缓存的key
     * @return Snapshot
     */
    @Nullable
    private DiskLruCache.Snapshot snapshot(String key) {
        if (mDiskLruCache != null) {
            try {
                return mDiskLruCache.get(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取 缓存数据的 InputStream
     *
     * @param key cache'key
     * @return InputStream
     */
    private InputStream getCacheInputStream(String key) {
        key = Utils.hashKeyForDisk(key);
        DiskLruCache.Snapshot snapshot = snapshot(key);
        if (snapshot != null) {
            return snapshot.getInputStream(0);
        } else {
            return null;
        }
    }

    public String getString(String key){
        InputStream is = getCacheInputStream(key);
        if (is == null){
            return null;
        }
        String value = Utils.readString(is);
        Utils.closeSilently(is);
        return value;
    }

    public void apply(String key, String value){
        getExecutor().execute(() -> put(key, value));
    }

    public void apply(String key, Object value){
        getExecutor().execute(() -> put(key, value));
    }

    public <T> Observable<T> getObservable(String key, @NonNull Class<T> clazz){
        return Observable.just(key)
                .map(this::getString)
                .map(s -> Utils.fromJson(key, clazz))
                .subscribeOn(getScheduler())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public <T> Observable<List<T>> getObservableList(String key, @NonNull Class<T> clazz){
        return Observable.just(key)
                .map(this::getString)
                .map(s -> Utils.fromJsonList(s, clazz))
                .subscribeOn(getScheduler())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
