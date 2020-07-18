package com.tory.demo.diskcache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tory.demo.diskcache.lrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import kotlin.text.Charsets;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/10
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/10 xutao 1.0
 * Why & What is modified:
 */
class DiskCache {

    private static final String TAG = "DiskCache";

    private static DiskCache sInstance = new DiskCache();

    public static DiskCache getInstance() {
        return sInstance;
    }

    private DiskCacheConfig mConfig;
    private DiskLruCache mLruCache;
    private IFileOperator mFileOperator;

    public void initialize(DiskCacheConfig config) {
        this.mConfig = config;
        this.mFileOperator = config.isNio() ? new ChannelFileOperator() : new IoFileOperator();
    }

    private void checkInitialized() {
        if (mConfig == null) {
            throw new RuntimeException(TAG + " please initialize it !!!");
        }
    }

    private void loge(String msg, Throwable e) {
        Log.d(TAG, msg, e);
    }

    @Nullable
    private File getCacheDir() {
        File systemCache = mConfig.isIsExternal() ?
                mConfig.getContext().getExternalCacheDir() : mConfig.getContext().getCacheDir();
        File cacheDir = new File(systemCache, mConfig.getBaseDirName());
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            return null;
        }
        return cacheDir;
    }


    /**
     * 创建DiskCache
     *
     * @return
     * @throws IOException
     */
    private DiskLruCache createDiskLruCache() throws IOException {
        File cacheDir = getCacheDir();
        if (cacheDir == null) return null;
        return DiskLruCache.open(cacheDir, mConfig.getVersion(), 1,
                mConfig.getMaxCacheSize());
    }

    private synchronized DiskLruCache getDiskLruCache() throws IOException {
        if (mLruCache == null) {
            TimeRecorder.begin("createDiskLruCache");
            mLruCache = createDiskLruCache();
            TimeRecorder.end("createDiskLruCache");
        }
        return mLruCache;
    }

    /**
     *
     * @param cacheKey
     * @param data 仅支持String类型和自定义的类, 不支持List, Set等
     * @return
     */
    public boolean write(@NonNull String cacheKey, @NonNull Object data) {
        checkInitialized();
        DiskLruCache.Editor editor = null;
        try {
            DiskLruCache lruCache = getDiskLruCache();
            TimeRecorder.begin("write#" + cacheKey);
            String key = CacheUtil.md5(cacheKey);
            editor = lruCache.edit(key);
            File file = editor.getFile(0);
            mFileOperator.write(file, CacheUtil.toJson(data));
            editor.commit();
            TimeRecorder.end("write#" + cacheKey);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (editor != null) {
                editor.abortUnlessCommitted();
            }
        }
    }

    /**
     *
     * @param cacheKey
     * @param clazz 仅支持String类型和自定义的类
     * @param <T>
     * @return
     */
    @Nullable
    public <T> T read(@NonNull String cacheKey, @NonNull Class<T> clazz) {
        checkInitialized();
        try {
            DiskLruCache lruCache = getDiskLruCache();
            TimeRecorder.begin("read#" + cacheKey);
            String key = CacheUtil.md5(cacheKey);
            DiskLruCache.Value value = lruCache.get(key);
            if (value == null) return null;
            File file = value.getFile(0);
            String str = mFileOperator.read(file);
            TimeRecorder.end("read#" + cacheKey);
            if (str == null) return null;
            T data = CacheUtil.fromJson(str, clazz);
            return data;
        } catch (IOException e) {
            return null;
        }
    }
}
