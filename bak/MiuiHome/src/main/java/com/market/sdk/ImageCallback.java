package com.market.sdk;

import android.net.Uri;

public interface ImageCallback {
    void onImageLoadFailed(String str);

    void onImageLoadSuccess(String str, Uri uri);
}
