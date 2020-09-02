package com.tory.library.utils.diskcache;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tory.library.log.LogUtils;
import com.tory.library.utils.TimeRecorder;
import com.tory.library.utils.lrucache.DiskLruCache;
import com.tory.library.widget.ThreadUtils;

import java.io.File;
import java.io.IOException;

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
public class DiskCacheManager {

    private static final String TAG = "DiskCache";

    private static DiskCacheManager sInstance = new DiskCacheManager();

    public static DiskCacheManager getInstance() {
        return sInstance;
    }

    private DiskCacheConfig mConfig;
    private volatile DiskLruCache mLruCache;

    private IFileOperator mFileOperator;

    public void initialize(@NonNull DiskCacheConfig config, boolean isPreload) {
        this.mConfig = config;
        this.mFileOperator = config.isNio() ? new ChannelFileOperator() : new IoFileOperator();

        if (!isPreload) return;
        ThreadUtils.execute(() -> {
            try {
                getDiskLruCache();
            } catch (IOException e) {
                LogUtils.e(TAG + " getDiskLruCache", e);
            }
        });
    }

    private void checkInitialized() {
        if (mConfig == null) {
            throw new RuntimeException(TAG + " please initialize it !!!");
        }
    }

    private void loge(String msg, Throwable e) {
        LogUtils.e(TAG, msg, e);
    }

    @Nullable
    private File getCacheDir() {
        File systemCache = mConfig.isIsExternal() ?
                mConfig.getContext().getExternalCacheDir() : mConfig.getContext().getCacheDir();
        File cacheDir = new File(systemCache,
                CacheConstants.CACHE_DIR_NAME + mConfig.getDirSuffix());
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

    private DiskLruCache getDiskLruCache() throws IOException {
        if (mLruCache == null) {
            synchronized (this){
                if (mLruCache == null){
                    TimeRecorder.begin("createDiskLruCache");
                    mLruCache = createDiskLruCache();
                    TimeRecorder.end("createDiskLruCache");
                }
            }

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
            LogUtils.e(TAG , " write cacheKey: " + cacheKey, e);
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
            TimeRecorder.begin("read#" + cacheKey);
            T data = CacheUtil.fromJson(str, clazz);
            TimeRecorder.end("read#" + cacheKey, "from json, clazz:" + clazz.getSimpleName());
            return data;
        } catch (IOException e) {
            LogUtils.e(TAG , " read cacheKey: " + cacheKey, e);
            return null;
        }
    }

    /**
     * 清除所有缓存, 为同步方法
     * @param context
     * @throws IOException
     */
    public static void deleteAll(@NonNull Context context) {
        deleteCache(context.getCacheDir());
        deleteCache(context.getExternalCacheDir());
    }

    private static void deleteCache(@NonNull File cacheDir){
        for (File file : cacheDir.listFiles()) {
            if (file.isDirectory() && file.getName() != null
                    && file.getName().startsWith(CacheConstants.CACHE_DIR_NAME)){
                CacheUtil.deleteDir(cacheDir);
            }
        }
    }
}
