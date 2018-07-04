package com.miui.home.launcher.gadget;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.miui.home.R;
import com.miui.home.launcher.ScreenUtils;

public class GoogleSearch extends Gadget implements OnClickListener {
    private Context mContext;

    public GoogleSearch(Context context) {
        super(context);
        this.mContext = context;
        inflate(context, R.layout.gadget_google_search, this);
        findViewById(R.id.google).setOnClickListener(this);
        findViewById(R.id.voice).setOnClickListener(this);
    }

    public void onClick(View v) {
        ComponentName cn = new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity");
        if (ScreenUtils.isActivityExist(this.mContext, cn)) {
            Intent intent = new Intent();
            intent.addFlags(268435456);
            intent.setComponent(cn);
            ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            switch (v.getId()) {
                case R.id.google:
                    intent.setAction("android.search.action.GLOBAL_SEARCH");
                    intent.addFlags(67108864);
                    intent.addFlags(32768);
                    break;
                case R.id.voice:
                    intent.setAction("android.speech.action.WEB_SEARCH");
                    break;
                default:
                    intent = null;
                    break;
            }
            if (intent != null) {
                getContext().startActivity(intent, opts.toBundle());
            }
        }
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public void onAdded() {
    }

    public void onDeleted() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void updateConfig(Bundle config) {
    }

    public void onEditDisable() {
    }

    public void onEditNormal() {
    }
}
