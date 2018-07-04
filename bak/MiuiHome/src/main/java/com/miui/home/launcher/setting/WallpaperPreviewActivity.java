package com.miui.home.launcher.setting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimatedRotateDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.service.wallpaper.IWallpaperConnection.Stub;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.miui.home.R;
import com.miui.home.launcher.AnalyticalDataCollector;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.ProgressManager;
import com.miui.home.launcher.ProgressManager.ProgressProcessor;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.AutoFadeInImageView;
import com.miui.home.launcher.common.PermissionUtils;
import com.miui.home.launcher.common.ScalableImageView;
import com.miui.home.launcher.common.ScalableImageView.Callbacks;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.MamlTools;
import java.io.File;
import java.io.IOException;
import miui.app.Activity;
import miui.graphics.BitmapFactory;
import miui.widget.SlidingButton;

public class WallpaperPreviewActivity extends Activity implements OnClickListener, ProgressProcessor, Callbacks {
    private int mApplyBothItemIndex = -1;
    private int mApplyLockItemIndex = -1;
    private int mApplyWallpaperItemIndex = -1;
    private boolean mApplyWallpaperToLock = false;
    private View mBottomBg;
    private View mBottomOptions;
    private Button mBtnApply;
    private Runnable mConfirmPreviewChanged = new Runnable() {
        public void run() {
            WallpaperPreviewActivity.this.onWallpaperPreviewChanged(false);
        }
    };
    private final int[] mDefaultHomeScreenOffsets = new int[2];
    private DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == WallpaperPreviewActivity.this.mApplyLockItemIndex) {
                WallpaperPreviewActivity.this.applyWallpaper(true, false);
            } else if (which == WallpaperPreviewActivity.this.mApplyWallpaperItemIndex) {
                WallpaperPreviewActivity.this.applyWallpaper(false, true);
            } else if (which == WallpaperPreviewActivity.this.mApplyBothItemIndex) {
                WallpaperPreviewActivity.this.applyWallpaper(true, true);
            }
        }
    };
    private SlidingButton mEnableScrollButton;
    private boolean mEnableWallpaperScroll = false;
    private boolean mIsCheckingProgress = false;
    private boolean mIsHidingOptions = false;
    private boolean mIsLoading = false;
    private boolean mIsSettingLockScreen = false;
    private boolean mIsSettingWallpaper = false;
    private boolean mIsShiftingScrollMode = false;
    private Bitmap mLauncherPreview;
    private Intent mLiveWallpaperIntent;
    private View mLiveWallpaperSettingBtn;
    private ProgressBar mLoadingProgress;
    private Bitmap mLockScreenPreview;
    private boolean mLockScreenPreviewMode = true;
    private final int[] mLockScreenSize = new int[2];
    private View mMoreSettingsBtn;
    private AlertDialog mMoreSettingsDialog;
    private boolean mMoreSettingsShowing = false;
    private AutoFadeInImageView mPreviewTopLayer;
    private View mScrollType;
    private String mSettingFlag = "both_wallpaper";
    private boolean mShowOptions = true;
    private ValueAnimator mShowOptionsAnimator = new ValueAnimator();
    private boolean mStartedByHome = false;
    private View mTopOptions;
    private boolean mUsingDefaultLockScreen = true;
    private int mWallpaperColorMode = -1;
    private WallpaperConnection mWallpaperConnection;
    private ScalableImageView mWallpaperImageView;
    private String mWallpaperKey;
    private String mWallpaperPackageName;
    private String mWallpaperPath;
    private int mWallpaperProgressStatus = -5;
    private String mWallpaperServer;
    private String mWallpaperSettings;
    private final int[] mWallpaperSize = new int[2];
    private Bitmap mWallpaperThumbnail;
    private Uri mWallpaperUri;

    class WallpaperConnection extends Stub implements ServiceConnection {
        boolean mConnected;
        IWallpaperEngine mEngine;
        final Intent mIntent;
        IWallpaperService mService;

        WallpaperConnection(Intent intent) {
            this.mIntent = intent;
        }

        public boolean connect() {
            boolean z = true;
            synchronized (this) {
                if (WallpaperPreviewActivity.this.bindService(this.mIntent, this, 1)) {
                    this.mConnected = true;
                } else {
                    z = false;
                }
            }
            return z;
        }

        public void disconnect() {
            synchronized (this) {
                this.mConnected = false;
                if (this.mEngine != null) {
                    try {
                        this.mEngine.destroy();
                    } catch (RemoteException e) {
                    }
                    this.mEngine = null;
                }
                WallpaperPreviewActivity.this.unbindService(this);
                this.mService = null;
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (WallpaperPreviewActivity.this.mWallpaperConnection == this) {
                this.mService = IWallpaperService.Stub.asInterface(service);
                try {
                    View view = WallpaperPreviewActivity.this.mWallpaperImageView;
                    View root = view.getRootView();
                    PortableUtils.attachWallpaperService(this.mService, WallpaperPreviewActivity.this.mWallpaperConnection, view.getWindowToken(), 1004, true, root.getWidth(), root.getHeight());
                } catch (RemoteException e) {
                    Log.i("Launcher.WallpaperPreview", "Failed attaching wallpaper; clearing", e);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            this.mService = null;
            this.mEngine = null;
            if (WallpaperPreviewActivity.this.mWallpaperConnection == this) {
                Log.i("Launcher.WallpaperPreview", "Wallpaper service gone: " + name);
            }
        }

        public void attachEngine(IWallpaperEngine engine) {
            synchronized (this) {
                if (this.mConnected) {
                    this.mEngine = engine;
                    try {
                        engine.setVisibility(true);
                    } catch (RemoteException e) {
                    }
                } else {
                    try {
                        engine.destroy();
                    } catch (RemoteException e2) {
                    }
                }
            }
        }

        public ParcelFileDescriptor setWallpaper(String name) {
            return null;
        }

        public void engineShown(IWallpaperEngine engine) throws RemoteException {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || getIntent().getExtras() == null) {
            finishActivity(true);
            return;
        }
        PermissionUtils.requestAccessStoragePermissions(this);
        setContentView(R.layout.wallpaper_preview);
        Bundle bundle = getIntent().getExtras();
        if ("com.miui.home.set_wallpaper".equals(getIntent().getAction()) || bundle.getString("Wallpaper_uri") != null) {
            this.mWallpaperUri = "com.miui.home.set_wallpaper".equals(getIntent().getAction()) ? getIntent().getData() : Uri.parse(bundle.getString("Wallpaper_uri"));
            this.mWallpaperPath = bundle.getString("Wallpaper_path", null);
            this.mWallpaperKey = bundle.getString("android.intent.extra.update_progress_key", null);
            if (!TextUtils.isEmpty(this.mWallpaperKey)) {
                this.mWallpaperProgressStatus = bundle.getInt("android.intent.extra.update_progress_status", -5);
                this.mWallpaperServer = getCallingPackage();
            }
            if (!(WallpaperUtils.isUriFileExists(this.mWallpaperUri) && WallpaperUtils.isLauncherExist())) {
                Toast.makeText(this, !WallpaperUtils.isLauncherExist() ? R.string.launcher_not_exist : R.string.failed_to_load_image, 200).show();
                finishActivity(true);
                return;
            }
        } else if (bundle.get("android.live_wallpaper.intent") != null) {
            setTheme(R.style.WallpaperPreviewTheme.WithImmersionBg);
            this.mLiveWallpaperIntent = (Intent) bundle.get("android.live_wallpaper.intent");
            this.mWallpaperSettings = bundle.getString("android.live_wallpaper.settings");
            this.mWallpaperPackageName = bundle.getString("android.live_wallpaper.package");
            this.mWallpaperConnection = new WallpaperConnection(this.mLiveWallpaperIntent);
            if (this.mLiveWallpaperIntent.getComponent().getClassName().equals("com.miui.miwallpaper.MiWallpaper")) {
                Intent intent = new Intent("android.intent.action.UPDATE_PREVIEW_MIWALLPAPER");
                intent.putExtra("preview_miwallpaper_path", "data/system/theme/miwallpaper");
                intent.putExtra("preview_miwallpaper_side", true);
                sendStickyBroadcast(intent);
            }
        } else {
            finishActivity(true);
            return;
        }
        this.mSettingFlag = bundle.getString("wallpaper_setting_type", "both_wallpaper");
        if ("wallpaper".equals(this.mSettingFlag)) {
            this.mLockScreenPreviewMode = false;
        }
        this.mLauncherPreview = Utilities.createBitmapSafely(DeviceConfig.getDeviceWidth() / 1, DeviceConfig.getDeviceHeight() / 1, Config.ARGB_8888);
        initViews();
        if (!setUpWallpaperView()) {
            Toast.makeText(this, R.string.bad_wallpaper_source_prompt, 200).show();
            finishActivity(true);
        }
        if (this.mWallpaperUri == null || initDefaultSettings()) {
            showStatusBar(false);
            if (this.mLiveWallpaperIntent != null) {
                this.mWallpaperImageView.getRootView().setBackgroundColor(-16777216);
                this.mWallpaperImageView.postDelayed(new Runnable() {
                    public void run() {
                        WallpaperPreviewActivity.this.mWallpaperImageView.getRootView().setBackgroundColor(0);
                    }
                }, 400);
            }
            getWindow().setFormat(1);
            if ("com.miui.gallery".equals(getCallingPackage())) {
                AnalyticalDataCollector.setWallpaperEntryType(this, "Gallery");
            } else if ("com.android.thememanager".equals(getCallingPackage())) {
                AnalyticalDataCollector.setWallpaperEntryType(this, "ThemeManager");
            } else if ("com.miui.home".equals(getCallingPackage())) {
                this.mStartedByHome = true;
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mWallpaperProgressStatus != -5) {
            start();
            checkProgress(this);
        }
        if (this.mWallpaperConnection != null && this.mWallpaperConnection.mEngine != null) {
            try {
                this.mWallpaperConnection.mEngine.setVisibility(true);
            } catch (RemoteException e) {
            }
        } else if (this.mWallpaperUri != null && this.mWallpaperImageView.getImageBitmap() == null) {
            this.mWallpaperImageView.init(this, this.mWallpaperUri, this);
            if (!setWallpaperSizeAndOffsets(true)) {
                return;
            }
        }
        this.mLiveWallpaperSettingBtn.post(this.mConfirmPreviewChanged);
    }

    public void onPause() {
        super.onPause();
        stop();
        if (this.mWallpaperConnection != null && this.mWallpaperConnection.mEngine != null) {
            try {
                this.mWallpaperConnection.mEngine.setVisibility(false);
            } catch (RemoteException e) {
            }
        } else if (this.mWallpaperUri != null) {
            this.mWallpaperImageView.recycleBitmap();
        }
    }

    public void onDestroy() {
        cancelWallpaperPreview();
        ProgressManager.getManager(this).removeWallpaperProcessor(this);
        super.onDestroy();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mWallpaperImageView.post(new Runnable() {
            public void run() {
                if (WallpaperPreviewActivity.this.mWallpaperConnection != null && !WallpaperPreviewActivity.this.mWallpaperConnection.connect()) {
                    WallpaperPreviewActivity.this.mWallpaperConnection = null;
                }
            }
        });
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mWallpaperConnection != null) {
            this.mWallpaperConnection.disconnect();
        }
        this.mWallpaperConnection = null;
    }

    private void initViews() {
        this.mPreviewTopLayer = (AutoFadeInImageView) findViewById(R.id.preview_top);
        this.mLiveWallpaperSettingBtn = findViewById(R.id.live_wallpaper_settings);
        if (this.mLiveWallpaperSettingBtn != null) {
            this.mLiveWallpaperSettingBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    WallpaperPreviewActivity.this.showImmersionMenu(v, null);
                }
            });
        }
        if (this.mLiveWallpaperIntent == null || this.mWallpaperSettings == null) {
            this.mLiveWallpaperSettingBtn.setVisibility(4);
        } else {
            this.mLiveWallpaperSettingBtn.setVisibility(0);
        }
        this.mBottomBg = findViewById(R.id.bottom_bg);
        this.mMoreSettingsBtn = findViewById(R.id.more_settings_btn);
        this.mMoreSettingsBtn.setOnClickListener(this);
        this.mWallpaperImageView = (ScalableImageView) findViewById(R.id.wallpaper_view);
        this.mTopOptions = findViewById(R.id.top_options);
        this.mBottomOptions = findViewById(R.id.bottom_options);
        this.mEnableScrollButton = (SlidingButton) findViewById(R.id.scroll_button);
        this.mEnableScrollButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WallpaperPreviewActivity.this.shiftScrollType(isChecked);
            }
        });
        this.mScrollType = findViewById(R.id.wallpaper_scroll_type);
        this.mBtnApply = (Button) findViewById(R.id.btnApply);
        this.mBtnApply.setOnClickListener(this);
        if (this.mLiveWallpaperIntent != null || "lock_wallpaper".equals(this.mSettingFlag)) {
            if (this.mLiveWallpaperIntent != null) {
                this.mMoreSettingsBtn.setVisibility(4);
            }
            this.mScrollType.setVisibility(8);
            this.mMoreSettingsDialog = new Builder(this).setItems(new String[]{getResources().getString(R.string.apply_as_wallpaper), getResources().getString(R.string.apply_both)}, this.mDialogListener).create();
            this.mApplyWallpaperItemIndex = 0;
            this.mApplyBothItemIndex = 1;
        } else if ("wallpaper".equals(this.mSettingFlag)) {
            this.mMoreSettingsDialog = new Builder(this).setItems(new String[]{getResources().getString(R.string.apply_as_lock_screen), getResources().getString(R.string.apply_both)}, this.mDialogListener).create();
            this.mApplyLockItemIndex = 0;
            this.mApplyBothItemIndex = 1;
        } else {
            this.mMoreSettingsDialog = new Builder(this).setItems(new String[]{getResources().getString(R.string.apply_as_lock_screen), getResources().getString(R.string.apply_as_wallpaper), getResources().getString(R.string.apply_both)}, this.mDialogListener).create();
            this.mMoreSettingsBtn.setVisibility(4);
            this.mApplyLockItemIndex = 0;
            this.mApplyWallpaperItemIndex = 1;
            this.mApplyBothItemIndex = 2;
        }
        if (this.mLockScreenPreviewMode) {
            this.mBottomBg.setVisibility(4);
        }
        this.mMoreSettingsDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                WallpaperPreviewActivity.this.showMoreSettings(false);
            }
        });
        if (this.mWallpaperProgressStatus != -5) {
            ProgressManager.getManager(this).addWallpaperProcessor(this);
            this.mMoreSettingsBtn.setClickable(false);
            this.mMoreSettingsBtn.setAlpha(0.5f);
            this.mBtnApply.setText(R.string.download_action);
        }
        if (!(this.mWallpaperProgressStatus == -4 || this.mWallpaperProgressStatus == -5)) {
            showLoading(true, getResources().getText(R.string.loading_wallpaper));
        }
        this.mShowOptionsAnimator.setDuration((long) getResources().getInteger(17694721));
        this.mShowOptionsAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        this.mShowOptionsAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                WallpaperPreviewActivity.this.mTopOptions.setTranslationY(WallpaperPreviewActivity.this.mShowOptions ? (float) ((int) (((float) WallpaperPreviewActivity.this.mTopOptions.getHeight()) * (value - 1.0f))) : (float) ((int) (((float) WallpaperPreviewActivity.this.mTopOptions.getHeight()) * (-value))));
                WallpaperPreviewActivity.this.mBottomOptions.setTranslationY(WallpaperPreviewActivity.this.mShowOptions ? (float) ((int) (((float) WallpaperPreviewActivity.this.mBottomOptions.getHeight()) * (1.0f - value))) : (float) ((int) (((float) WallpaperPreviewActivity.this.mBottomOptions.getHeight()) * value)));
            }
        });
        this.mShowOptionsAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                WallpaperPreviewActivity.this.mIsHidingOptions = true;
            }

            public void onAnimationEnd(Animator animation) {
                WallpaperPreviewActivity.this.mIsHidingOptions = false;
            }
        });
    }

    private void showLoading(boolean show, CharSequence loadingMessage) {
        if (this.mIsLoading != show) {
            final View loading = findViewById(R.id.loading_view);
            this.mIsLoading = show;
            if (show) {
                loading.setEnabled(false);
                loading.setClickable(true);
                loading.setAlpha(1.0f);
                loading.setVisibility(0);
                this.mLoadingProgress = (ProgressBar) findViewById(R.id.progress);
                ((TextView) findViewById(R.id.message)).setText(loadingMessage);
                AnimatedRotateDrawable progress = (AnimatedRotateDrawable) this.mLoadingProgress.getIndeterminateDrawable();
                progress.setFramesCount(60);
                progress.setFramesDuration(20);
                return;
            }
            ValueAnimator fadeOut = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            fadeOut.setInterpolator(new LinearInterpolator());
            fadeOut.setDuration(300);
            fadeOut.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    loading.setAlpha(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            fadeOut.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    loading.setVisibility(4);
                }
            });
            fadeOut.start();
        }
    }

    private boolean initDefaultSettings() {
        if (new File("/data/system/theme/lockscreen").exists()) {
            this.mLockScreenPreview = null;
        } else {
            this.mLockScreenPreview = MamlTools.snapshootLockscreen(this, WallpaperUtils.getCurrentWallpaperColorMode());
        }
        this.mUsingDefaultLockScreen = MamlTools.usingDefaultLockScreen();
        this.mLockScreenSize[0] = DeviceConfig.getDeviceWidth();
        int[] iArr = this.mWallpaperSize;
        int[] iArr2 = this.mLockScreenSize;
        int deviceHeight = DeviceConfig.getDeviceHeight();
        iArr2[1] = deviceHeight;
        iArr[1] = deviceHeight;
        if ("wallpaper".equals(this.mSettingFlag) || ("both_wallpaper".equals(this.mSettingFlag) && !this.mLockScreenPreviewMode)) {
            this.mScrollType.setVisibility(0);
        } else {
            this.mScrollType.setVisibility(4);
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.immersion, menu);
        MenuItem itemConfigure = menu.findItem(R.id.wallpaper_configure);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wallpaper_configure:
                configureLiveWallpaper();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureLiveWallpaper() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this.mWallpaperPackageName, this.mWallpaperSettings));
        intent.putExtra("android.service.wallpaper.PREVIEW_MODE", true);
        startActivity(intent);
    }

    private void changePreviewTopLayerColor(int wallpaperColorMode) {
        WallpaperUtils.correctHomeScreenPreview(wallpaperColorMode, this.mLauncherPreview);
        if (this.mUsingDefaultLockScreen) {
            this.mLockScreenPreview = MamlTools.snapshootLockscreen(this, wallpaperColorMode);
        }
    }

    private void showStatusBar(boolean show) {
        Window launcherWindow = getWindow();
        LayoutParams attrs = launcherWindow.getAttributes();
        attrs.flags = show ? attrs.flags & -1025 : attrs.flags | 1024;
        launcherWindow.setAttributes(attrs);
    }

    private boolean setUpWallpaperView() {
        if (this.mLiveWallpaperIntent != null) {
            this.mScrollType.setVisibility(4);
        } else if (this.mWallpaperImageView == null) {
            return false;
        }
        if (this.mWallpaperUri != null) {
            getWindow().clearFlags(1048576);
        }
        this.mWallpaperImageView.setOnClickListener(this);
        return true;
    }

    public void handleProgressUpdate(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.WALLPAPER_PROGRESS_UPDATE") && !TextUtils.isEmpty(this.mWallpaperKey)) {
            if (this.mWallpaperKey.equals(intent.getStringExtra("android.intent.extra.update_progress_key"))) {
                int status = intent.getIntExtra("android.intent.extra.update_progress_status", -100);
                Uri wallpaperUri = null;
                try {
                    wallpaperUri = Uri.parse(intent.getStringExtra("Wallpaper_uri"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateProgressStatus(status, wallpaperUri);
            }
        }
    }

    public void start() {
        this.mIsCheckingProgress = true;
    }

    public void stop() {
        this.mIsCheckingProgress = false;
    }

    public void clear() {
    }

    public void checkProgress(Context context) {
        if (ProgressManager.isServerEnableShareProgressStatus(this, this.mWallpaperServer)) {
            updateProgressStatus(ProgressManager.queryProgressSharedValue(this.mWallpaperServer, this.mWallpaperKey), this.mWallpaperUri);
        } else {
            ProgressManager.getManager(this).queryProgressByBroadcast((Context) this, this.mWallpaperServer, this.mWallpaperKey);
        }
    }

    public boolean isStop() {
        if (ProgressManager.isServerEnableShareProgressStatus(this, this.mWallpaperServer) && this.mIsCheckingProgress) {
            return false;
        }
        return true;
    }

    private void updateProgressStatus(int status, Uri wallpaperUri) {
        boolean cancelLoading;
        boolean z = false;
        if (status == -100) {
            Toast.makeText(this, getResources().getText(R.string.bad_wallpaper_source_prompt), 200);
            finishActivity(true);
        } else if (status == -5) {
            ProgressManager.getManager(this).removeWallpaperProcessor(this);
            this.mWallpaperKey = null;
            onDownloadStatusChanged(true);
        }
        if (status == -4 || status == -5) {
            cancelLoading = true;
        } else {
            cancelLoading = false;
        }
        if (!cancelLoading) {
            z = true;
        }
        showLoading(z, getResources().getText(R.string.loading_wallpaper));
        if (!(wallpaperUri == null || wallpaperUri.equals(this.mWallpaperUri))) {
            this.mWallpaperUri = wallpaperUri;
            if (!WallpaperUtils.isUriFileExists(this.mWallpaperUri) || !WallpaperUtils.isLauncherExist()) {
                Toast.makeText(this, !WallpaperUtils.isLauncherExist() ? R.string.launcher_not_exist : R.string.bad_wallpaper_source_prompt, 200).show();
                finishActivity(true);
            } else if (status == -4 || status == -5) {
                this.mWallpaperImageView.init(this, this.mWallpaperUri, this);
                setUpWallpaperView();
                setWallpaperSizeAndOffsets(true);
            }
        }
        this.mWallpaperProgressStatus = status;
    }

    private void onDownloadStatusChanged(boolean downloadFinished) {
        boolean z;
        float f = 1.0f;
        boolean z2 = true;
        this.mBtnApply.setAlpha(downloadFinished ? 1.0f : 0.5f);
        Button button = this.mBtnApply;
        if (downloadFinished) {
            z = true;
        } else {
            z = false;
        }
        button.setClickable(z);
        this.mBtnApply.setText(downloadFinished ? R.string.apply_action : R.string.status_downloading);
        View view = this.mMoreSettingsBtn;
        if (!downloadFinished) {
            f = 0.5f;
        }
        view.setAlpha(f);
        View view2 = this.mMoreSettingsBtn;
        if (downloadFinished) {
            z = true;
        } else {
            z = false;
        }
        view2.setClickable(z);
        SlidingButton slidingButton = this.mEnableScrollButton;
        if (!downloadFinished) {
            z2 = false;
        }
        slidingButton.setClickable(z2);
    }

    public void onClick(View v) {
        if (!this.mIsShiftingScrollMode && !this.mIsSettingLockScreen && !this.mIsSettingWallpaper) {
            switch (v.getId()) {
                case R.id.wallpaper_view:
                    if ("both_wallpaper".equals(this.mSettingFlag)) {
                        shiftPreviewMode();
                        return;
                    } else if (this.mMoreSettingsShowing) {
                        showMoreSettings(false);
                        return;
                    } else {
                        return;
                    }
                case R.id.btnApply:
                    if (this.mWallpaperProgressStatus != -5) {
                        requestDownloadWallpaper();
                        onDownloadStatusChanged(false);
                        return;
                    } else if (this.mLiveWallpaperIntent != null || "both_wallpaper".equals(this.mSettingFlag)) {
                        showMoreSettings(true);
                        return;
                    } else if ("lock_wallpaper".equals(this.mSettingFlag)) {
                        applyWallpaper(true, false);
                        return;
                    } else if ("wallpaper".equals(this.mSettingFlag)) {
                        applyWallpaper(false, true);
                        return;
                    } else {
                        return;
                    }
                case R.id.more_settings_btn:
                    showMoreSettings(true);
                    return;
                default:
                    return;
            }
        }
    }

    private void showOptions(boolean show) {
        if (this.mShowOptions != show) {
            this.mShowOptions = show;
            this.mShowOptionsAnimator.cancel();
            this.mShowOptionsAnimator.start();
        }
    }

    private void cancelWallpaperPreview() {
        if (!TextUtils.isEmpty(this.mWallpaperKey)) {
            Intent intent = new Intent("com.miui.home.action.WALLPAPER_PREVIEW_CANCELED");
            intent.setPackage(this.mWallpaperServer);
            intent.putExtra("android.intent.extra.update_progress_key", this.mWallpaperKey);
            sendBroadcast(intent);
        }
    }

    private void requestDownloadWallpaper() {
        if (!TextUtils.isEmpty(this.mWallpaperKey)) {
            Intent intent = new Intent("com.miui.home.action.WALLPAPER_REQUEST_DOWNLOAD");
            intent.setPackage(this.mWallpaperServer);
            intent.putExtra("android.intent.extra.update_progress_key", this.mWallpaperKey);
            sendBroadcast(intent);
        }
    }

    public void onImageMatrixChanged() {
        showOptions(false);
    }

    public void onImageMatrixConfirm() {
        showOptions(true);
        this.mPreviewTopLayer.removeCallbacks(this.mConfirmPreviewChanged);
        this.mPreviewTopLayer.post(this.mConfirmPreviewChanged);
    }

    public void onWallpaperPreviewChanged(boolean previewModeChanged) {
        int wallpaperColorMode = this.mWallpaperColorMode;
        if (this.mWallpaperUri == null) {
            wallpaperColorMode = 0;
        } else if (!previewModeChanged) {
            Bitmap wallpaperBitmap = this.mWallpaperImageView.getImageBitmap();
            Matrix m = new Matrix(this.mWallpaperImageView.getImageMatrix());
            if (this.mWallpaperThumbnail == null) {
                this.mWallpaperThumbnail = Bitmap.createBitmap(this.mWallpaperSize[0] / 5, this.mWallpaperSize[1] / 5, Config.ARGB_8888);
            }
            m.postTranslate((float) this.mDefaultHomeScreenOffsets[0], 0.0f);
            m.postScale(0.2f, 0.2f);
            WallpaperUtils.cropBitmap(this.mWallpaperThumbnail, wallpaperBitmap, m);
            if (this.mWallpaperThumbnail != null) {
                wallpaperColorMode = BitmapFactory.getBitmapColorMode(this.mWallpaperThumbnail, 1);
            } else {
                wallpaperColorMode = 0;
            }
        }
        if (wallpaperColorMode != this.mWallpaperColorMode) {
            changePreviewTopLayerColor(wallpaperColorMode);
        }
        if (previewModeChanged || wallpaperColorMode != this.mWallpaperColorMode) {
            setPreviewTopLayer(previewModeChanged);
        }
        if (this.mWallpaperColorMode != wallpaperColorMode) {
            this.mWallpaperColorMode = wallpaperColorMode;
        }
    }

    private void setPreviewTopLayer(boolean withAnim) {
        Drawable top;
        if (this.mLockScreenPreviewMode) {
            top = new BitmapDrawable(getResources(), this.mLockScreenPreview);
            top.setBounds(0, 0, DeviceConfig.getDeviceWidth(), DeviceConfig.getDeviceHeight());
        } else {
            top = new BitmapDrawable(getResources(), this.mLauncherPreview);
            top.setBounds(0, 0, DeviceConfig.getDeviceWidth(), DeviceConfig.getDeviceHeight());
        }
        this.mPreviewTopLayer.changeDrawable(top, withAnim);
    }

    private void shiftScrollType(boolean enable) {
        if (this.mEnableWallpaperScroll != enable) {
            this.mEnableWallpaperScroll = enable;
            setWallpaperSizeAndOffsets(false);
        }
    }

    private boolean setWallpaperSizeAndOffsets(boolean firstTime) {
        if (this.mWallpaperUri == null) {
            return true;
        }
        if (this.mEnableWallpaperScroll) {
            this.mWallpaperSize[0] = DeviceConfig.getDeviceWidth() * 2;
            WallpaperUtils.getDefaultScreenWallpaperOffset(this.mDefaultHomeScreenOffsets, DeviceConfig.getDeviceWidth() * 2, DeviceConfig.getDeviceWidth());
        } else {
            this.mWallpaperSize[0] = DeviceConfig.getDeviceWidth();
            int[] iArr = this.mDefaultHomeScreenOffsets;
            this.mDefaultHomeScreenOffsets[1] = 0;
            iArr[0] = 0;
        }
        this.mWallpaperImageView.setOffsets(this.mDefaultHomeScreenOffsets[0], this.mDefaultHomeScreenOffsets[1]);
        boolean success = this.mWallpaperImageView.setMinLayoutSize(this.mWallpaperSize[0], this.mWallpaperSize[1], firstTime);
        if (success) {
            return success;
        }
        Toast.makeText(this, R.string.image_too_small_message, 200).show();
        finishActivity(true);
        return success;
    }

    private boolean shiftPreviewMode() {
        if (this.mPreviewTopLayer.inFadeAnim()) {
            return false;
        }
        this.mLockScreenPreviewMode = !this.mLockScreenPreviewMode;
        onWallpaperPreviewChanged(true);
        this.mWallpaperImageView.invalidate();
        this.mBottomBg.setVisibility(0);
        if (this.mLockScreenPreviewMode) {
            if (this.mLiveWallpaperIntent == null) {
                this.mScrollType.animate().alpha(0.0f).setDuration(200).start();
            }
            this.mBottomBg.animate().alpha(0.0f).setDuration(200).start();
        } else {
            if (this.mLiveWallpaperIntent == null) {
                this.mScrollType.setVisibility(0);
                this.mScrollType.animate().alpha(1.0f).setDuration(200).start();
            }
            this.mBottomBg.animate().alpha(1.0f).setDuration(200).start();
        }
        return true;
    }

    private void notifySettingFail(String mssage) {
        Intent wallpaperPreview = new Intent();
        wallpaperPreview.setClassName(this, "com.miui.home.launcher.setting.WallpaperPreviewActivity");
        Bundle bundle = new Bundle();
        if (this.mWallpaperUri != null) {
            bundle.putString("Wallpaper_uri", this.mWallpaperUri.toSafeString());
        } else if (this.mLiveWallpaperIntent != null) {
            bundle.putParcelable("android.live_wallpaper.intent", this.mLiveWallpaperIntent);
            bundle.putString("android.live_wallpaper.settings", this.mWallpaperSettings);
            bundle.putString("android.live_wallpaper.package", this.mWallpaperPackageName);
        } else {
            return;
        }
        wallpaperPreview.putExtras(bundle);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.icon_launcher).setContentTitle(mssage).setContentIntent(PendingIntent.getActivity(this, 0, wallpaperPreview, 1207959552)).setDeleteIntent(null).setAutoCancel(true);
        ((NotificationManager) getSystemService("notification")).notify(344865, builder.build());
    }

    private void applyWallpaper(boolean applyLock, boolean applyWallpaper) {
        if (this.mLiveWallpaperIntent != null) {
            if (applyWallpaper) {
                if (!WallpaperUtils.setLiveWallpaper(this.mLiveWallpaperIntent.getComponent(), this.mWallpaperImageView.getWindowToken())) {
                    notifySettingFail(getResources().getString(R.string.wallpaper_failed_message));
                }
                if (this.mStartedByHome) {
                    AnalyticalDataCollector.trackWallpaperChanged("home");
                }
            }
            this.mApplyWallpaperToLock = applyLock;
            finishActivity(false);
            return;
        }
        Bitmap wallpaperBitmap = this.mWallpaperImageView.getImageBitmap();
        if (wallpaperBitmap == null) {
            Toast.makeText(this, R.string.lock_screen_failed_message, 200).show();
            return;
        }
        showLoading(true, getResources().getString(R.string.is_setting_wallpaper_message));
        Uri wallpaperSrc = null;
        if (this.mWallpaperPath != null) {
            File wallpaper = new File(this.mWallpaperPath);
            if (wallpaper.exists()) {
                wallpaperSrc = Uri.fromFile(wallpaper);
            }
        }
        final String wallpaperPath = wallpaperSrc == null ? this.mWallpaperUri.toString() : wallpaperSrc.toString();
        final Bitmap wallpaperPreview = Bitmap.createBitmap(wallpaperBitmap);
        if (applyWallpaper) {
            new AsyncTask<Void, Void, Boolean>() {
                protected Boolean doInBackground(Void... params) {
                    WallpaperPreviewActivity.this.mIsSettingWallpaper = true;
                    Matrix m = new Matrix(WallpaperPreviewActivity.this.mWallpaperImageView.getImageMatrix());
                    m.postTranslate((float) WallpaperPreviewActivity.this.mDefaultHomeScreenOffsets[0], 0.0f);
                    return Boolean.valueOf(WallpaperUtils.setWallpaper(WallpaperPreviewActivity.this.getWallpaper(m, WallpaperPreviewActivity.this.mWallpaperSize[0], WallpaperPreviewActivity.this.mWallpaperSize[1], wallpaperPreview), wallpaperPath));
                }

                protected void onPostExecute(Boolean success) {
                    WallpaperPreviewActivity.this.mIsSettingWallpaper = false;
                    if (!success.booleanValue()) {
                        WallpaperPreviewActivity.this.notifySettingFail(WallpaperPreviewActivity.this.getResources().getString(R.string.wallpaper_failed_message));
                    }
                    if (!WallpaperPreviewActivity.this.mIsSettingLockScreen) {
                        WallpaperPreviewActivity.this.finishActivity(false);
                    }
                }
            }.execute(new Void[0]);
            if (this.mStartedByHome) {
                AnalyticalDataCollector.trackWallpaperChanged("home");
            }
        }
        if (applyLock) {
            new AsyncTask<Void, Void, Boolean>() {
                protected Boolean doInBackground(Void... params) {
                    WallpaperPreviewActivity.this.mIsSettingLockScreen = true;
                    return Boolean.valueOf(WallpaperUtils.setLockWallpaper(WallpaperPreviewActivity.this, WallpaperPreviewActivity.this.getWallpaper(new Matrix(WallpaperPreviewActivity.this.mWallpaperImageView.getImageMatrix()), WallpaperPreviewActivity.this.mLockScreenSize[0], WallpaperPreviewActivity.this.mLockScreenSize[1], wallpaperPreview), false, wallpaperPath));
                }

                protected void onPostExecute(Boolean success) {
                    WallpaperPreviewActivity.this.mIsSettingLockScreen = false;
                    if (!success.booleanValue()) {
                        WallpaperPreviewActivity.this.notifySettingFail(WallpaperPreviewActivity.this.getResources().getString(R.string.lock_screen_failed_message));
                    }
                    if (!WallpaperPreviewActivity.this.mIsSettingWallpaper) {
                        WallpaperPreviewActivity.this.finishActivity(false);
                    }
                }
            }.execute(new Void[0]);
            if (this.mStartedByHome) {
                AnalyticalDataCollector.trackLockWallpaperChanged("home");
            }
        }
    }

    private void correctCropRectWithRotation(int rotation, Rect cropRect, Rect srcImage) {
        Rect tmp = new Rect(cropRect);
        if (rotation == 90) {
            cropRect.left = tmp.top;
            cropRect.right = cropRect.left + tmp.height();
            cropRect.top = srcImage.width() - tmp.right;
            cropRect.bottom = cropRect.top + tmp.width();
        } else if (rotation == 180) {
            cropRect.left = srcImage.width() - tmp.right;
            cropRect.right = cropRect.left + tmp.width();
            cropRect.top = srcImage.height() - srcImage.bottom;
            cropRect.bottom = cropRect.top + tmp.height();
        } else if (rotation == 270) {
            cropRect.left = srcImage.height() - tmp.bottom;
            cropRect.right = cropRect.left + tmp.height();
            cropRect.top = tmp.left;
            cropRect.bottom = tmp.left + tmp.width();
        }
    }

    private float[] calcStartPos(Canvas canvas, int rotation, Rect dstRect, Rect cropRect, Bitmap src) {
        float scaleX;
        float scaleY;
        canvas.rotate((float) rotation, (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
        float[] start = new float[2];
        if (rotation == 90 || rotation == 270) {
            scaleX = ((float) dstRect.width()) / ((float) cropRect.height());
            scaleY = ((float) dstRect.height()) / ((float) cropRect.width());
            canvas.scale(scaleX, scaleY, (float) (canvas.getHeight() / 2), (float) (canvas.getWidth() / 2));
        } else {
            scaleX = ((float) dstRect.width()) / ((float) cropRect.width());
            scaleY = ((float) dstRect.height()) / ((float) cropRect.height());
            canvas.scale(scaleX, scaleY, (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));
        }
        if (rotation == 90) {
            start[0] = ((float) cropRect.bottom) * scaleX;
            start[1] = ((float) (-cropRect.left)) * scaleY;
        } else if (rotation == 180) {
            start[0] = ((float) cropRect.right) * scaleX;
            start[1] = ((float) cropRect.bottom) * scaleY;
        } else if (rotation == 270) {
            start[0] = ((float) (-cropRect.top)) * scaleX;
            start[1] = ((float) cropRect.right) * scaleY;
        } else {
            start[0] = ((float) (-cropRect.left)) * scaleX;
            start[1] = ((float) (-cropRect.top)) * scaleY;
        }
        Matrix inverse = new Matrix();
        canvas.getMatrix().invert(inverse);
        inverse.mapPoints(start);
        return start;
    }

    private Bitmap getWallpaper(Matrix imageMatrix, int width, int height, Bitmap wallpaperPreview) {
        try {
            Options op = BitmapFactory.getBitmapSize(this, this.mWallpaperUri);
            float scaleX = ((float) op.outWidth) / ((float) wallpaperPreview.getWidth());
            float scaleY = ((float) op.outHeight) / ((float) wallpaperPreview.getHeight());
            RectF rectF = new RectF(0.0f, 0.0f, (float) wallpaperPreview.getWidth(), (float) wallpaperPreview.getHeight());
            imageMatrix.mapRect(rectF);
            Rect cropRect = new Rect();
            float widthRatio = ((float) this.mWallpaperImageView.getImageWidth()) / rectF.width();
            float heightRatio = ((float) this.mWallpaperImageView.getImageHeight()) / rectF.height();
            cropRect.left = (int) (((-rectF.left) * widthRatio) * scaleX);
            cropRect.top = (int) (((-rectF.top) * heightRatio) * scaleY);
            cropRect.right = cropRect.left + ((int) ((((float) width) * widthRatio) * scaleX));
            cropRect.bottom = cropRect.top + ((int) ((((float) height) * heightRatio) * scaleY));
            int rotation = this.mWallpaperImageView.getImageRotation();
            int height2 = (rotation == 90 || rotation == 270) ? wallpaperPreview.getHeight() : wallpaperPreview.getWidth();
            float srcWidth = ((float) height2) * scaleX;
            height2 = (rotation == 90 || rotation == 270) ? wallpaperPreview.getWidth() : wallpaperPreview.getHeight();
            correctCropRectWithRotation(rotation, cropRect, new Rect(0, 0, (int) srcWidth, (int) (((float) height2) * scaleY)));
            if (scaleX != 1.0f || scaleY != 1.0f) {
                return WallpaperUtils.decodeRegion(this, this.mWallpaperUri, cropRect, width, height, rotation);
            }
            height2 = (rotation == 90 || rotation == 270) ? cropRect.height() : cropRect.width();
            int destWidth = Math.min(width, height2);
            height2 = (rotation == 90 || rotation == 270) ? cropRect.width() : cropRect.height();
            int destHeight = Math.min(height, height2);
            Bitmap result = Utilities.createBitmapSafely(destWidth, destHeight, Config.ARGB_8888);
            if (result == null) {
                return result;
            }
            Canvas canvas = new Canvas(result);
            float[] start = calcStartPos(canvas, rotation, new Rect(0, 0, destWidth, destHeight), cropRect, wallpaperPreview);
            canvas.drawBitmap(wallpaperPreview, start[0], start[1], new Paint(2));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showMoreSettings(boolean show) {
        if (this.mMoreSettingsShowing != show) {
            this.mMoreSettingsShowing = show;
            if (show) {
                this.mMoreSettingsDialog.show();
                this.mBottomOptions.animate().translationY((float) this.mBottomOptions.getHeight()).start();
                return;
            }
            if (this.mMoreSettingsDialog != null) {
                this.mMoreSettingsDialog.dismiss();
            }
            this.mBottomOptions.animate().translationY(0.0f).start();
        }
    }

    public void onBackPressed() {
        if (this.mMoreSettingsShowing) {
            showMoreSettings(false);
        } else if (!this.mIsSettingLockScreen && !this.mIsSettingWallpaper) {
            super.onBackPressed();
        }
    }

    public void finishActivity(boolean isCanceled) {
        if (this.mPreviewTopLayer == null) {
            super.finish();
            return;
        }
        this.mPreviewTopLayer.removeCallbacks(this.mConfirmPreviewChanged);
        if (!isCanceled) {
            WallpaperUtils.setLockScreenShowLiveWallpaper(this.mApplyWallpaperToLock);
            if (this.mStartedByHome && this.mApplyWallpaperToLock) {
                AnalyticalDataCollector.trackLockWallpaperChanged("home");
            }
            WallpaperUtils.setEnableWallpaperScroll(this.mEnableWallpaperScroll);
            Intent data = new Intent();
            data.putExtra("android.live_wallpaper.intent", this.mLiveWallpaperIntent);
            data.putExtra("Wallpaper_uri", this.mWallpaperUri);
            setResult(-1, data);
        }
        finish();
    }
}
