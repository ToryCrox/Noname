package com.tory.demo.diskcache;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.io.File;

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
class DiskCacheConfig {

    private Context mContext;
    private int mVersion = 1;
    private String mBaseDirName = "du_http";
    private long mMaxCacheSize = 50 * 1024 * 1024L;
    private boolean mIsNio = true;
    private boolean mIsExternal = false;

    private DiskCacheConfig(@NonNull Context context, int version,
                            String baseDirName, long maxCacheSize,
                            boolean isNio,boolean isExternal){
        this.mContext = context;
        this.mVersion = version;
        this.mBaseDirName = baseDirName;
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
    public String getBaseDirName() {
        return mBaseDirName;
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
        private String mBaseDirName = "du_http";
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

        public Builder setBaseDirName(@NonNull String name){
            this.mBaseDirName = name;
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
            if (TextUtils.isEmpty(mBaseDirName)){
                throw new IllegalArgumentException("mBaseDirName can not be null or empty!!!");
            }
            if (mMaxCacheSize <  10 * 1024 * 1024L){
                throw new IllegalArgumentException("mMaxCacheSize can not be low 10M!!");
            }
            if (mContext == null){
                throw new NullPointerException("mContext can not be null!!");
            }
            return new DiskCacheConfig(mContext.getApplicationContext(), mVersion,
                    mBaseDirName, mMaxCacheSize, mIsNio, mIsExternal);
        }
    }
}
