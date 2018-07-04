package com.miui.home.launcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.market.sdk.AdsBannerInfo;
import com.miui.home.launcher.common.Utilities;
import java.util.List;

public class RecommendBannerScreenView extends LinkedScreenView implements OnClickListener {
    public RecommendBannerScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecommendBannerScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
        setPushGestureEnabled(false);
        setScreenTransitionType(10);
        setScreenLayoutMode(1);
        ignoreCenterY(true);
    }

    public void loadContent(List<AdsBannerInfo> list) {
        removeAllScreens();
        if (list != null) {
            for (AdsBannerInfo bannerInfo : list) {
                ImageView item = new ImageView(this.mContext);
                item.setTag(bannerInfo.uri);
                item.setLayoutParams(new LayoutParams(DeviceConfig.sRecommendBannerWidth, DeviceConfig.sRecommendBannerHeight));
                item.setScaleType(ScaleType.FIT_XY);
                Bitmap image = Utilities.getBitmapFromUri(this.mContext, bannerInfo.iconUri);
                if (image != null) {
                    item.setImageBitmap(image);
                    addView(item);
                    item.setOnClickListener(this);
                }
            }
        }
    }

    public void onClick(View v) {
        if (v != null && (v.getTag() instanceof Uri)) {
            Uri uri = (Uri) v.getTag();
            Intent intent = new Intent();
            intent.setData(uri);
            intent.setPackage(Utilities.getMarketPackageName(this.mContext));
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            this.mContext.startActivity(intent);
        }
    }
}
