package com.market.sdk;

import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import com.market.sdk.IImageCallback.Stub;
import com.market.sdk.utils.CollectionUtils;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImageManager {
    private static ConcurrentHashMap<String, Uri> sCachedUri = new ConcurrentHashMap();
    private static Map<String, HashSet<ImageCallback>> sLoadingIcons = CollectionUtils.newHashMap();

    private static class IconLoadTask {
        private String mAppId;
        private String mMask;
        private IImageCallback mResponse;

        public IconLoadTask(String url, String mask, ImageCallback callback) {
            this.mAppId = url;
            this.mMask = mask;
            String key = this.mAppId;
            if (!TextUtils.isEmpty(mask)) {
                key = key + "_" + mask;
            }
            this.mResponse = new ImageLoadResponse(key);
        }

        public void start() {
            new RemoteMethodInvoker<Void>() {
                public Void innerInvoke(IMarketService s) throws RemoteException {
                    s.loadIcon(IconLoadTask.this.mAppId, IconLoadTask.this.mMask, IconLoadTask.this.mResponse);
                    return null;
                }
            }.invokeAsync();
        }
    }

    private static class ImageLoadResponse extends Stub {
        private String mKey;

        public ImageLoadResponse(String key) {
            this.mKey = key;
        }

        public void onImageLoadSuccess(String url, Uri uri) {
            ImageManager.sCachedUri.put(this.mKey, uri);
            synchronized (ImageManager.sLoadingIcons) {
                Set<ImageCallback> callbacks = (Set) ImageManager.sLoadingIcons.remove(this.mKey);
                if (!CollectionUtils.isEmpty(callbacks)) {
                    for (ImageCallback callback : callbacks) {
                        callback.onImageLoadSuccess(url, uri);
                    }
                }
            }
        }

        public void onImageLoadFailed(String url) {
            synchronized (ImageManager.sLoadingIcons) {
                Set<ImageCallback> callbacks = (Set) ImageManager.sLoadingIcons.remove(this.mKey);
                if (!CollectionUtils.isEmpty(callbacks)) {
                    for (ImageCallback callback : callbacks) {
                        callback.onImageLoadFailed(url);
                    }
                }
            }
        }
    }

    private static class ImageLoadTask {
        private int mMaxHeight;
        private int mMaxWidth;
        private IImageCallback mResponse = new ImageLoadResponse(this.mUrl);
        private String mUrl;

        public ImageLoadTask(String url, int maxWidth, int maxHeight, ImageCallback callback) {
            this.mUrl = url;
            this.mMaxWidth = maxWidth;
            this.mMaxHeight = maxHeight;
        }

        public void start() {
            new RemoteMethodInvoker<Void>() {
                public Void innerInvoke(IMarketService s) throws RemoteException {
                    s.loadImage(ImageLoadTask.this.mUrl, ImageLoadTask.this.mMaxWidth, ImageLoadTask.this.mMaxHeight, ImageLoadTask.this.mResponse);
                    return null;
                }
            }.invokeAsync();
        }
    }

    public static void loadIcon(String appId, String mask, ImageCallback callback) {
        String key = appId;
        if (!TextUtils.isEmpty(mask)) {
            key = key + "_" + mask;
        }
        Uri uri = (Uri) sCachedUri.get(key);
        if (uri == null || !new File(uri.getPath()).exists()) {
            synchronized (sLoadingIcons) {
                HashSet<ImageCallback> callbacks = (HashSet) sLoadingIcons.get(key);
                boolean needNewTask = !sLoadingIcons.containsKey(key);
                if (callbacks == null) {
                    callbacks = CollectionUtils.newHashSet();
                    sLoadingIcons.put(key, callbacks);
                }
                callbacks.add(callback);
                if (needNewTask) {
                    new IconLoadTask(appId, mask, callback).start();
                }
            }
            return;
        }
        callback.onImageLoadSuccess(appId, uri);
    }

    public static void loadImage(String url, int maxWidth, int maxHeight, ImageCallback callback) {
        Uri uri = (Uri) sCachedUri.get(url);
        if (uri == null || !new File(uri.getPath()).exists()) {
            String key = url;
            synchronized (sLoadingIcons) {
                HashSet<ImageCallback> callbacks = (HashSet) sLoadingIcons.get(key);
                boolean needNewTask = !sLoadingIcons.containsKey(key);
                if (callbacks == null) {
                    callbacks = CollectionUtils.newHashSet();
                    sLoadingIcons.put(key, callbacks);
                }
                callbacks.add(callback);
                if (needNewTask) {
                    new ImageLoadTask(url, maxWidth, maxHeight, callback).start();
                }
            }
            return;
        }
        callback.onImageLoadSuccess(url, uri);
    }
}
