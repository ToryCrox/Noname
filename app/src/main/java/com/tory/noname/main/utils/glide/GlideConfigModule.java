package com.tory.noname.main.utils.glide;

import android.content.Context;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class GlideConfigModule extends AppGlideModule {
    public static final long DISK_CACHE_SIZE = 200 * 1024 * 1024;

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        //修改默认配置，如缓存配置
        //设置磁盘缓存保存在外部存储，且指定缓存大小
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        //内存缓存配置（不建议配置，Glide会自动根据手机配置进行分配）

        //设置内存缓存大小
        //builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
        //设置Bitmap池大小
        //builder.setBitmapPool(new LruBitmapPool(bitmapPoolSize));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        //替换组件，如网络请求组件
    }

}