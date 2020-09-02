package com.tory.library.utils.diskcache;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
public class DiskCacheConfig {

    private Context mContext;
    private int mVersion = 1;
    private String mDirSuffix;
    private long mMaxCacheSize;
    private boolean mIsNio;
    private boolean mIsExternal;

    private DiskCacheConfig(@NonNull Context context, int version,
                            String dirSuffix, long maxCacheSize,
                            boolean isNio, boolean isExternal){
        this.mContext = context;
        this.mVersion = version;
        this.mDirSuffix = dirSuffix == null ? "" : dirSuffix;
        this.mMaxCacheSize = maxCacheSize;
        this.mIsNio = isNio;
        this.mIsExternal = isExternal;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    public int getVersion() {
        return mVersion;
    }

    @NonNull
    public String getDirSuffix() {
        return mDirSuffix;
    }

    public long getMaxCacheSize() {
        return mMaxCacheSize;
    }

    public boolean isIsExternal() {
        return mIsExternal;
    }

    public boolean isNio() {
        return mIsNio;
    }

    public static Builder newBuilder(@NonNull Context context){
        return new Builder(context);
    }

    public static class Builder{
        private Context mContext;
        private int mVersion = 1;
        private String mDirSuffix= "";
        private long mMaxCacheSize = 50 * 1024 * 1024L;
        private boolean mIsNio = true;
        private boolean mIsExternal = false;

        private Builder(@NonNull Context context){
            mContext = context;
        }

        public Builder setVersion(int version){
            this.mVersion = version;
            return this;
        }

        public Builder setDirSuffix(@Nullable String name){
            this.mDirSuffix = name;
            return this;
        }

        public Builder setMaxCacheSize(long maxCacheSize){
            this.mMaxCacheSize = maxCacheSize;
            return this;
        }

        public Builder setIsNio(boolean isNio){
            this.mIsNio = isNio;
            return this;
        }

        public Builder setIsExternal(boolean isExternal){
            this.mIsExternal = isExternal;
            return this;
        }

        public DiskCacheConfig build(){
            if (mMaxCacheSize <  10 * 1024 * 1024L){
                throw new IllegalArgumentException("mMaxCacheSize can not be low 10M!!");
            }
            if (mContext == null){
                throw new NullPointerException("mContext can not be null!!");
            }
            return new DiskCacheConfig(mContext.getApplicationContext(), mVersion,
                    mDirSuffix, mMaxCacheSize, mIsNio, mIsExternal);
        }
    }
}
