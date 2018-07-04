package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.IPackageInstallObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedRotateDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.MiuiSettings.System;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MiuiWindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.market.sdk.MarketManager;
import com.miui.home.R;
import com.miui.home.launcher.ItemIcon.OnSlideVerticallyListener;
import com.miui.home.launcher.LauncherModel.Callbacks;
import com.miui.home.launcher.LauncherModel.ComponentAndUser;
import com.miui.home.launcher.LauncherSettings.Favorites;
import com.miui.home.launcher.LauncherSettings.Screens;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.Workspace.CellInfo;
import com.miui.home.launcher.common.AsyncTaskExecutorHelper;
import com.miui.home.launcher.common.CubicEaseInOutInterpolater;
import com.miui.home.launcher.common.ForegroundTaskQueue;
import com.miui.home.launcher.common.PermissionUtils;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.Gadget;
import com.miui.home.launcher.gadget.GadgetAutoChangeService;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.miui.home.launcher.lockwallpaper.mode.RequestInfo;
import com.miui.home.launcher.lockwallpaper.mode.ResultInfo;
import com.miui.home.launcher.lockwallpaper.mode.WallpaperInfo;
import com.miui.home.launcher.setting.PortableUtils;
import com.miui.home.launcher.upsidescene.SceneScreen;
import com.miui.home.launcher.upsidescene.data.FreeStyle;
import com.miui.home.launcher.upsidescene.data.FreeStyleSerializer;
import com.miui.systemAdSolution.miuiHome.DownloadNotificationChecker;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import miui.app.ToggleManager;
import miui.app.backup.BackupManager;
import miui.graphics.BitmapFactory;
import miui.maml.RenderThread;
import miui.os.Build;
import miui.os.Environment;
import miui.os.FileUtils;
import miui.security.SecurityManager;
import miui.util.FileAccessable.Factory;
import miui.util.MiuiFeatureUtils;
import miui.util.ScreenshotUtils;
import miui.widget.ArrowPopupWindow;

public final class Launcher extends Activity implements OnClickListener, OnLongClickListener, OnSlideVerticallyListener, Callbacks, WallpaperColorChangedListener {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final String MIUI_HOME_SPLASH_PATH = (FileUtils.normalizeDirectoryName(Environment.getExternalStorageDirectory().getAbsolutePath()) + "Download/mihome_splash.jpg");
    private static HashMap<Long, FolderInfo> mFolders = new HashMap();
    private static Bitmap sBlurBitmap = null;
    private static boolean sChildrenModeEnabled = false;
    private static boolean sConfigurationChanged = false;
    private static boolean sEditingModeExiting = false;
    private static boolean sEnteredSceneScreen = false;
    private static final boolean sIsClipTransitionDevice = Build.DEVICE.startsWith("mione");
    private static boolean sIsDefaultThemeApplied = false;
    private static boolean sPrefShowIconShadow = false;
    private static boolean sResumeWithUninstalling = false;
    private HashMap<Intent, ShortcutInfo> mAllLoadedApps = new HashMap();
    private int mAppLocateFolderScrollOffset;
    private LauncherAppWidgetHost mAppWidgetHost;
    private AppWidgetManager mAppWidgetManager;
    private ApplicationsMessage mApplicationsMessage;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mChangeByCloud;
    private long mChildModeEnterTime = -1;
    private FolderInfo mChildrenFolderInfo;
    private ContentObserver mChildrenModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean childrenModeEnabled = Utilities.isChildrenModeEnabled(Launcher.this);
            Launcher.this.mOpenChildMode = childrenModeEnabled;
            Launcher.this.showChildrenMode(childrenModeEnabled);
            if (!childrenModeEnabled && Launcher.this.mChildModeEnterTime > 0) {
                AnalyticalDataCollector.trackChildRemainTime(System.currentTimeMillis() - Launcher.this.mChildModeEnterTime);
                Launcher.this.mChildModeEnterTime = -1;
            }
        }
    };
    private View mCurrentThumbnailView = null;
    private SpannableStringBuilder mDefaultKeySsb = null;
    private DeleteZone mDeleteZone;
    private ArrayList<ItemInfo> mDesktopItems = new ArrayList();
    String mDialogComponent;
    private DisplayListener mDisplayListener = new DisplayListener() {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (!DeviceConfig.checkIfIsOrientationChanged(Launcher.this.getApplicationContext())) {
                DeviceConfig.loadScreenSize(Launcher.this.getApplicationContext(), Launcher.this.getResources());
                if (DeviceConfig.isScreenSizeChanged()) {
                    Launcher.this.onScreenSizeChanged();
                }
            }
        }
    };
    private DragController mDragController;
    private DragLayer mDragLayer;
    private Background mDragLayerBackground;
    private EasterEggs mEasterEggs;
    private EditingEntryThumbnailView mEditingEntryView;
    private int mEditingState = 7;
    private boolean mEnableDemoMode = false;
    private ErrorBar mErrorBar;
    private ValueAnimator mFolderAnim;
    private FolderCling mFolderCling;
    private boolean mFolderClosingInNormalEdit;
    private ForceTouchLayer mForceTouchLayer;
    ForegroundTaskQueue mForegroundTaskQueue;
    private boolean mFreeStyleExists;
    private Runnable mFreeStyleExitTimer = new Runnable() {
        public void run() {
            AnalyticalDataCollector.exitFreeStyle(Launcher.this.getApplicationContext());
        }
    };
    public ArrayList<Gadget> mGadgets = new ArrayList();
    private ContentObserver mGlobalSearchSwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            DeviceConfig.setAllowedSlidingUpToStartGolbalSearch(String.valueOf(0).equals(uri.getFragment()));
        }
    };
    private Gson mGson = new Gson();
    private long mHomeDataCreateTime = -1;
    private HotSeats mHotSeats;
    private Animation mHotseatEditingEnter;
    private Animation mHotseatEditingExit;
    private IconLoader mIconLoader;
    private boolean mInAutoFilling = false;
    private ProgressDialog mInstallPresetAppDialog = null;
    private ArrayList<ShortcutInfo> mInstalledAppToDelayNotificatoinList = new ArrayList();
    private boolean mIsAppLocating = false;
    private boolean mIsChangingLockWallpaper = false;
    private boolean mIsFolderAnimating = false;
    private boolean mIsMinusScreenShowing = false;
    private boolean mIsNewIntentNow = false;
    private boolean mIsPause = false;
    private boolean mIsPreviewShowing = false;
    private boolean mIsStartingLockWallpaperPreviewActivity = false;
    private ItemInfo mLastAddInfo = null;
    private View mLastHideThumbnailView = null;
    private long mLastPausedTime = -1;
    private long mLastStopTime = -1;
    private boolean mLaunchAppFromFolder;
    private Dialog mLoadingDialog = null;
    private ContentObserver mLockWallpaperObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            PreferenceManager.getDefaultSharedPreferences(Launcher.this).edit().remove("currentWallpaperInfo").commit();
            if (WallpaperUtils.getLockWallpaperProvider(Launcher.this) == null) {
                Launcher.this.changeToDefaultLockWallpaper();
            } else {
                Launcher.this.autoChangeLockWallpaper(false);
            }
            if (Launcher.this.mChangeByCloud) {
                Launcher.this.mChangeByCloud = false;
            } else {
                WallpaperUtils.setProviderSetter(Launcher.this, "other");
            }
        }
    };
    private MinusOneScreenView mMinusOneScreenView;
    private LauncherModel mModel;
    private MultiSelectContainerView mMultiSelectContainer;
    private boolean mNeedLast = false;
    private ArrayList<ShortcutInfo> mNewInstalledApps = new ArrayList();
    private boolean mOnResumeExpectedForActivityResult = false;
    private boolean mOpenChildMode = false;
    private PerformLaunchAction mPerformLaunchAction = new PerformLaunchAction();
    private ArrayList<ItemInfo> mPosInvalidItems = new ArrayList();
    private View mPositionSnap = null;
    private int mPredictedEditingState = this.mEditingState;
    String mPreviewComponent;
    private Runnable mReloadWidgetsRunnable = new Runnable() {
        public void run() {
            Launcher.this.mWidgetThumbnailViewAdapter.reloadWidgets(Launcher.this.mCurrentThumbnailView == Launcher.this.mWidgetThumbnailView);
        }
    };
    private Bundle mSavedInstanceState;
    private Bundle mSavedState;
    private boolean mSceneAnimating = false;
    private ViewGroup mSceneScreenLoading;
    private CustomableReference<SceneScreen> mSceneScreenRef;
    private FrameLayout mScreen;
    private ContentObserver mScreenCellsSizeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            String screenCells = System.getString(Launcher.this.getContentResolver(), "miui_home_screen_cells_size", null);
            if (!TextUtils.isEmpty(screenCells)) {
                int[] cells = new int[2];
                ScreenUtils.parseCellsSize(screenCells, cells);
                if (!(cells[0] == DeviceConfig.getCellCountX() && cells[1] == DeviceConfig.getCellCountY()) && DeviceConfig.setScreenCells(Launcher.this, cells[0], cells[1])) {
                    AnalyticalDataCollector.trackScreenCellsSizeChanged(screenCells);
                    Process.killProcess(Process.myPid());
                }
            }
        }
    };
    private ScreenCellsThumbnailView mScreenCellsThumbnailView;
    private final ContentObserver mScreenChangeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.d("Launcher", "onContentChange");
            Launcher.this.mWorkspace.loadScreens(false, true);
            if (Launcher.this.mLastAddInfo instanceof LauncherAppWidgetProviderInfo) {
                Launcher.this.addAppWidget((LauncherAppWidgetProviderInfo) Launcher.this.mLastAddInfo);
            }
        }
    };
    private ScreenContent mScreenContent;
    int mScreenDiagonalDistance;
    private ShortcutWidgetLoader mShortcutWidgetLoader = new ShortcutWidgetLoader();
    private boolean mShowingChildrenTips;
    private Animation mThumbnailViewEditingEnter;
    private Animation mThumbnailViewEditingExit;
    private File mTmpFile;
    int[] mTmpLocation = new int[2];
    private int[] mTmpPos = new int[2];
    private RandomAccessFile mTmpRAFile;
    private ToggleManager mToggleManager;
    private TogglesSelectView mTogglesSelectView;
    private TransitionEffectThumbnailView mTransEffectThumbnailView;
    UsageStatsChecker mUsageStatsChecker;
    private long mUserPersentAnimationPrepairedId = -1;
    private boolean mWaitingForMarketDetail;
    private boolean mWaitingForResult;
    private WallpaperThumbnailView mWallpaperThumbnailView;
    private WallpaperThumbnailViewAdapter mWallpaperThumbnailViewAdapter;
    private WallpaperUtils mWallpaperUtils = new WallpaperUtils();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();
    private WidgetThumbnailView mWidgetThumbnailView;
    private WidgetThumbnailViewAdapter mWidgetThumbnailViewAdapter;
    private Workspace mWorkspace;
    private boolean mWorkspaceLoading = true;
    private WorkspaceThumbnailView mWorkspacePreview;

    public interface IconContainer {
        int removeShortcutIcon(ShortcutIcon shortcutIcon);
    }

    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            Launcher.this.onAppWidgetReset();
        }
    }

    static class BackEaseOutInterpolater implements Interpolator {
        static final BackEaseOutInterpolater sInstance = new BackEaseOutInterpolater();
        private float mOvershot = 1.70158f;

        public float getInterpolation(float t) {
            float s = this.mOvershot;
            t -= 1.0f;
            return ((t * t) * (((s + 1.0f) * t) + s)) + 1.0f;
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc;
        public int mnc;

        private LocaleConfiguration() {
            this.mcc = -1;
            this.mnc = -1;
        }
    }

    private class PerformLaunchAction implements Runnable {
        Intent mIntent;
        Object mTag;
        View mView;

        public PerformLaunchAction() {
            reset();
        }

        public void launch(Intent intent, Object tag, View v, Handler handler) {
            set(intent, tag, v);
            run();
        }

        public void set(Intent intent, Object tag, View v) {
            this.mIntent = intent;
            this.mTag = tag;
            this.mView = v;
        }

        public void reset() {
            this.mIntent = null;
            this.mTag = null;
            this.mView = null;
        }

        public void run() {
            if (this.mIntent == null) {
                reset();
            } else if (Launcher.this.checkIntentPermissions(this.mIntent)) {
                Launcher.this.startActivity(this.mIntent, this.mTag, this.mView);
                reset();
            }
        }
    }

    private class ShortcutWidgetLoader extends BroadcastReceiver {
        private IntentFilter mCommandAction;
        private View mLastShortcutWidgetView;
        private String mTargetPackage;

        private ShortcutWidgetLoader() {
            this.mCommandAction = new IntentFilter("miui.intent.action.ICON_PANEL_COMMAND");
        }

        public void startShortcutWidget(Context context, View v, String packageName) {
            if (this.mLastShortcutWidgetView == null) {
                this.mLastShortcutWidgetView = v;
                this.mTargetPackage = packageName;
                context.registerReceiver(this, this.mCommandAction);
                Intent intent = new Intent("miui.intent.action.ICON_PANEL");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setPackage(packageName);
                Bitmap snapshot = DragController.createViewBitmap(v, 1.0f);
                if (snapshot != null) {
                    intent.putExtra("miui.intent.extra.ICON", snapshot);
                }
                Launcher.this.startActivity(intent, v.getTag(), v);
                AnalyticalDataCollector.launcherShortcutWidget(Launcher.this, intent);
            }
        }

        private void stopShortcutWidget(Context context) {
            if (this.mLastShortcutWidgetView != null) {
                context.unregisterReceiver(this);
                this.mLastShortcutWidgetView.setVisibility(0);
                this.mLastShortcutWidgetView = null;
            }
        }

        public void onActivityResume(Context context) {
            stopShortcutWidget(context);
        }

        public void onActivityStop(Context context) {
            stopShortcutWidget(context);
        }

        public void onReceive(Context context, Intent intent) {
            if (this.mLastShortcutWidgetView != null) {
                String command = intent.getStringExtra("miui.intent.extra.ICON_PANEL_COMMAND");
                if ("hide".equals(command)) {
                    this.mLastShortcutWidgetView.setVisibility(4);
                    intent.setAction("miui.intent.action.ICON_PANEL_COMMAND");
                    intent.setPackage(this.mTargetPackage);
                    intent.putExtra("miui.intent.extra.ICON_PANEL_COMMAND", "ok");
                    context.sendBroadcast(intent);
                } else if ("ok".equals(command)) {
                    stopShortcutWidget(context);
                }
            }
        }
    }

    static {
        boolean z;
        if (Launcher.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public void onScreenCellsChanged() {
        closeFolder();
        showLoadingDialog();
        reloadDeviceConfig(Application.getLauncherApplication(this), true);
        this.mModel.forceReload(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1792);
        showLoadingDialog();
        WallpaperUtils.resetLockWallpaperProviderIfNeeded(this);
        Settings.System.putString(getContentResolver(), getPackageName() + ".enable_share_progress_status", String.valueOf(true));
        Log.d("Launcher", "onCreate");
        WallpaperUtils.suggestWallpaperDimension(this, true);
        Window win = getWindow();
        win.addFlags(256);
        if (VERSION.SDK_INT > 16) {
            win.addFlags(67108864);
        }
        win.setFlags(-65537, 65536);
        win.setExtraFlags(1, 1);
        win.setFormat(1);
        LauncherApplication app = Application.getLauncherApplication(this);
        AsyncTaskExecutorHelper.initDefaultExecutor();
        if (DeviceConfig.isRotatable()) {
            setRequestedOrientation(2);
        }
        sPrefShowIconShadow = getResources().getBoolean(R.bool.config_enable_icon_shadow);
        GadgetFactory.updateGadgetBackup(this);
        this.mModel = app.setLauncher(this);
        this.mIconLoader = app.getIconLoader();
        reloadDeviceConfig(app, false);
        if (checkForLocaleChange() || sConfigurationChanged) {
            this.mIconLoader.updateDefaultIcon();
            Factory.clearCache();
        }
        this.mDragController = new DragController(this);
        registerContentObservers();
        registerBroadcastReceivers();
        this.mAppWidgetManager = AppWidgetManager.getInstance(this);
        this.mAppWidgetHost = new LauncherAppWidgetHost(getApplicationContext(), this, 1024);
        this.mApplicationsMessage = new ApplicationsMessage(this);
        this.mWallpaperUtils.setLauncher(this);
        registerWallpaperChangedReceiver();
        if (DeviceConfig.needHideMinusScreen()) {
            setContentView(R.layout.launcher_without_minus_screen);
        } else {
            setContentView(R.layout.launcher);
        }
        setupViews();
        sChildrenModeEnabled = Utilities.isChildrenModeEnabled(this);
        this.mWallpaperUtils.onWallpaperChanged();
        showChildrenMode(sChildrenModeEnabled);
        MarketManager.getManager(getApplicationContext()).updateApplicationEnableState();
        this.mModel.startLoader(getApplicationContext(), true);
        this.mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(this.mDefaultKeySsb, 0);
        this.mAppWidgetHost.startListening();
        this.mToggleManager = ToggleManager.createInstance(getApplicationContext());
        if (Settings.System.getInt(getContentResolver(), "keep_launcher_in_memory", 1) != 0) {
            startService(new Intent(this, ForegroundPlaceholderService.class));
        }
        try {
            openTmpFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.mEasterEggs = EasterEggs.init(this, getDragLayer());
        sIsDefaultThemeApplied = applyingDefaultTheme();
        this.mForegroundTaskQueue = new ForegroundTaskQueue();
        if (this.mMinusOneScreenView != null) {
            this.mMinusOneScreenView.setCurrentScreen(1);
            this.mMinusOneScreenView.setScreenTransitionType(1);
        }
        DownloadNotificationChecker.checkShowNotificationFlag(getApplicationContext());
        Utilities.queryIfAllowToStartGlobalSearch(this);
    }

    public static final boolean isEnableIconShadow() {
        return sPrefShowIconShadow;
    }

    public static final boolean isDefaultThemeApplied() {
        return sIsDefaultThemeApplied;
    }

    public final boolean isFolderIdValid(long folderId) {
        return mFolders.containsKey(Long.valueOf(folderId));
    }

    public static final boolean isClipTransitionDevice() {
        return sIsClipTransitionDevice;
    }

    public static final boolean isSupportCompleteAnimation() {
        return MiuiFeatureUtils.isSystemFeatureSupported("feature_complete_animation", true);
    }

    public final boolean isErrorBarShowing() {
        return this.mErrorBar.isShowing();
    }

    private void setErrorBarBackground() {
        Drawable errorBg = Utilities.loadThemeCompatibleDrawable(getApplicationContext(), R.drawable.error_background);
        if (errorBg != null) {
            this.mErrorBar.setBackground(errorBg);
        }
    }

    void reloadDeviceConfig(LauncherApplication app, boolean screenCellsChanged) {
        if (DeviceConfig.Init(this, screenCellsChanged)) {
            app.getModel().stopLoader();
            if (app.getLauncherProvider() != null) {
                app.getLauncherProvider().onCreate();
            }
        }
    }

    private boolean checkForLocaleChange() {
        boolean localeChanged;
        LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        readConfiguration(this, localeConfiguration);
        Configuration configuration = getResources().getConfiguration();
        String previousLocale = localeConfiguration.locale;
        String locale = configuration.locale.toString();
        int previousMcc = localeConfiguration.mcc;
        int mcc = configuration.mcc;
        int previousMnc = localeConfiguration.mnc;
        int mnc = configuration.mnc;
        if (locale.equals(previousLocale) && mcc == previousMcc && mnc == previousMnc) {
            localeChanged = false;
        } else {
            localeChanged = true;
        }
        if (!localeChanged) {
            return false;
        }
        localeConfiguration.locale = locale;
        localeConfiguration.mcc = mcc;
        localeConfiguration.mnc = mnc;
        writeConfiguration(this, localeConfiguration);
        return true;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        Throwable th;
        DataInputStream in = null;
        try {
            DataInputStream in2 = new DataInputStream(context.openFileInput("launcher.preferences"));
            try {
                configuration.locale = in2.readUTF();
                configuration.mcc = in2.readInt();
                configuration.mnc = in2.readInt();
                if (in2 != null) {
                    try {
                        in2.close();
                        in = in2;
                        return;
                    } catch (IOException e) {
                        in = in2;
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            if (in != null) {
                in.close();
            }
        } catch (IOException e8) {
            if (in != null) {
                in.close();
            }
        } catch (Throwable th3) {
            th = th3;
            if (in != null) {
                in.close();
            }
            throw th;
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        Throwable th;
        DataOutputStream out = null;
        try {
            DataOutputStream out2 = new DataOutputStream(context.openFileOutput("launcher.preferences", 0));
            try {
                out2.writeUTF(configuration.locale);
                out2.writeInt(configuration.mcc);
                out2.writeInt(configuration.mnc);
                out2.flush();
                if (out2 != null) {
                    try {
                        out2.close();
                        out = out2;
                        return;
                    } catch (IOException e) {
                        out = out2;
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                out = out2;
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                out = out2;
                try {
                    context.getFileStreamPath("launcher.preferences").delete();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            } catch (NullPointerException e7) {
                out = out2;
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e8) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                    out.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            if (out != null) {
                out.close();
            }
        } catch (IOException e10) {
            context.getFileStreamPath("launcher.preferences").delete();
            if (out != null) {
                out.close();
            }
        } catch (NullPointerException e11) {
            if (out != null) {
                out.close();
            }
        }
    }

    public DragController getDragController() {
        return this.mDragController;
    }

    public DragLayer getDragLayer() {
        return this.mDragLayer;
    }

    public FrameLayout getScreenContent() {
        return this.mScreenContent;
    }

    public FrameLayout getScreen() {
        return this.mScreen;
    }

    public FolderCling getFolderCling() {
        return this.mFolderCling;
    }

    public IconLoader getIconLoader() {
        return this.mIconLoader;
    }

    public SharedPreferences getWorldReadableSharedPreference() {
        return getSharedPreferences(getPackageName() + "_world_readable_preferences", DeviceConfig.TEMP_SHARE_MODE_FOR_WORLD_READABLE);
    }

    public void blurBehindWithAnim(boolean show) {
        this.mDragLayerBackground.showUninstallBgColor(show);
    }

    public Drawable getBlurScreenShot(boolean scaleScreen) {
        int layer;
        if (scaleScreen) {
            layer = getDragLayer().getWallpaperLayer();
        } else {
            layer = MiuiWindowManager.getLayer(this, 2000);
        }
        Bitmap background = ScreenshotUtils.getScreenshot(this, 0.2f, 0, layer, false);
        if (background != null) {
            Canvas canvas = new Canvas();
            if (scaleScreen) {
                if (background.isMutable()) {
                    canvas.setBitmap(background);
                } else {
                    Bitmap tmp = Utilities.createBitmapSafely(background.getWidth(), background.getHeight(), background.getConfig());
                    if (tmp != null) {
                        canvas.setBitmap(tmp);
                        canvas.drawBitmap(background, 0.0f, 0.0f, null);
                    }
                    background.recycle();
                    background = tmp;
                }
                if (background == null) {
                    return new ColorDrawable(getResources().getColor(R.color.folder_background_mask));
                }
                View screen = getScreen();
                float scale = ((float) background.getWidth()) / ((float) screen.getMeasuredWidth());
                canvas.save();
                canvas.scale(scale, scale);
                canvas.scale(0.85f, 0.85f, ((float) screen.getMeasuredWidth()) / 2.0f, ((float) screen.getMeasuredHeight()) / 2.0f);
                screen.draw(canvas);
                canvas.restore();
            }
            sBlurBitmap = BitmapFactory.fastBlur(background, sBlurBitmap, ScreenshotUtils.DEFAULT_SCREEN_BLUR_RADIUS);
            background.recycle();
            if (sBlurBitmap != null) {
                canvas.setBitmap(sBlurBitmap);
                canvas.drawColor(getResources().getColor(R.color.folder_background_mask));
                return new BitmapDrawable(getResources(), sBlurBitmap);
            }
        }
        return new ColorDrawable(getResources().getColor(R.color.folder_background_mask));
    }

    public void onWallpaperColorChanged() {
        Intent intent = new Intent("miui.intent.action.MINUS_SCREEN_WALLPAPER");
        intent.putExtra("WALLPAPER_COLOR_MODE", WallpaperUtils.getCurrentWallpaperColorMode());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    public void onWallpaperChanged() {
        if (this.mCurrentThumbnailView == this.mWallpaperThumbnailView) {
            this.mWallpaperThumbnailView.onWallpaperChanged();
        }
    }

    public void onScreenDeleted(long screenId) {
        this.mMultiSelectContainer.onScreenDeleted(screenId);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mWaitingForResult = false;
        if (resultCode == -1) {
            switch (requestCode) {
                case 1:
                    completeAddShortcut(data);
                    break;
                case 5:
                    completeAddAppWidget(data);
                    break;
                case 7:
                    if (!getResources().getText(R.string.toggle_title).equals(data.getStringExtra("android.intent.extra.shortcut.NAME"))) {
                        onDropShortcut(null, data);
                        break;
                    } else {
                        showTogglesSelectView();
                        break;
                    }
                case 11:
                    getSceneScreen().completeAddAppWidget(data);
                    break;
                case 101:
                    if (!isSceneShowing()) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            long itemId = GadgetFactory.getGadgetItemId(extras);
                            if (itemId != -1) {
                                Gadget gadget = findGadget(itemId);
                                if (gadget != null) {
                                    gadget.updateConfig(extras);
                                    break;
                                }
                            }
                        }
                    }
                    getSceneScreen().completeGadgetConfig(data);
                    break;
                    break;
                case 1002:
                    if (this.mFolderCling.isOpened()) {
                        this.mFolderCling.getRecommendScreen().snapToAppView(data.getStringExtra("appId"));
                        break;
                    }
                    break;
            }
        } else if (requestCode == 5 && resultCode == 0 && data != null) {
            int appWidgetId = data.getIntExtra("appWidgetId", -1);
            if (appWidgetId != -1) {
                this.mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
        if (requestCode == 10 && resultCode == -1) {
            WallpaperUtils.setWallpaperFromCustom(this, data);
        }
        if (requestCode == 1001) {
            if (!this.mFolderCling.isOpened()) {
                return;
            }
            FolderInfo folderInfo;
            if (resultCode == -1) {
                this.mFolderCling.setMarketAllowConnectToNetwork(true);
                for (FolderInfo folderInfo2 : mFolders.values()) {
                    if (folderInfo2.isRecommendAppsViewEnable(this)) {
                        if (this.mFolderCling.getFolderId() == folderInfo2.id) {
                            folderInfo2.getRecommendInfo(this).clearContents(true);
                            folderInfo2.getRecommendInfo(this).initRecommendViewAndRequest();
                        } else {
                            folderInfo2.getRecommendInfo(this).clearContents(false);
                        }
                    }
                }
                this.mFolderCling.showRecommendApps(true, true, 0);
            } else {
                this.mFolderCling.setMarketAllowConnectToNetwork(false);
                folderInfo2 = this.mFolderCling.getFolder().getInfo();
                this.mFolderCling.setRecommendButtonChecked(false);
                this.mFolderCling.getFolder().getInfo().setRecommendAppsViewEnable(false);
                this.mFolderCling.getFolder().getInfo().recordRecommendAppsSwitchState(getApplicationContext(), false);
                this.mFolderCling.showRecommendApps(false, true, 0);
            }
        }
        if (requestCode == 5 || requestCode == 7 || requestCode == 1 || requestCode == 101 || ((requestCode == 10 && resultCode == 0) || requestCode == 12)) {
            this.mOnResumeExpectedForActivityResult = true;
        }
    }

    private void startChildrenModeSettings() {
        sendBroadcast(new Intent("miui.intent.action.CHILDREN_MODE_CHANGED"));
        Intent filterIntent = new Intent("android.intent.action.CHILDREN_CONTROL_SETTING");
        filterIntent.addCategory("android.intent.category.DEFAULT");
        startActivity(filterIntent);
    }

    protected void onResume() {
        super.onResume();
        this.mIsPause = false;
        this.mWaitingForMarketDetail = false;
        this.mWorkspace.onResume();
        this.mDragLayer.clearAllResizeFrames();
        this.mOnResumeExpectedForActivityResult = false;
        notifyGadgetStateChanged(4);
        RenderThread.globalThread().setPaused(false);
        scrollToDefault();
        this.mShortcutWidgetLoader.onActivityResume(this);
        sResumeWithUninstalling = false;
        this.mIsNewIntentNow = false;
        this.mLaunchAppFromFolder = false;
        this.mForegroundTaskQueue.handleRemainingTasksOnResume(this, this.mWorkspace.getHandler());
        updateStatusBarClock();
        if (this.mMinusOneScreenView != null && this.mMinusOneScreenView.getCurrentScreenIndex() == 0) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("miui.intent.action.MINUS_SCREEN_ONRESUME"));
        }
        showInstalledAppToDelayNotification();
    }

    private void showChildrenMode(boolean show) {
        if (show) {
            if (this.mChildrenFolderInfo == null) {
                this.mChildrenFolderInfo = new FolderInfo();
                FolderIcon folderIcon = FolderIcon.fromXml(R.layout.folder_icon, this, null, this.mChildrenFolderInfo);
                folderIcon.setTag(this.mChildrenFolderInfo);
                this.mChildrenFolderInfo.setBuddyIconView(folderIcon);
            }
            loadChildrenAccessableApps();
            closeFolder(false);
            setEditingState(7);
            openFolder(this.mChildrenFolderInfo, this.mChildrenFolderInfo.getBuddyIconView());
            this.mScreenContent.setVisibility(4);
            this.mIsMinusScreenShowing = false;
            if (this.mMinusOneScreenView != null && this.mMinusOneScreenView.getCurrentScreenIndex() == 0) {
                this.mMinusOneScreenView.setCurrentScreen(1);
                this.mIsMinusScreenShowing = true;
            }
            sChildrenModeEnabled = show;
            return;
        }
        sChildrenModeEnabled = show;
        closeFolder(false);
        this.mScreenContent.setVisibility(0);
        if (this.mIsMinusScreenShowing && this.mMinusOneScreenView != null) {
            this.mMinusOneScreenView.setCurrentScreen(0);
        }
        if (this.mChildrenFolderInfo != null) {
            this.mChildrenFolderInfo.clear();
            this.mChildrenFolderInfo = null;
        }
    }

    private void showChildrenModeTips() {
        if (this.mChildrenFolderInfo.count() > 0) {
            this.mShowingChildrenTips = false;
        } else if (!this.mShowingChildrenTips) {
            this.mShowingChildrenTips = true;
            new Builder(this).setCancelable(false).setIconAttribute(16843605).setTitle(getResources().getString(R.string.children_mode_tips_title)).setMessage(getResources().getString(R.string.children_mode_tips_message)).setPositiveButton(R.string.confirm_btn_label, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Launcher.this.mShowingChildrenTips = false;
                    Launcher.this.startChildrenModeSettings();
                }
            }).create().show();
        }
    }

    private void loadChildrenAccessableApps() {
        this.mChildrenFolderInfo.clear();
        List<ComponentName> list = new ArrayList();
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        SecurityManager manager = (SecurityManager) getSystemService("security");
        for (ResolveInfo resolveInfo : ActivityThread.currentApplication().getPackageManager().queryIntentActivities(mainIntent, 0)) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (manager.getApplicationChildrenControlEnabled(packageName)) {
                new Intent().setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                this.mChildrenFolderInfo.add(new ShortcutInfo(this, resolveInfo, Process.myUserHandle()));
            }
        }
    }

    public static boolean isChildrenModeEnabled() {
        return sChildrenModeEnabled;
    }

    protected void onPause() {
        super.onPause();
        this.mIsPause = true;
        this.mDragController.cancelDrag();
        notifyGadgetStateChanged(3);
        RenderThread.globalThread().setPaused(true);
        this.mLastPausedTime = SystemClock.uptimeMillis();
        exitTogglesSelectView(true);
        this.mWorkspace.post(new Runnable() {
            public void run() {
                Launcher.this.updateStatusBarClock();
            }
        });
    }

    public void scrollToDefault() {
        this.mPositionSnap.setFocusableInTouchMode(true);
        this.mPositionSnap.requestFocus();
        this.mPositionSnap.setFocusableInTouchMode(false);
    }

    private void notifyGadgetStateChanged(int state) {
        for (int i = this.mGadgets.size() - 1; i >= 0; i--) {
            Gadget gadget = (Gadget) this.mGadgets.get(i);
            GadgetInfo info = (GadgetInfo) gadget.getTag();
            switch (state) {
                case 1:
                    gadget.onStart();
                    break;
                case 2:
                    gadget.onStop();
                    break;
                case 3:
                    gadget.onPause();
                    break;
                case 4:
                    if (info.screenId != this.mWorkspace.getCurrentScreenId()) {
                        break;
                    }
                    gadget.onResume();
                    break;
                case 5:
                    gadget.onCreate();
                    break;
                case 6:
                    gadget.onDestroy();
                    break;
                case 7:
                    gadget.onEditDisable();
                    break;
                case 8:
                    gadget.onEditNormal();
                    break;
                default:
                    break;
            }
        }
        SceneScreen sceneScreen = getSceneScreen();
        if (sceneScreen == null) {
            return;
        }
        if ((sceneScreen.isShowing() || state == 6) && state != 8 && state != 7) {
            sceneScreen.notifyGadgets(state);
        }
    }

    protected void onStop() {
        super.onStop();
        ProgressManager.getManager(this).onLauncherPaused();
        this.mWorkspace.onStop();
        this.mLastStopTime = System.currentTimeMillis();
        if (isSceneShowing()) {
            getSceneScreen().onStop();
        }
        if (this.mWorkspacePreview.isShowing()) {
            showPreview(false, false);
        }
        this.mDragLayer.updateWallpaperOffset();
        notifyGadgetStateChanged(2);
        LauncherModel.flashDelayedUpdateItemFlags(this);
        this.mShortcutWidgetLoader.onActivityStop(this);
        if (this.mEasterEggs != null) {
            this.mEasterEggs.onStop();
        }
        unRegisterDisplayListener();
    }

    protected void onStart() {
        super.onStart();
        this.mWorkspace.onStart();
        ProgressManager.getManager(this).onLauncherResume();
        if (isSceneShowing()) {
            getSceneScreen().onStart();
        }
        if (sChildrenModeEnabled) {
            refreshChildrenFolder();
            if (this.mOpenChildMode) {
                AnalyticalDataCollector.trackChildSettingEnter();
                this.mOpenChildMode = false;
                this.mChildModeEnterTime = System.currentTimeMillis();
                AnalyticalDataCollector.trackChildAppNum(this.mChildrenFolderInfo.count());
            }
        }
        if (this.mCurrentThumbnailView == this.mWallpaperThumbnailView) {
            this.mWallpaperThumbnailView.refreshWallpaperThumbnail();
        }
        if (((WallpaperManager) getSystemService("wallpaper")).getWallpaperInfo() != null) {
            this.mWallpaperUtils.onWallpaperChanged();
        }
        this.mDragLayer.updateWallpaper();
        this.mApplicationsMessage.requestUpdateMessages(false);
        notifyGadgetStateChanged(1);
        checkNewInstalledAppsBeStarted();
        if (this.mEasterEggs != null) {
            this.mEasterEggs.onStart();
        }
        this.mEnableDemoMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_enable_demo_mode", false);
        registerDisplayListener();
    }

    private void registerDisplayListener() {
        ((DisplayManager) getApplicationContext().getSystemService("display")).registerDisplayListener(this.mDisplayListener, null);
    }

    private void unRegisterDisplayListener() {
        ((DisplayManager) getApplicationContext().getSystemService("display")).unregisterDisplayListener(this.mDisplayListener);
    }

    private void checkNewInstalledAppsBeStarted() {
        if (this.mUsageStatsChecker == null) {
            this.mUsageStatsChecker = new UsageStatsChecker();
        }
        if (this.mLastStopTime == -1) {
            Calendar cal = Calendar.getInstance();
            cal.add(6, -1);
            this.mLastStopTime = cal.getTimeInMillis();
        }
        this.mUsageStatsChecker.updateNewInstalledApps(this, this.mAllLoadedApps, this.mNewInstalledApps, this.mLastStopTime);
    }

    private boolean acceptFilter() {
        return !((InputMethodManager) getSystemService("input_method")).isFullscreenMode();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (handled || !acceptFilter() || keyCode == 66 || !TextKeyListener.getInstance().onKeyDown(this.mWorkspace, this.mDefaultKeySsb, keyCode, event) || this.mDefaultKeySsb == null || this.mDefaultKeySsb.length() <= 0) {
            return handled;
        }
        return onSearchRequested();
    }

    private String getTypedText() {
        return this.mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        this.mDefaultKeySsb.clear();
        this.mDefaultKeySsb.clearSpans();
        Selection.setSelection(this.mDefaultKeySsb, 0);
    }

    private void setupViews() {
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this);
        DragController dragController = this.mDragController;
        this.mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        this.mDragLayerBackground = (Background) findViewById(R.id.drag_layer_background);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mDragLayerBackground);
        this.mDragLayer.setDragController(dragController);
        this.mDragLayer.setLauncher(this);
        this.mScreen = (FrameLayout) findViewById(R.id.screen);
        this.mScreenContent = (ScreenContent) findViewById(R.id.screen_content);
        this.mScreenContent.setLauncher(this);
        this.mWidgetThumbnailView = (WidgetThumbnailView) findViewById(R.id.widget_thumbnail_view);
        this.mWidgetThumbnailView.setLauncher(this);
        this.mWidgetThumbnailViewAdapter = new WidgetThumbnailViewAdapter(this);
        this.mWidgetThumbnailViewAdapter.setLauncher(this);
        this.mWidgetThumbnailView.setDragController(dragController);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mWidgetThumbnailView);
        this.mWallpaperThumbnailView = (WallpaperThumbnailView) findViewById(R.id.wallpaper_thumbnail_view);
        this.mWallpaperThumbnailView.setLauncher(this);
        this.mWallpaperThumbnailView.setDragController(dragController);
        this.mWallpaperThumbnailViewAdapter = new WallpaperThumbnailViewAdapter(this);
        this.mWallpaperThumbnailViewAdapter.setLauncher(this);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mWallpaperThumbnailView);
        this.mTransEffectThumbnailView = (TransitionEffectThumbnailView) findViewById(R.id.transition_thumbnail_view);
        this.mTransEffectThumbnailView.setLauncher(this);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mTransEffectThumbnailView);
        this.mScreenCellsThumbnailView = (ScreenCellsThumbnailView) findViewById(R.id.screen_cells_thumbnail_view);
        this.mScreenCellsThumbnailView.setLauncher(this);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mScreenCellsThumbnailView);
        this.mEditingEntryView = (EditingEntryThumbnailView) findViewById(R.id.editing_entry_view);
        this.mEditingEntryView.setLauncher(this);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mEditingEntryView);
        this.mErrorBar = (ErrorBar) findViewById(R.id.error);
        this.mErrorBar.setLauncher(this);
        setErrorBarBackground();
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mErrorBar);
        this.mWorkspace = (Workspace) this.mDragLayer.findViewById(R.id.workspace);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mWorkspace);
        Workspace workspace = this.mWorkspace;
        workspace.setHapticFeedbackEnabled(false);
        this.mWorkspacePreview = (WorkspaceThumbnailView) this.mDragLayer.findViewById(R.id.workspace_preview);
        this.mWorkspacePreview.setDragController(dragController);
        workspace.setOnLongClickListener(this);
        workspace.setDragController(dragController);
        workspace.setLauncher(this);
        workspace.setThumbnailView(this.mWorkspacePreview);
        this.mWorkspacePreview.setResource(this.mWorkspace);
        this.mDeleteZone = (DeleteZone) this.mDragLayer.findViewById(R.id.delete_zone);
        this.mDeleteZone.setLauncher(this);
        this.mDeleteZone.setDragController(dragController);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mDeleteZone);
        this.mFolderCling = (FolderCling) findViewById(R.id.folder_cling);
        this.mFolderCling.setLauncher(this);
        this.mFolderCling.setDragController(dragController);
        this.mFolderCling.checkMarketAllowConnectToNetwork();
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mFolderCling);
        this.mMultiSelectContainer = (MultiSelectContainerView) findViewById(R.id.multi_select_container);
        this.mMultiSelectContainer.setLauncher(this);
        this.mMultiSelectContainer.setDragController(dragController);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mMultiSelectContainer);
        this.mHotSeats = (HotSeats) this.mDragLayer.findViewById(R.id.hot_seats);
        this.mHotSeats.setLaucher(this);
        this.mHotSeats.setDragController(dragController);
        this.mWallpaperUtils.setOnWallpaperColorChangedListener(this.mHotSeats);
        dragController.setDragScoller(workspace);
        dragController.addDragListener(this.mDeleteZone);
        dragController.setScrollView(this.mDragLayer);
        dragController.setMoveTarget(workspace);
        dragController.addDropTarget(this.mWorkspacePreview);
        dragController.addDropTarget(workspace);
        dragController.addDropTarget(this.mHotSeats);
        setupAnimations();
        this.mPositionSnap = this.mDragLayer.findViewById(R.id.default_position);
        this.mForceTouchLayer = (ForceTouchLayer) findViewById(R.id.force_touch_layer);
        this.mMinusOneScreenView = (MinusOneScreenView) findViewById(R.id.minus_one_layer);
    }

    private void setupAnimations() {
        this.mHotseatEditingEnter = AnimationUtils.loadAnimation(this, R.anim.hotseat_editing_enter);
        this.mHotseatEditingExit = AnimationUtils.loadAnimation(this, R.anim.hotseat_editing_exit);
        this.mThumbnailViewEditingEnter = AnimationUtils.loadAnimation(this, R.anim.thumbnail_editing_enter);
        this.mThumbnailViewEditingEnter.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (Launcher.this.mCurrentThumbnailView == Launcher.this.mWallpaperThumbnailView) {
                    Launcher.this.mWallpaperThumbnailViewAdapter.startLoading();
                } else if (Launcher.this.mCurrentThumbnailView == Launcher.this.mWidgetThumbnailView) {
                    Launcher.this.mWidgetThumbnailViewAdapter.startLoading();
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.mThumbnailViewEditingExit = AnimationUtils.loadAnimation(this, R.anim.thumbnail_editing_exit);
        this.mThumbnailViewEditingExit.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (Launcher.this.mLastHideThumbnailView == Launcher.this.mWidgetThumbnailView) {
                    Launcher.this.mWidgetThumbnailView.setAdapter(null);
                } else if (Launcher.this.mLastHideThumbnailView == Launcher.this.mWallpaperThumbnailView) {
                    Launcher.this.mWallpaperThumbnailView.setAdapter(null);
                } else if (Launcher.this.mLastHideThumbnailView instanceof ThumbnailView) {
                    ((ThumbnailView) Launcher.this.mLastHideThumbnailView).removeAllScreens();
                } else if (Launcher.this.mLastHideThumbnailView instanceof ViewGroup) {
                    ((ViewGroup) Launcher.this.mLastHideThumbnailView).removeAllViews();
                }
                Launcher.this.mLastHideThumbnailView = null;
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.mFolderAnim = new ValueAnimator();
        this.mFolderAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                Launcher.this.mIsFolderAnimating = true;
            }

            public void onAnimationEnd(Animator animation) {
                Launcher.this.mIsFolderAnimating = false;
                FolderInfo folderInfo = Launcher.this.mFolderCling.getFolder().getInfo();
                if (!folderInfo.isRecommendAppsViewEnable(Launcher.this) && !Launcher.this.isInEditing() && folderInfo.launchCount == 1 && Launcher.this.mFolderCling.isOpened() && !Utilities.isChildrenModeEnabled(Launcher.this) && !Launcher.this.isSceneShowing()) {
                    Launcher.this.mFolderCling.getFolder().showEditPanel(true, true);
                }
            }
        });
        this.mFolderAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                Drawable folderBg = Launcher.this.mFolderCling.getBackground();
                int folderBgAlpha = (int) (255.0f * value);
                Launcher.this.setScreenContentAlpha(1.0f - value);
                if (folderBg != null) {
                    folderBg.setAlpha(folderBgAlpha);
                } else {
                    Launcher.this.mFolderCling.setBackgroundColor((int) (((float) Launcher.this.getResources().getColor(R.color.folder_cling_bg)) * value));
                }
            }
        });
    }

    private void setScreenContentAlpha(float alpha) {
        this.mHotSeats.setAlpha(alpha);
        this.mWorkspace.setAlpha(alpha);
    }

    public ItemIcon createItemIcon(ViewGroup parent, ItemInfo info) {
        ItemIcon ii = null;
        if (info instanceof ShortcutInfo) {
            ii = createShortcutIcon(parent, (ShortcutInfo) info);
            if (this.mModel.hasShortcutWidgetActivity(((ShortcutInfo) info).getPackageName())) {
                ii.setOnSlideVerticallyListener(this);
            }
        } else if (info instanceof FolderInfo) {
            ii = createFolderIcon(parent, (FolderInfo) info);
        }
        if (ii != null) {
            ii.setOnClickListener(this);
            ii.setOnLongClickListener(null);
        }
        return ii;
    }

    private FolderIcon createFolderIcon(ViewGroup parent, FolderInfo info) {
        return FolderIcon.fromXml(R.layout.folder_icon, this, parent, info);
    }

    private ShortcutIcon createShortcutIcon(ViewGroup parent, ShortcutInfo info) {
        return ShortcutIcon.fromXml(R.layout.application, this, parent, info);
    }

    public boolean isTogglesSelectViewShowing() {
        return this.mTogglesSelectView != null;
    }

    public void showTogglesSelectView() {
        if (this.mTogglesSelectView == null) {
            this.mTogglesSelectView = new TogglesSelectView(getApplicationContext(), this);
            this.mDragLayer.addView(this.mTogglesSelectView, new LayoutParams(-1, -1));
        }
    }

    public void exitTogglesSelectView(boolean isCancel) {
        if (this.mTogglesSelectView != null) {
            if (isSceneShowing()) {
                getSceneScreen().finishDropAddSpriteView(isCancel);
            }
            this.mDragLayer.removeView(this.mTogglesSelectView);
            this.mTogglesSelectView = null;
        }
    }

    private View completeAddShortcut(Intent data) {
        int cellX = 0;
        int cellY = 0;
        if ((this.mLastAddInfo instanceof ShortcutProviderInfo) || (this.mLastAddInfo instanceof ShortcutPlaceholderProviderInfo)) {
            cellX = this.mLastAddInfo.cellX;
            cellY = this.mLastAddInfo.cellY;
        }
        this.mLastAddInfo = null;
        if (this.mWorkspace.getCurrentCellScreen().isEditingNewScreenMode()) {
            this.mWorkspace.insertNewScreen(-1, false);
        }
        CellInfo cellInfo = findSingleSlot(cellX, cellY, true);
        if (cellInfo == null) {
            return null;
        }
        CellLayout cellLayout = this.mWorkspace.getCurrentCellLayout();
        ShortcutInfo info = this.mModel.getShortcutInfo(this, data, cellInfo);
        if (info == null) {
            return null;
        }
        View shortcut = createItemIcon(cellLayout, info);
        this.mModel.insertItemToDatabase(this, info);
        this.mWorkspace.addInScreen(shortcut, info.screenId, info.cellX, info.cellY, 1, 1, isWorkspaceLocked());
        return shortcut;
    }

    public void completeSelectToggle(int id) {
        if (isSceneShowing()) {
            getSceneScreen().completeSelectToggle(id);
        } else {
            completeAddShortcutToggle(id);
        }
    }

    private View completeAddShortcutToggle(int id) {
        Intent intent = new Intent("com.miui.action.TOGGLE_SHURTCUT");
        intent.putExtra("ToggleId", id);
        Intent data = new Intent();
        data.putExtra("android.intent.extra.shortcut.INTENT", intent);
        return completeAddShortcut(data);
    }

    private void completeAddAppWidget(Intent data) {
        int appWidgetId = data.getExtras().getInt("appWidgetId", -1);
        if (appWidgetId != -1 && (this.mLastAddInfo instanceof LauncherAppWidgetProviderInfo)) {
            LauncherAppWidgetProviderInfo providerInfo = this.mLastAddInfo;
            this.mLastAddInfo = null;
            LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId, providerInfo);
            if (providerInfo.screenId == -1) {
                addItemToWorkspace(launcherInfo, this.mWorkspace.getCurrentScreenId(), -100, providerInfo.cellX, providerInfo.cellY, null);
                return;
            }
            this.mModel.insertItemToDatabase(this, launcherInfo);
            this.mDesktopItems.add(launcherInfo);
            launcherInfo.hostView = this.mAppWidgetHost.createView(this, appWidgetId, providerInfo.providerInfo);
            launcherInfo.hostView.setAppWidget(appWidgetId, providerInfo.providerInfo);
            launcherInfo.hostView.setTag(launcherInfo);
            Rect size = this.mWorkspace.getCurrentCellLayout().getWidgetMeasureSpec(providerInfo.spanX, providerInfo.spanY, null);
            launcherInfo.hostView.updateAppWidgetSize(null, size.left, size.top, size.right, size.bottom);
            this.mWorkspace.addInCurrentScreen(launcherInfo.hostView, providerInfo.cellX, providerInfo.cellY, launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        this.mDesktopItems.remove(launcherInfo);
        launcherInfo.hostView = null;
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return this.mAppWidgetHost;
    }

    public void removeGadget(ItemInfo info) {
        if (info.itemType == 5) {
            Gadget gadget = null;
            Iterator i$ = this.mGadgets.iterator();
            while (i$.hasNext()) {
                Gadget g = (Gadget) i$.next();
                if (g.getTag().equals(info)) {
                    gadget = g;
                    break;
                }
            }
            if (gadget != null) {
                this.mGadgets.remove(gadget);
                gadget.onDestroy();
                gadget.onDeleted();
                int mockWidgetId = ((GadgetInfo) info).getMtzMockWidgetId();
                if (mockWidgetId != -1) {
                    this.mAppWidgetHost.deleteAppWidgetId(mockWidgetId);
                }
            }
            this.mWorkspace.onAlertGadget(info);
        }
    }

    public void reloadWidgetPreview() {
        this.mWorkspace.removeCallbacks(this.mReloadWidgetsRunnable);
        this.mWorkspace.postDelayed(this.mReloadWidgetsRunnable, 500);
    }

    void showError(int resId) {
        this.mErrorBar.showError(resId);
        this.mDeleteZone.hideEditingTips();
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();
        this.mWaitingForResult = false;
    }

    protected void onNewIntent(Intent intent) {
        boolean alreadyOnHome = false;
        super.onNewIntent(intent);
        this.mIsNewIntentNow = true;
        if (!isActivityLocked() && "android.intent.action.MAIN".equals(intent.getAction())) {
            closeSystemDialogs();
            this.mOnResumeExpectedForActivityResult = false;
            if (!locateApp(intent) && SystemClock.uptimeMillis() - this.mLastPausedTime <= 100) {
                if (this.mWorkspacePreview.isShowing()) {
                    this.mWorkspace.setCurrentScreen(this.mWorkspace.getDefaultScreenIndex());
                    showPreview(false, true);
                }
                if (isSceneShowing()) {
                    getSceneScreen().onNewIntent(intent);
                }
                if ((intent.getFlags() & 4194304) != 4194304) {
                    alreadyOnHome = true;
                }
                if (isTogglesSelectViewShowing()) {
                    exitTogglesSelectView(true);
                } else if (!alreadyOnHome) {
                } else {
                    if (this.mForceTouchLayer.isShowing()) {
                        this.mForceTouchLayer.closeForceTouch();
                        return;
                    }
                    sResumeWithUninstalling = this.mDeleteZone.onCancelUninstall();
                    boolean isFolderOpening = isFolderShowing();
                    if (isFolderOpening && !(this.mIsNewIntentNow && this.mIsAppLocating)) {
                        closeFolder();
                    }
                    if (isInEditing()) {
                        if (!(this.mIsNewIntentNow && this.mIsAppLocating)) {
                            closeFolder();
                        }
                        if (sResumeWithUninstalling) {
                            this.mWorkspace.postDelayed(new Runnable() {
                                public void run() {
                                    Launcher.this.setEditingState(7);
                                }
                            }, (long) getResources().getInteger(17694721));
                        } else {
                            setEditingState(7);
                        }
                    } else if (!sResumeWithUninstalling && !isFolderOpening) {
                        if (this.mMinusOneScreenView != null && this.mMinusOneScreenView.getCurrentScreenIndex() == 0) {
                            this.mMinusOneScreenView.snapToScreen(1);
                        }
                        if (!this.mWorkspace.isDefaultScreenShowing()) {
                            this.mWorkspace.moveToDefaultScreen(true);
                        }
                    }
                }
            }
        }
    }

    public ComponentName reConstructComponentName(String name) {
        ComponentName cn = ComponentName.unflattenFromString(name);
        if (cn != null || name == null) {
            return cn;
        }
        int sep = name.indexOf(47);
        if (sep > 0) {
            return new ComponentName(name.substring(0, sep), "");
        }
        return cn;
    }

    private boolean locateApp(Intent intent) {
        String locatedApp = intent.getStringExtra("locate_app");
        if (TextUtils.isEmpty(locatedApp)) {
            return false;
        }
        if (isFolderShowing()) {
            closeFolder(false);
        }
        if (isSceneShowing()) {
            hideSceneScreen(true);
        }
        if (isPreviewShowing()) {
            showPreview(false, true);
        }
        if (isInEditing()) {
            setEditingState(7);
        }
        ShortcutInfo target = getShortcutInfo(reConstructComponentName(locatedApp), Process.myUserHandle().getIdentifier());
        if (target == null || !locateAppInner(target)) {
            return false;
        }
        this.mIsAppLocating = true;
        return true;
    }

    public void onFinishHighlightLocatedApp() {
        this.mIsAppLocating = false;
    }

    private int getSnapToScreenIndexForLocate(ItemInfo target) {
        if (target.container != -101) {
            return this.mWorkspace.getScreenIndexById(target.screenId);
        }
        if (this.mWorkspace.getCurrentScreenType() == 2) {
            return this.mWorkspace.getNextTypeScreenIndex();
        }
        return this.mWorkspace.getCurrentScreenIndex();
    }

    private boolean locateAppInner(final ShortcutInfo targetInfo) {
        if (targetInfo == null) {
            return false;
        }
        ItemInfo targetItemInScreen;
        boolean needSlideScreen;
        int screenSnapMaxDuration;
        final FolderIcon folderParent = getParentFolderIcon(targetInfo);
        if (folderParent != null) {
            targetItemInScreen = (FolderInfo) folderParent.getTag();
        } else {
            targetItemInScreen = targetInfo;
        }
        final int snapToScreenIndex = getSnapToScreenIndexForLocate(targetItemInScreen);
        if (snapToScreenIndex != this.mWorkspace.getCurrentScreenIndex()) {
            needSlideScreen = true;
        } else {
            needSlideScreen = false;
        }
        final int interval = getResources().getInteger(R.integer.config_app_locate_interval);
        int i = interval + 300;
        if (needSlideScreen) {
            screenSnapMaxDuration = this.mWorkspace.getScreenSnapMaxDuration();
        } else {
            screenSnapMaxDuration = 0;
        }
        int delay = i + screenSnapMaxDuration;
        if (this.mMinusOneScreenView != null && this.mMinusOneScreenView.getCurrentScreenIndex() == 0) {
            this.mMinusOneScreenView.snapToScreen(1);
        }
        if (targetInfo.container == -100 || targetInfo.container == -101) {
            this.mDragLayer.postDelayed(new Runnable() {
                public void run() {
                    Launcher.this.mDragLayer.highlightLocatedApp(targetInfo.getBuddyIconView(), false);
                }
            }, (long) delay);
        } else if (folderParent == null) {
            return false;
        } else {
            folderParent.postDelayed(new Runnable() {
                public void run() {
                    Launcher.this.mDragLayer.postDelayed(new Runnable() {
                        public void run() {
                            Launcher.this.openFolder((FolderInfo) folderParent.getTag(), folderParent);
                            folderParent.postDelayed(new Runnable() {
                                public void run() {
                                    final FolderGridView folderGrid = Launcher.this.mFolderCling.getFolder().getContent();
                                    folderGrid.smoothScrollToPosition(targetInfo.cellX);
                                    Launcher.this.mAppLocateFolderScrollOffset = folderGrid.computeVerticalScrollOffset();
                                    folderGrid.postOnAnimationDelayed(new Runnable() {
                                        public void run() {
                                            View child = folderGrid.findViewWithTag(targetInfo);
                                            int newOffset = folderGrid.computeVerticalScrollOffset();
                                            if (child != null && Launcher.this.mAppLocateFolderScrollOffset == newOffset) {
                                                Launcher.this.mDragLayer.highlightLocatedApp((ShortcutIcon) child, false);
                                            } else if (Launcher.this.isFolderShowing()) {
                                                Launcher.this.mAppLocateFolderScrollOffset = newOffset;
                                                folderGrid.postOnAnimationDelayed(this, 30);
                                            } else {
                                                Launcher.this.onFinishHighlightLocatedApp();
                                            }
                                        }
                                    }, 30);
                                }
                            }, (long) (Folder.DEFAULT_FOLDER_OPEN_DURATION + interval));
                        }
                    }, (long) Launcher.this.mDragLayer.highlightLocatedApp(folderParent, true));
                }
            }, (long) delay);
        }
        if (needSlideScreen) {
            this.mWorkspace.postDelayed(new Runnable() {
                public void run() {
                    Launcher.this.mWorkspace.snapToScreen(snapToScreenIndex);
                }
            }, 300);
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        LauncherApplication app = Application.getLauncherApplication(this);
        if (app.getLauncher() != this) {
            Process.killProcess(Process.myPid());
            return;
        }
        TextKeyListener.getInstance().release();
        WallpaperUtils.onDestroy();
        this.mModel.stopLoader();
        ProgressManager.getManager(this).onDestroy();
        app.setLauncher(null);
        try {
            this.mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w("Launcher", "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        unbindDesktopItems();
        getContentResolver().unregisterContentObserver(this.mWidgetObserver);
        getContentResolver().unregisterContentObserver(this.mScreenChangeObserver);
        getContentResolver().unregisterContentObserver(this.mChildrenModeObserver);
        getContentResolver().unregisterContentObserver(this.mScreenCellsSizeObserver);
        getContentResolver().unregisterContentObserver(this.mLockWallpaperObserver);
        getContentResolver().unregisterContentObserver(this.mGlobalSearchSwitchObserver);
        unregisterReceiver(this.mBroadcastReceiver);
        unregisterReceiver(this.mWallpaperUtils);
        showPreview(false, false);
        this.mWorkspace.onDestroy();
        Application.getLauncherApplication(this).stopShakeMonitor();
        this.mApplicationsMessage.onDestroy();
        notifyGadgetStateChanged(6);
        sConfigurationChanged = isChangingConfigurations();
        try {
            closeTmpFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dismissLoadingProgressDialog();
        LauncherAnimUtils.onDestroyActivity();
        GadgetFactory.resetMtzGadgetList();
        this.mForegroundTaskQueue.onDestroy();
        if (sConfigurationChanged) {
            Utilities.resetResourceDependenceItem();
            if (sEnteredSceneScreen || SpecificDeviceConfig.isBigScreenLowMemory()) {
                Process.killProcess(Process.myPid());
            }
        }
        DownloadNotificationChecker.unbind();
    }

    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
        if (initialQuery == null) {
            initialQuery = getTypedText();
            clearTypedText();
        }
        ((SearchManager) getSystemService("search")).startSearch(initialQuery, selectInitialQuery, getComponentName(), appSearchData, globalSearch);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (isWorkspaceLocked() || sChildrenModeEnabled) {
            return false;
        }
        menu.add(2, 2, 0, R.string.menu_edit);
        menu.add(1, 3, 0, R.string.menu_reset_scene).setAlphabeticShortcut('R');
        menu.add(3, 4, 0, R.string.menu_exit_scene).setAlphabeticShortcut('X');
        return true;
    }

    private boolean prepareSceneMenu(Menu menu) {
        boolean z;
        boolean z2 = false;
        if (getSceneScreen().isInEditMode()) {
            menu.findItem(2).setTitle(R.string.menu_back_desktop).setAlphabeticShortcut('B');
        } else {
            menu.findItem(2).setTitle(R.string.menu_edit).setAlphabeticShortcut('E');
        }
        if (isPrivacyModeEnabled()) {
            z = false;
        } else {
            z = true;
        }
        menu.setGroupVisible(2, z);
        if (!isPrivacyModeEnabled()) {
            z2 = true;
        }
        menu.setGroupVisible(1, z2);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!isSceneShowing() || sChildrenModeEnabled) {
            return true;
        }
        if (getSceneScreen().isSelectViewShowing()) {
            return false;
        }
        return prepareSceneMenu(menu);
    }

    private boolean sceneOptionItemSelected(MenuItem item) {
        SceneScreen sceneScreen = getSceneScreen();
        switch (item.getItemId()) {
            case 2:
                if (sceneScreen.isInEditMode()) {
                    sceneScreen.exitEditMode();
                    return true;
                }
                sceneScreen.gotoEditMode();
                return true;
            case 3:
                sceneScreen.reset();
                return true;
            case 4:
                hideSceneScreen(true);
                return true;
            default:
                return false;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (isSceneShowing() && sceneOptionItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onScreenOrientationChanged() {
        this.mDragController.setIsScreenOrientationChanged(true);
        this.mDragController.cancelDrag();
        this.mDragController.setIsScreenOrientationChanged(false);
        this.mDragLayer.clearAllResizeFrames();
        this.mDragLayerBackground.onScreenOrientationChanged();
        closeFolder();
        this.mWorkspace.onScreenOrientationChanged();
        this.mFolderCling.updateLayout(isInNormalEditing());
        this.mHotSeats.onScreenOrientationChanged();
        if (this.mCurrentThumbnailView == this.mMultiSelectContainer) {
            this.mMultiSelectContainer.onScreenOrientationChanged();
        }
    }

    public void onScreenSizeChanged() {
        this.mWorkspace.onScreenSizeChanged();
        if (this.mForceTouchLayer.isShowing()) {
            this.mForceTouchLayer.closeForceTouch();
        }
    }

    public boolean onSearchRequested() {
        if (DeviceConfig.allowedSlidingUpToStartGolbalSearch()) {
            startSearch(null, false, null, true);
        }
        return true;
    }

    public boolean isWorkspaceLoading() {
        return this.mWorkspaceLoading;
    }

    public boolean isUninstallDialogShowing() {
        return this.mDeleteZone.isUninstallDialogShowing();
    }

    public boolean isWorkspaceLocked() {
        return this.mDeleteZone.isUninstallDialogShowing() || this.mDeleteZone.isUninstallAnimShowing() || isFolderShowing() || isActivityLocked();
    }

    public boolean isInSnapshotMode() {
        return new File("/data/system/themeScreenshotMode").exists();
    }

    public boolean isActivityLocked() {
        return this.mInAutoFilling || this.mWorkspaceLoading || this.mWaitingForResult || this.mIsAppLocating || this.mWorkspace.getCurrentCellLayout().isDropAnimating() || this.mDeleteZone.isUninstallAnimShowing();
    }

    int addAppWidget(LauncherAppWidgetProviderInfo info) {
        AppWidgetProviderInfo widgetInfo = info.providerInfo;
        CellInfo cellInfo = findSlot(info.cellX, info.cellY, info.spanX, info.spanY, false);
        if (cellInfo == null) {
            showError(R.string.failed_to_drop_widget_nospace);
            return -1;
        }
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        String pkgName = widgetInfo.provider.getPackageName();
        try {
            this.mAppWidgetManager.bindAppWidgetId(appWidgetId, widgetInfo.provider);
            info.cellX = cellInfo.cellX;
            info.cellY = cellInfo.cellY;
            info.screenId = cellInfo.screenId;
            this.mLastAddInfo = info;
            if (widgetInfo.configure != null) {
                Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
                intent.setComponent(widgetInfo.configure);
                intent.putExtra("appWidgetId", appWidgetId);
                startActivityForResult(intent, 5);
                return appWidgetId;
            }
            String packageName = widgetInfo.provider.getPackageName();
            String className = widgetInfo.provider.getClassName();
            Intent data = new Intent("android.intent.action.MAIN", null);
            data.addCategory("android.intent.category.DEFAULT");
            data.putExtra("appWidgetId", appWidgetId);
            if (packageName == null || className == null) {
                data.setAction("android.intent.action.CREATE_SHORTCUT");
                data.putExtra("android.intent.extra.shortcut.NAME", widgetInfo.label);
            } else {
                data.setClassName(packageName, className);
            }
            onActivityResult(5, -1, data);
            this.mOnResumeExpectedForActivityResult = false;
            return appWidgetId;
        } catch (IllegalArgumentException e) {
            this.mErrorBar.showError(R.string.failed_to_drop_widget_invalid);
            return -1;
        }
    }

    public void reloadGadget(int gadgetId) {
        Iterator i$ = new ArrayList(this.mGadgets).iterator();
        while (i$.hasNext()) {
            Gadget gadget = (Gadget) i$.next();
            GadgetInfo gadgetInfo = (GadgetInfo) gadget.getTag();
            if (gadgetInfo.getGadgetId() == gadgetId) {
                CellLayout cl = this.mWorkspace.getCellLayoutById(gadgetInfo.screenId);
                if (cl != null) {
                    cl.removeView(gadget);
                    this.mGadgets.remove(gadget);
                    gadget.onDestroy();
                    addGadget(gadgetInfo, false);
                }
            }
        }
    }

    View addGadget(GadgetInfo info, boolean insertToDB) {
        Gadget gadget = GadgetFactory.createGadget(this, info, 101);
        if (gadget == null) {
            return null;
        }
        if (insertToDB) {
            this.mModel.insertItemToDatabase(this, info);
        }
        gadget.onAdded();
        gadget.onCreate();
        this.mWorkspace.addInScreen(gadget, info.screenId, info.cellX, info.cellY, info.spanX, info.spanY, false);
        this.mWorkspace.requestLayout();
        this.mGadgets.add(gadget);
        if (info.screenId == this.mWorkspace.getCurrentScreenId()) {
            gadget.onResume();
            this.mWorkspace.onAlertGadget(info);
        }
        if (isInEditing()) {
            gadget.onEditNormal();
        }
        ComponentName mockProvider = info.getMtzMockWidgetProvider();
        if (mockProvider == null) {
            return gadget;
        }
        String pkgName = mockProvider.getPackageName();
        try {
            int appWidgetId = info.getMtzMockWidgetId();
            if (appWidgetId == -1 || this.mAppWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
                appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                info.setMtzMockWidgetId(appWidgetId);
            }
            this.mAppWidgetManager.bindAppWidgetId(appWidgetId, mockProvider);
            return gadget;
        } catch (IllegalArgumentException e) {
            return gadget;
        }
    }

    Gadget findGadget(long itemId) {
        Iterator i$ = this.mGadgets.iterator();
        while (i$.hasNext()) {
            Gadget g = (Gadget) i$.next();
            if (((GadgetInfo) g.getTag()).id == itemId) {
                return g;
            }
        }
        return null;
    }

    void onDropShortcut(DragObject d, Intent intent) {
        this.mLastAddInfo = d == null ? null : d.getDragInfo();
        startActivityForResult(intent, 1);
    }

    View onDropToggleShortcut(DragObject d) {
        this.mLastAddInfo = d == null ? null : d.getDragInfo();
        return completeAddShortcutToggle(((ShortcutInfo) d.getDragInfo()).getToggleId());
    }

    View onDropSettingShortcut(DragObject d) {
        this.mLastAddInfo = d == null ? null : d.getDragInfo();
        return completeAddShortcut(((ShortcutInfo) d.getDragInfo()).intent);
    }

    FolderIcon addFolderToCurrentScreen(FolderInfo info, int cellX, int cellY) {
        FolderIcon newFolder;
        closeFolder();
        if (info.id == -1) {
            newFolder = createNewFolder(this.mWorkspace.getCurrentScreenId(), cellX, cellY);
        } else {
            FolderIcon newFolder2 = createFolderIcon(this.mWorkspace.getCurrentCellLayout(), info);
            LauncherModel.moveItemInDatabase(this, info, -100, this.mWorkspace.getCurrentScreenId(), cellX, cellY);
            newFolder = newFolder2;
        }
        this.mWorkspace.addInCurrentScreen(newFolder, cellX, cellY, 1, 1, isWorkspaceLocked());
        newFolder.setOnClickListener(this);
        return newFolder;
    }

    FolderIcon createNewFolder(long screenID, int cellX, int cellY) {
        return createNewFolder(screenID, cellX, cellY, null);
    }

    FolderIcon createNewFolder(long screenID, int cellX, int cellY, String title) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
        if (TextUtils.isEmpty(title)) {
            folderInfo.setTitle(getResources().getResourceName(R.string.folder_name), this);
        } else {
            folderInfo.setTitle(title, this);
        }
        folderInfo.container = -100;
        folderInfo.screenId = screenID;
        folderInfo.cellX = cellX;
        folderInfo.cellY = cellY;
        this.mModel.insertItemToDatabase(this, folderInfo);
        mFolders.put(Long.valueOf(folderInfo.id), folderInfo);
        return (FolderIcon) createItemIcon(this.mWorkspace.getCurrentCellLayout(), folderInfo);
    }

    void preRemoveItem(View v) {
        ViewGroup vg = (ViewGroup) v.getParent();
        if (vg instanceof CellLayout) {
            ((CellLayout) vg).preRemoveView(v);
        }
    }

    void removeFolder(FolderIcon folder) {
        ((ViewGroup) folder.getParent()).removeView(folder);
        FolderInfo info = (FolderInfo) folder.getTag();
        LauncherModel.deleteUserFolderContentsFromDatabase(this, info);
        removeFolder(info);
    }

    void removeFolder(FolderInfo folder) {
        mFolders.remove(Long.valueOf(folder.id));
        folder.removeRecommendAppsViewKey(getApplicationContext());
    }

    static boolean removeShortcutFromWorkspace(Context context, RemoveInfo ri, ShortcutInfo info) {
        if (ri.packageName.equals(info.getPackageName()) && ri.user.equals(info.getUser())) {
            if (!(info.itemType == 0 || info.isPresetApp() || ri.replacing)) {
                LauncherModel.deleteItemFromDatabase(context, info);
            }
            if (!(info.itemType == 1 && ri.replacing && !info.isPresetApp())) {
                return true;
            }
        }
        return false;
    }

    private CellInfo findSingleSlot(int cellX, int cellY, boolean showError) {
        return findSlot(cellX, cellY, 1, 1, showError);
    }

    private CellInfo findSlot(int cellX, int cellY, int spanX, int spanY, boolean showError) {
        return findSlot(-1, cellX, cellY, spanX, spanY, showError);
    }

    private CellInfo findSlot(long screenId, int cellX, int cellY, int spanX, int spanY, boolean showError) {
        CellLayout cellLayout = screenId == -1 ? this.mWorkspace.getCurrentCellLayout() : this.mWorkspace.getCellLayout(this.mWorkspace.getScreenIndexById(screenId));
        if (cellLayout == null) {
            return null;
        }
        int[] slot = cellLayout.findNearestVacantAreaByCellPos(cellX, cellY, spanX, spanY, false);
        if (slot != null) {
            CellInfo cellinfo = new CellInfo();
            cellinfo.cellX = slot[0];
            cellinfo.cellY = slot[1];
            cellinfo.spanX = spanX;
            cellinfo.spanY = spanY;
            cellinfo.container = -100;
            cellinfo.screenId = this.mWorkspace.getCurrentScreenId();
            return cellinfo;
        } else if (!showError) {
            return null;
        } else {
            showError(R.string.out_of_space);
            return null;
        }
    }

    public void startWallpaper(Intent intent) {
        startActivityForResult(intent, 10);
        this.mOnResumeExpectedForActivityResult = true;
    }

    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true, this.mWidgetObserver);
        resolver.registerContentObserver(Screens.CONTENT_URI, true, this.mScreenChangeObserver);
        resolver.registerContentObserver(Secure.getUriFor("children_mode_enabled"), false, this.mChildrenModeObserver);
        resolver.registerContentObserver(Settings.System.getUriFor("miui_home_screen_cells_size"), false, this.mScreenCellsSizeObserver);
        resolver.registerContentObserver(Settings.System.getUriFor("lock_wallpaper_provider_authority"), false, this.mLockWallpaperObserver);
        resolver.registerContentObserver(DeviceConfig.GLOBAL_SEARCH_SWITCH_URI, true, this.mGlobalSearchSwitchObserver);
    }

    private void registerBroadcastReceivers() {
        if (this.mBroadcastReceiver == null) {
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    final Intent tmpIntent = intent;
                    Launcher.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (!Launcher.this.isWorkspaceLoading() && tmpIntent.getAction().equals("android.intent.action.PRIVACY_MODE_CHANGED") && Launcher.this.isPrivacyModeEnabled()) {
                                Launcher.this.mDeleteZone.onCancelUninstall();
                                Launcher.this.closeFolder();
                                Launcher.this.mDragLayer.clearAllResizeFrames();
                                Launcher.this.showPreview(false, false);
                                Launcher.this.setEditingState(7);
                                if (Launcher.this.getSceneScreen() != null) {
                                    Launcher.this.getSceneScreen().exitEditableMode(false, true);
                                }
                            } else if ("android.provider.Telephony.SECRET_CODE".equals(tmpIntent.getAction())) {
                                String host = tmpIntent.getData().getHost();
                                if ("4663".equals(host)) {
                                    try {
                                        Launcher.this.getContentResolver().acquireProvider(Favorites.CONTENT_URI).call(Launcher.this.getPackageName(), "dumpDefaultWorkspace", String.valueOf(Launcher.this.mWorkspace.getScreenIdByIndex(Launcher.this.mWorkspace.getDefaultScreenIndex())), null);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                } else if ("969767".equals(host)) {
                                    Launcher.this.mEnableDemoMode = !Launcher.this.mEnableDemoMode;
                                    PreferenceManager.getDefaultSharedPreferences(Launcher.this).edit().putBoolean("pref_key_enable_demo_mode", Launcher.this.mEnableDemoMode).commit();
                                    Toast.makeText(Launcher.this, (Launcher.this.mEnableDemoMode ? "enable" : "disable") + " demo mode.", 100).show();
                                }
                            } else if ("android.intent.action.USER_PRESENT".equals(tmpIntent.getAction())) {
                                if (!WallpaperUtils.isKeyguardShowLiveWallpaper()) {
                                    Launcher.this.showUserPersentAnimation(false);
                                }
                                Launcher.this.mChangeByCloud = WallpaperUtils.setLockWallpaperProviderByCloud(Launcher.this);
                                Launcher.this.autoChangeLockWallpaper(false);
                            } else if ("android.intent.action.SCREEN_ON".equals(tmpIntent.getAction())) {
                                if (!Launcher.this.isWorkspaceLoading()) {
                                    ActivityManager am = (ActivityManager) Launcher.this.getSystemService("activity");
                                    if (((KeyguardManager) Launcher.this.getSystemService("keyguard")).isKeyguardLocked()) {
                                        ComponentName cn = ((RunningTaskInfo) am.getRunningTasks(1).get(0)).topActivity;
                                        if (cn != null && Launcher.class.getName().equals(cn.getClassName())) {
                                            Launcher.this.prepairUserPersentAnimation();
                                        }
                                    }
                                }
                                if (!Launcher.this.mLaunchAppFromFolder && Launcher.this.isFolderShowing() && !Launcher.this.mWaitingForMarketDetail) {
                                    Launcher.this.closeFolder(false);
                                }
                            } else if ("android.intent.action.SYSTEM_UI_VISIBILITY_CHANGED".equals(tmpIntent.getAction())) {
                                if (!Launcher.this.isResumed()) {
                                    return;
                                }
                                if (tmpIntent.getBooleanExtra("is_show", false)) {
                                    RenderThread.globalThread().setPaused(true);
                                } else {
                                    RenderThread.globalThread().setPaused(false);
                                }
                            } else if ("android.miui.REQUEST_LOCKSCREEN_WALLPAPER".equals(tmpIntent.getAction())) {
                                String wallpaperInfo = tmpIntent.getStringExtra("wallpaperInfo");
                                Uri uri = null;
                                if (!TextUtils.isEmpty(wallpaperInfo)) {
                                    String wallpaperUri = ((WallpaperInfo) Launcher.this.mGson.fromJson(wallpaperInfo, WallpaperInfo.class)).wallpaperUri;
                                    if (!TextUtils.isEmpty(wallpaperUri)) {
                                        uri = Uri.parse(wallpaperUri);
                                    }
                                    if (uri != null) {
                                        final String str = wallpaperInfo;
                                        new AsyncTask<Uri, Void, Boolean>() {
                                            protected Boolean doInBackground(Uri... params) {
                                                if (tmpIntent.getBooleanExtra("apply", false)) {
                                                    return Boolean.valueOf(WallpaperUtils.setLockWallpaper(params[0], true));
                                                }
                                                return Boolean.valueOf(true);
                                            }

                                            protected void onPostExecute(Boolean result) {
                                                if (result.booleanValue()) {
                                                    PreferenceManager.getDefaultSharedPreferences(Launcher.this).edit().putString("currentWallpaperInfo", str).commit();
                                                    return;
                                                }
                                                Intent intent = new Intent("com.miui.keyguard.setwallpaper");
                                                intent.putExtra("set_lock_wallpaper_result", false);
                                                Launcher.this.sendBroadcast(intent);
                                            }
                                        }.execute(new Uri[]{uri});
                                        return;
                                    }
                                    Intent intent = new Intent("com.miui.keyguard.setwallpaper");
                                    intent.putExtra("set_lock_wallpaper_result", false);
                                    Launcher.this.sendBroadcast(intent);
                                } else if (tmpIntent.hasExtra("showTime")) {
                                    Launcher.this.startLockWallpaperPreviewActivity(tmpIntent.getLongExtra("showTime", 0));
                                } else {
                                    Launcher.this.autoChangeLockWallpaper(true);
                                }
                            } else if (VERSION.SDK_INT >= 21 && ("android.intent.action.MANAGED_PROFILE_ADDED".equals(tmpIntent.getAction()) || "android.intent.action.MANAGED_PROFILE_REMOVED".equals(tmpIntent.getAction()))) {
                                Launcher.this.mModel.forceReload(Launcher.this.getApplicationContext());
                            } else if ("miui.intent.action.MIUI_REGION_CHANGED".equals(tmpIntent.getAction())) {
                                Launcher.this.mModel.forceReload(Launcher.this.getApplicationContext());
                            } else if ("com.xiaomi.mihomemanager.clearMiuiHome".equals(tmpIntent.getAction()) && "com.xiaomi.mihomemanager".equals(tmpIntent.getSender())) {
                                Application.getLauncherApplication(Launcher.this).getLauncherProvider().loadDefaultWorkspace();
                                Process.killProcess(Process.myPid());
                            } else if ("com.miui.home.notification".equals(tmpIntent.getAction())) {
                                ((NotificationManager) Launcher.this.getSystemService("notification")).cancel(tmpIntent.getStringExtra("com.miui.home.notification.title"), 0);
                                Intent toOpenIntent = (Intent) tmpIntent.getParcelableExtra("com.miui.home.notification.extra");
                                int userId = tmpIntent.getIntExtra("com.miui.home.notification.userId", Process.myUserHandle().getIdentifier());
                                if (toOpenIntent != null) {
                                    Launcher.this.startActivity(toOpenIntent);
                                    ShortcutInfo shortcutInfo = Launcher.this.getShortcutInfo(toOpenIntent.getComponent(), userId);
                                    if (shortcutInfo != null) {
                                        shortcutInfo.onLaunch(Launcher.this);
                                    }
                                }
                            }
                        }
                    });
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PRIVACY_MODE_CHANGED");
            registerReceiver(this.mBroadcastReceiver, filter);
            filter = new IntentFilter("android.provider.Telephony.SECRET_CODE");
            filter.addDataScheme("android_secret_code");
            registerReceiver(this.mBroadcastReceiver, filter);
            registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.USER_PRESENT"));
            registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.SCREEN_ON"));
            registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.SYSTEM_UI_VISIBILITY_CHANGED"));
            registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.miui.REQUEST_LOCKSCREEN_WALLPAPER"));
            registerReceiver(this.mBroadcastReceiver, new IntentFilter("miui.intent.action.MIUI_REGION_CHANGED"));
            registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.xiaomi.mihomemanager.clearMiuiHome"));
            if (VERSION.SDK_INT >= 21) {
                registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.MANAGED_PROFILE_ADDED"));
                registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.MANAGED_PROFILE_REMOVED"));
            }
            registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.miui.home.notification"));
        }
    }

    private Bundle generateDefaultParams(long showTime) {
        return null;
    }

    private Bundle geneateParams(long showTime, String current, String wallpapers, String ads, String dialogComponent) {
        Bundle bundle = new Bundle();
        bundle.putLong("showTime", showTime);
        bundle.putString("currentWallpaperInfo", current);
        bundle.putString("wallpaperInfos", wallpapers);
        bundle.putString("adWallpaperInfos", ads);
        bundle.putString("dialogComponent", dialogComponent);
        return bundle;
    }

    private void startLockWallpaperPreviewActivity(long showTime) {
        if (!Build.IS_TABLET && !this.mIsStartingLockWallpaperPreviewActivity) {
            AsyncTask<Long, Void, Bundle> task = new AsyncTask<Long, Void, Bundle>() {
                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                protected android.os.Bundle doInBackground(java.lang.Long... r25) {
                    /*
                    r24 = this;
                    r5 = 0;
                    r5 = r25[r5];
                    r6 = r5.longValue();
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r11 = 1;
                    r5.mIsStartingLockWallpaperPreviewActivity = r11;
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r11 = 0;
                    r5.mPreviewComponent = r11;
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r11 = 0;
                    r5.mDialogComponent = r11;
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r16 = com.miui.home.launcher.WallpaperUtils.getLockWallpaperProvider(r5);
                    r15 = "com.xiaomi.ad.LockScreenAdProvider";
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = com.miui.home.launcher.WallpaperUtils.hasValidProvider(r5);
                    if (r5 != 0) goto L_0x0033;
                L_0x0031:
                    r16 = com.miui.home.launcher.WallpaperUtils.sDefaultLockWallpaperProvider;
                L_0x0033:
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = r5.getContentResolver();
                    r11 = new java.lang.StringBuilder;
                    r11.<init>();
                    r22 = "content://";
                    r0 = r22;
                    r11 = r11.append(r0);
                    r0 = r16;
                    r11 = r11.append(r0);
                    r11 = r11.toString();
                    r11 = android.net.Uri.parse(r11);
                    r14 = r5.acquireUnstableProvider(r11);
                    if (r14 != 0) goto L_0x0065;
                L_0x005c:
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = r5.generateDefaultParams(r6);
                L_0x0064:
                    return r5;
                L_0x0065:
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = r5.getContentResolver();
                    r11 = new java.lang.StringBuilder;
                    r11.<init>();
                    r22 = "content://";
                    r0 = r22;
                    r11 = r11.append(r0);
                    r11 = r11.append(r15);
                    r11 = r11.toString();
                    r11 = android.net.Uri.parse(r11);
                    r12 = r5.acquireUnstableProvider(r11);
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r21 = android.preference.PreferenceManager.getDefaultSharedPreferences(r5);
                    r5 = "currentWallpaperInfo";
                    r11 = 0;
                    r0 = r21;
                    r8 = r0.getString(r5, r11);
                    r9 = 0;
                    r10 = 0;
                    r17 = new com.miui.home.launcher.lockwallpaper.mode.RequestInfo;	 Catch:{ Exception -> 0x0177 }
                    r17.<init>();	 Catch:{ Exception -> 0x0177 }
                    r5 = 2;
                    r0 = r17;
                    r0.mode = r5;	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r5 = r5.mGson;	 Catch:{ Exception -> 0x0177 }
                    r11 = com.miui.home.launcher.lockwallpaper.mode.WallpaperInfo.class;
                    r5 = r5.fromJson(r8, r11);	 Catch:{ Exception -> 0x0177 }
                    r5 = (com.miui.home.launcher.lockwallpaper.mode.WallpaperInfo) r5;	 Catch:{ Exception -> 0x0177 }
                    r0 = r17;
                    r0.currentWallpaperInfo = r5;	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r5 = r5.mGson;	 Catch:{ Exception -> 0x0177 }
                    r0 = r17;
                    r18 = r5.toJson(r0);	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r0 = r18;
                    r20 = r5.getLockWallpaperListFromProvider(r14, r0);	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r5 = r5.mGson;	 Catch:{ Exception -> 0x0177 }
                    r11 = com.miui.home.launcher.lockwallpaper.mode.ResultInfo.class;
                    r0 = r20;
                    r19 = r5.fromJson(r0, r11);	 Catch:{ Exception -> 0x0177 }
                    r19 = (com.miui.home.launcher.lockwallpaper.mode.ResultInfo) r19;	 Catch:{ Exception -> 0x0177 }
                    if (r19 == 0) goto L_0x0151;
                L_0x00e7:
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r0 = r19;
                    r11 = r0.previewComponent;	 Catch:{ Exception -> 0x0177 }
                    r5.mPreviewComponent = r11;	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r0 = r19;
                    r11 = r0.dialogComponent;	 Catch:{ Exception -> 0x0177 }
                    r5.mDialogComponent = r11;	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r5 = r5.mGson;	 Catch:{ Exception -> 0x0177 }
                    r0 = r19;
                    r11 = r0.wallpaperInfos;	 Catch:{ Exception -> 0x0177 }
                    r9 = r5.toJson(r11);	 Catch:{ Exception -> 0x0177 }
                    r0 = r19;
                    r5 = r0.wallpaperInfos;	 Catch:{ Exception -> 0x0177 }
                    r0 = r17;
                    r0.wallpaperInfos = r5;	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r5 = r5.mGson;	 Catch:{ Exception -> 0x0177 }
                    r0 = r17;
                    r18 = r5.toJson(r0);	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r22 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                    r0 = r18;
                    r1 = r22;
                    r4 = r5.getLockWallpaperListFromProvider(r12, r0, r1);	 Catch:{ Exception -> 0x0177 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r5 = r5.mGson;	 Catch:{ Exception -> 0x0177 }
                    r11 = com.miui.home.launcher.lockwallpaper.mode.ResultInfo.class;
                    r19 = r5.fromJson(r4, r11);	 Catch:{ Exception -> 0x0177 }
                    r19 = (com.miui.home.launcher.lockwallpaper.mode.ResultInfo) r19;	 Catch:{ Exception -> 0x0177 }
                    if (r19 == 0) goto L_0x0151;
                L_0x0141:
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;	 Catch:{ Exception -> 0x0177 }
                    r5 = r5.mGson;	 Catch:{ Exception -> 0x0177 }
                    r0 = r19;
                    r11 = r0.wallpaperInfos;	 Catch:{ Exception -> 0x0177 }
                    r10 = r5.toJson(r11);	 Catch:{ Exception -> 0x0177 }
                L_0x0151:
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = r5.getContentResolver();
                    r5.releaseProvider(r14);
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = r5.getContentResolver();
                    r5.releaseProvider(r12);
                L_0x0167:
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r0 = r24;
                    r11 = com.miui.home.launcher.Launcher.this;
                    r11 = r11.mDialogComponent;
                    r5 = r5.geneateParams(r6, r8, r9, r10, r11);
                    goto L_0x0064;
                L_0x0177:
                    r13 = move-exception;
                    r13.printStackTrace();	 Catch:{ all -> 0x0192 }
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = r5.getContentResolver();
                    r5.releaseProvider(r14);
                    r0 = r24;
                    r5 = com.miui.home.launcher.Launcher.this;
                    r5 = r5.getContentResolver();
                    r5.releaseProvider(r12);
                    goto L_0x0167;
                L_0x0192:
                    r5 = move-exception;
                    r0 = r24;
                    r11 = com.miui.home.launcher.Launcher.this;
                    r11 = r11.getContentResolver();
                    r11.releaseProvider(r14);
                    r0 = r24;
                    r11 = com.miui.home.launcher.Launcher.this;
                    r11 = r11.getContentResolver();
                    r11.releaseProvider(r12);
                    throw r5;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.Launcher.20.doInBackground(java.lang.Long[]):android.os.Bundle");
                }

                protected void onPostExecute(Bundle result) {
                    Launcher.this.mIsStartingLockWallpaperPreviewActivity = false;
                    if (result == null) {
                        Intent intent = new Intent("com.miui.keyguard.setwallpaper");
                        intent.putExtra("set_lock_wallpaper_result", false);
                        Launcher.this.sendBroadcast(intent);
                        return;
                    }
                    intent = new Intent();
                    ComponentName component = null;
                    if (Launcher.this.mPreviewComponent != null) {
                        component = ComponentName.unflattenFromString(Launcher.this.mPreviewComponent);
                    }
                    if (component != null) {
                        intent.setComponent(component);
                    } else {
                        intent.setClassName("com.miui.home", "com.miui.home.launcher.lockwallpaper.LockWallpaperPreviewActivity");
                    }
                    intent.addFlags(268435456);
                    intent.putExtras(result);
                    try {
                        Launcher.this.startActivity(intent, ActivityOptions.makeCustomAnimation(Launcher.this, 0, 0, new Handler(), null).toBundle());
                    } catch (Exception ex) {
                        Log.e("Launcher", "start activity failed.", ex);
                    }
                }
            }.execute(new Long[]{Long.valueOf(showTime)});
        }
    }

    private String getLockWallpaperListFromProvider(IContentProvider provider, String requestJson) {
        String str = null;
        try {
            Bundle extras = new Bundle();
            extras.putString("request_json", requestJson);
            Bundle result = provider.call(getPackageName(), "getNextLockWallpaperUri", null, extras);
            if (result != null) {
                str = result.getString("result_json");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private String getLockWallpaperListFromProvider(final IContentProvider provider, final String requestJson, long timeOut) {
        if (timeOut <= 0) {
            return getLockWallpaperListFromProvider(provider, requestJson);
        }
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                return Launcher.this.getLockWallpaperListFromProvider(provider, requestJson);
            }
        }.execute(new Void[0]);
        try {
            return (String) task.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            task.cancel(true);
            e.printStackTrace();
            return null;
        }
    }

    private void autoChangeLockWallpaper(final boolean forceRefresh) {
        if (WallpaperUtils.isDefaultLockStyle() && !this.mIsChangingLockWallpaper) {
            new AsyncTask<Void, Void, Boolean>() {
                protected Boolean doInBackground(Void... params) {
                    Launcher.this.mIsChangingLockWallpaper = true;
                    String providerInCharge = WallpaperUtils.getLockWallpaperProvider(Launcher.this);
                    String providerAd = "com.xiaomi.ad.LockScreenAdProvider";
                    if (TextUtils.isEmpty(providerInCharge) || "com.miui.home.none_provider".equals(providerInCharge)) {
                        return Boolean.valueOf(false);
                    }
                    IContentProvider provider = Launcher.this.getContentResolver().acquireUnstableProvider(Uri.parse("content://" + providerInCharge));
                    if (provider == null) {
                        return Boolean.valueOf(false);
                    }
                    Launcher.this.getContentResolver().releaseProvider(provider);
                    if (Launcher.this.setLockWallpaperFromProvider(providerAd, forceRefresh, providerInCharge, false)) {
                        Launcher.this.mNeedLast = true;
                        return Boolean.valueOf(true);
                    }
                    boolean ret = Launcher.this.setLockWallpaperFromProvider(providerInCharge, forceRefresh, providerInCharge, Launcher.this.mNeedLast);
                    Launcher.this.mNeedLast = false;
                    return Boolean.valueOf(ret);
                }

                protected void onPostExecute(Boolean result) {
                    Launcher.this.mIsChangingLockWallpaper = false;
                    if (!result.booleanValue()) {
                        Intent intent = new Intent("com.miui.keyguard.setwallpaper");
                        intent.putExtra("set_lock_wallpaper_result", result);
                        Launcher.this.sendBroadcast(intent);
                    }
                }
            }.execute(new Void[0]);
        }
    }

    private boolean setLockWallpaperFromProvider(String providerName, boolean forceRefresh, String providerInCharge, boolean needLast) {
        IContentProvider provider = getContentResolver().acquireUnstableProvider(Uri.parse("content://" + providerName));
        boolean z;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String currentWallpaperInfo = sp.getString("currentWallpaperInfo", null);
            RequestInfo requestInfo = new RequestInfo();
            requestInfo.mode = 1;
            requestInfo.currentWallpaperInfo = (WallpaperInfo) this.mGson.fromJson(currentWallpaperInfo, WallpaperInfo.class);
            requestInfo.needLast = needLast;
            String requestJson = this.mGson.toJson(requestInfo);
            Bundle extras = new Bundle();
            extras.putBoolean("force_refresh", forceRefresh);
            extras.putString("extra_current_provider", providerInCharge);
            extras.putString("request_json", requestJson);
            if (provider == null) {
                z = false;
                return z;
            }
            Bundle result = provider.call(getPackageName(), "getNextLockWallpaperUri", null, extras);
            if (result == null) {
                getContentResolver().releaseUnstableProvider(provider);
                return false;
            }
            Uri wallpaperUri;
            String resultJson = result.getString("result_json");
            String uriString;
            if (TextUtils.isEmpty(resultJson)) {
                uriString = result.getString("result_string");
                if (TextUtils.isEmpty(uriString)) {
                    getContentResolver().releaseUnstableProvider(provider);
                    return false;
                }
                wallpaperUri = Uri.parse(uriString);
                sp.edit().remove("currentWallpaperInfo").commit();
            } else {
                ResultInfo resultInfo = (ResultInfo) this.mGson.fromJson(resultJson, ResultInfo.class);
                if (resultInfo == null) {
                    getContentResolver().releaseUnstableProvider(provider);
                    return false;
                }
                List<WallpaperInfo> wallpaperList = resultInfo.wallpaperInfos;
                if (wallpaperList.size() <= 0) {
                    getContentResolver().releaseUnstableProvider(provider);
                    return false;
                }
                WallpaperInfo info = (WallpaperInfo) wallpaperList.get(0);
                uriString = info.wallpaperUri;
                if (TextUtils.isEmpty(uriString)) {
                    getContentResolver().releaseUnstableProvider(provider);
                    return false;
                }
                wallpaperUri = Uri.parse(uriString);
                sp.edit().putString("currentWallpaperInfo", this.mGson.toJson(info)).commit();
            }
            z = WallpaperUtils.setLockWallpaper(wallpaperUri, true);
            getContentResolver().releaseUnstableProvider(provider);
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            z = false;
        } finally {
            getContentResolver().releaseUnstableProvider(provider);
        }
    }

    private void changeToDefaultLockWallpaper() {
        if (WallpaperUtils.isDefaultLockStyle()) {
            WallpaperUtils.resetLockWallpaperToDefault();
            sendBroadcast(new Intent("com.miui.keyguard.setwallpaper"));
        }
    }

    private boolean applyingDefaultTheme() {
        File themeFolder = new File("/data/system/theme");
        if (!themeFolder.exists() || !themeFolder.isDirectory()) {
            return true;
        }
        File[] content = themeFolder.listFiles();
        if (content == null) {
            return true;
        }
        for (File file : content) {
            if (!file.isDirectory() && (file.getName().contains("icons") || file.getName().contains(getPackageName()))) {
                return false;
            }
        }
        return true;
    }

    private void registerWallpaperChangedReceiver() {
        if (this.mWallpaperUtils != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.WALLPAPER_CHANGED");
            filter.addAction("com.miui.keyguard.setwallpaper");
            registerReceiver(this.mWallpaperUtils, filter);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isActivityLocked()) {
            return false;
        }
        if (event.getAction() != 0) {
            if (event.getAction() == 1 && !event.isCanceled()) {
                switch (event.getKeyCode()) {
                    case 3:
                        return true;
                    default:
                        break;
                }
            }
        }
        switch (event.getKeyCode()) {
            case 3:
                return true;
            case 25:
                if (SystemProperties.getInt("debug.launcher2.dumpstate", 0) != 0) {
                    dumpState();
                    return true;
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        if ((this.mMinusOneScreenView != null && this.mMinusOneScreenView.getCurrentScreenIndex() == 0) || isFolderShowing()) {
            return false;
        }
        if (isSceneShowing()) {
            return true;
        }
        if (isInNormalEditing()) {
            setEditingState(7);
            return false;
        } else if (isInEditing()) {
            return false;
        } else {
            AnalyticalDataCollector.trackEditMode("menu");
            setEditingState(8);
            return false;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isActivityLocked()) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onBackPressed() {
        if (this.mForceTouchLayer.isShowing()) {
            this.mForceTouchLayer.closeForceTouch();
        } else if (this.mWorkspacePreview.isShowing()) {
            showPreview(false, true);
        } else if (!this.mDeleteZone.onCancelUninstall() && !this.mFolderCling.stepClose()) {
            if (this.mWidgetThumbnailView.hasGroupUnfolding()) {
                this.mWidgetThumbnailView.foldingGroupMembers();
            } else if (this.mWallpaperThumbnailView.hasGroupUnfolding()) {
                this.mWallpaperThumbnailView.foldingGroupMembers();
            } else if (this.mEditingState == 11 || this.mEditingState == 12 || this.mEditingState == 13 || this.mEditingState == 14) {
                setEditingState(8);
            } else if (this.mEditingState == 10) {
                setEditingState(8);
            } else if (!isSceneShowing() || !getSceneScreen().onBackPressed()) {
                if (isTogglesSelectViewShowing()) {
                    exitTogglesSelectView(true);
                    return;
                }
                this.mDragLayer.clearAllResizeFrames();
                forceHideErrorBar();
                setEditingState(7);
            }
        }
    }

    private void onAppWidgetReset() {
        this.mAppWidgetHost.startListening();
    }

    private void unbindDesktopItems() {
        Iterator i$ = this.mDesktopItems.iterator();
        while (i$.hasNext()) {
            ((ItemInfo) i$.next()).unbind();
        }
    }

    public void onClick(View v) {
        if ((this.mEasterEggs == null || !this.mEasterEggs.onClick(v)) && !this.mDeleteZone.isUninstallDialogShowing()) {
            ShortcutInfo tag = v.getTag();
            if (tag instanceof ShortcutInfo) {
                ShortcutInfo info = tag;
                if (info.container != -101 && this.mWorkspace.getCurrentScreenType() != 2 && isMultiSelectEnabled() && !this.mWorkspace.inEditingModeAnimating()) {
                    DragSource dragSource;
                    setEditingState(10);
                    ShortcutsAdapter adapter = null;
                    if (isFolderShowing()) {
                        dragSource = getFolderCling().getFolder();
                        adapter = ((Folder) dragSource).getInfo().getAdapter(this);
                        adapter.disableSaveWhenDatasetChanged(true);
                    } else {
                        dragSource = this.mWorkspace;
                    }
                    this.mDragController.startAutoDrag(new View[]{v}, dragSource, this.mMultiSelectContainer, 0, 0);
                    if (info.container == -100) {
                        getWorkspace().getCurrentCellScreen().updateLayout();
                    }
                    if (adapter != null) {
                        adapter.disableSaveWhenDatasetChanged(false);
                    }
                } else if (!isInEditing()) {
                    if (info.isPresetApp()) {
                        installPresetApp(info);
                    } else if (info.mIconType == 3) {
                        switch (info.getToggleId()) {
                            case 2:
                            case 15:
                                this.mToggleManager.performToggle(info.getToggleId());
                                break;
                            default:
                                Intent intent = new Intent("com.miui.app.ExtraStatusBarManager.action_TRIGGER_TOGGLE");
                                intent.putExtra("com.miui.app.ExtraStatusBarManager.extra_TOGGLE_ID", info.getToggleId());
                                sendBroadcast(intent);
                                break;
                        }
                        AnalyticalDataCollector.clickToggle(getApplicationContext(), info);
                    } else if (!info.isLaunchDisabled()) {
                        if (info.progressStatus != -5) {
                            ProgressManager.getManager(this).onProgressIconClicked(info);
                        }
                        if (info.itemType != 11) {
                            this.mLaunchAppFromFolder = isShortcutIconInFolder(info);
                            this.mPerformLaunchAction.launch(info.intent, tag, v, this.mWorkspace.getHandler());
                            info.onLaunch(this);
                        }
                    } else if (BackupManager.getBackupManager(this).getState() == 1) {
                        Toast.makeText(this, R.string.app_being_backup_message, 200).show();
                    } else if (BackupManager.getBackupManager(this).getState() == 2) {
                        Toast.makeText(this, R.string.app_being_restored_message, 200).show();
                    }
                }
            } else if (tag instanceof FolderInfo) {
                openFolder((FolderInfo) tag, v);
            }
        }
    }

    public void onSlideVertically(ItemIcon icon) {
        ShortcutInfo tag = icon.getTag();
        if ((tag instanceof ShortcutInfo) && !isInEditing()) {
            ShortcutInfo info = tag;
            if (info.itemType == 0 && this.mModel.hasShortcutWidgetActivity(info.getPackageName())) {
                this.mShortcutWidgetLoader.startShortcutWidget(this, icon.getIcon(), info.getPackageName());
            }
        }
    }

    private boolean checkIntentPermissions(Intent intent) {
        if (!"android.intent.action.CALL".equals(intent.getAction()) || PermissionUtils.checkCallPhonePermission(this)) {
            return true;
        }
        PermissionUtils.requestCallPhonePermissions(this, 2);
        return false;
    }

    @SuppressLint({"NewApi"})
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2 && permissions.length == 1 && grantResults[0] == 0) {
            this.mPerformLaunchAction.run();
        } else {
            this.mPerformLaunchAction.reset();
        }
    }

    void startActivity(Intent intent, Object tag, View v) {
        try {
            if (tag instanceof ShortcutInfo) {
                ShortcutInfo info = (ShortcutInfo) tag;
                info.onLaunch();
                LauncherModel.updateItemFlagsInDatabaseDelayed(this, info);
                this.mApplicationsMessage.onLaunchApplication(intent.getComponent(), info.getUserId(this));
            }
            UserHandle user = (UserHandle) intent.getParcelableExtra("profile");
            Intent launchIntent = new Intent(intent);
            launchIntent.addFlags(268435456);
            tryToAddSourceBounds(launchIntent, v);
            if (user == null || user.equals(Process.myUserHandle())) {
                startActivity(launchIntent, getLaunchActivityOptions(v));
                return;
            }
            try {
                PortableUtils.startMainActivity(this, launchIntent.getComponent(), user, launchIntent.getSourceBounds(), getLaunchActivityOptions(v));
            } catch (ActivityNotFoundException e) {
                String pkgName = null;
                if (intent.getComponent() != null) {
                    pkgName = intent.getComponent().getPackageName();
                }
                if (!handleActivityNotFound(pkgName)) {
                    Toast.makeText(this, R.string.activity_not_found, 0).show();
                }
            } catch (SecurityException e2) {
                Toast.makeText(this, R.string.activity_not_found, 0).show();
                Log.e("Launcher", "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity " + "or use the exported attribute for this activity.", e2);
            }
        } catch (NullPointerException e3) {
            Toast.makeText(this, R.string.start_activity_failed, 0).show();
            Log.e("Launcher", "Launcher cannot start this activity(app2sd?)tag=" + tag + " intent=" + intent, e3);
        }
    }

    public static Bundle getLaunchActivityOptions(View v) {
        if (v == null) {
            return null;
        }
        return ActivityOptions.makeClipRevealAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle();
    }

    private void tryToAddSourceBounds(Intent intent, View v) {
        if (v != null) {
            if (v instanceof ItemIcon) {
                v = ((ItemIcon) v).getIcon();
            }
            v.getLocationOnScreen(this.mTmpPos);
            intent.setSourceBounds(new Rect(this.mTmpPos[0], this.mTmpPos[1], this.mTmpPos[0] + v.getWidth(), this.mTmpPos[1] + v.getHeight()));
        }
    }

    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        String pkgName;
        if (requestCode >= 0) {
            try {
                this.mWaitingForResult = true;
            } catch (ActivityNotFoundException e) {
                pkgName = null;
                if (intent.getComponent() != null) {
                    pkgName = intent.getComponent().getPackageName();
                }
                if (!handleActivityNotFound(pkgName)) {
                    Toast.makeText(this, R.string.activity_not_found, 0).show();
                }
            } catch (SecurityException e2) {
                Toast.makeText(this, R.string.activity_not_found, 0).show();
                Log.e("Launcher", "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity " + "or use the exported attribute for this activity.", e2);
            }
        }
        super.startActivityForResult(intent, requestCode, options);
        if (requestCode == 1002) {
            this.mWaitingForMarketDetail = true;
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    private boolean handleActivityNotFound(final String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(pkgName, 0);
            Intent mainIntent = new Intent("android.intent.action.MAIN");
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            mainIntent.setPackage(pkgName);
            List<ResolveInfo> mainActivities = pm.queryIntentActivities(mainIntent, 0);
            if (mainActivities.size() != 1) {
                return false;
            }
            mainIntent.setComponent(new ComponentName(((ResolveInfo) mainActivities.get(0)).activityInfo.packageName, ((ResolveInfo) mainActivities.get(0)).activityInfo.name));
            startActivity(mainIntent);
            return true;
        } catch (NameNotFoundException e) {
            new Builder(this).setTitle(R.string.guide_install_app_title).setMessage(R.string.guide_install_app_msg).setNegativeButton(R.string.cancel_btn_label, null).setPositiveButton(R.string.confirm_btn_label, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + pkgName));
                    intent.setClassName("com.xiaomi.market", "com.xiaomi.market.ui.AppDetailActivity");
                    try {
                        LauncherApplication.startActivity(Launcher.this.getApplicationContext(), intent, null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).create().show();
            return true;
        }
    }

    private void installPresetApp(ShortcutInfo info) {
        this.mInstallPresetAppDialog = ProgressDialog.show(this, "", getString(R.string.starting_preset_app));
        this.mInstallPresetAppDialog.setCancelable(false);
        getPackageManager().installPackage(info.intent.getData(), new Stub() {
            public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                if (returnCode != 1) {
                    Launcher.this.mInstallPresetAppDialog.dismiss();
                    Launcher.this.mInstallPresetAppDialog = null;
                }
            }
        }, 2, null);
    }

    public void openFolder(final FolderInfo folderInfo, View folderIcon) {
        this.mFolderClosingInNormalEdit = false;
        this.mWorkspace.post(new Runnable() {
            public void run() {
                if (!Launcher.this.mFolderCling.isOpened()) {
                    ShortcutIcon.setEnableLoadingAnim(true);
                    Launcher.this.mFolderCling.bind(folderInfo);
                    Launcher.this.mFolderCling.open();
                    Launcher.this.updateStatusBarClock();
                    Launcher.this.mFolderAnim.cancel();
                    if (Launcher.this.isInNormalEditing() || Launcher.this.isSceneShowing()) {
                        Launcher.this.mFolderAnim.setDuration((long) Folder.DEFAULT_FOLDER_BACKGROUND_SHORT_DURATION);
                    } else {
                        Launcher.this.mFolderAnim.setDuration((long) Folder.DEFAULT_FOLDER_OPEN_DURATION);
                    }
                    Launcher.this.mFolderAnim.setFloatValues(new float[]{0.0f, 1.0f});
                    Launcher.this.mFolderAnim.setInterpolator(new CubicEaseInOutInterpolater());
                    Launcher.this.mFolderAnim.start();
                    if (!Launcher.this.isInEditing()) {
                        folderInfo.onLaunch();
                        LauncherModel.updateItemFlagsInDatabaseDelayed(Launcher.this, folderInfo);
                    }
                }
            }
        });
    }

    boolean closeFolder() {
        return closeFolder(true);
    }

    boolean closeFolder(boolean allowAnimation) {
        if (!(isUninstallDialogShowing() || sChildrenModeEnabled || !this.mFolderCling.isOpened())) {
            ShortcutIcon.setEnableLoadingAnim(false);
            this.mFolderClosingInNormalEdit = isInNormalEditing();
            enableFolderInteractive(true);
            this.mFolderCling.close(allowAnimation);
            this.mFolderAnim.cancel();
            if (allowAnimation) {
                this.mFolderAnim.setDuration((long) Folder.DEFAULT_FOLDER_CLOSE_DURATION);
                this.mFolderAnim.setFloatValues(new float[]{1.0f, 0.0f});
                this.mFolderAnim.setInterpolator(new CubicEaseInOutInterpolater());
                this.mFolderAnim.start();
            } else {
                this.mScreenContent.setScaleX(1.0f);
                this.mScreenContent.setScaleY(1.0f);
                this.mScreenContent.setAlpha(1.0f);
                setScreenContentAlpha(1.0f);
            }
            updateStatusBarClock();
        }
        return false;
    }

    public void enableFolderInteractive(boolean enabled) {
        if (isFolderShowing()) {
            this.mFolderCling.enableInteractive(enabled);
        }
    }

    public boolean isFolderShowing() {
        return this.mFolderCling.isOpened();
    }

    public boolean isFolderAnimating() {
        return this.mIsFolderAnimating;
    }

    View getCurrentOpenedFolder() {
        if (isFolderShowing()) {
            return this.mFolderCling;
        }
        return null;
    }

    public boolean onLongClick(View v) {
        if (isWorkspaceLocked()) {
            return false;
        }
        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }
        CellInfo cellInfo = (CellInfo) v.getTag();
        if (cellInfo == null || this.mInAutoFilling) {
            return true;
        }
        if (this.mWorkspace.allowLongPress()) {
            if (cellInfo.cell != null) {
                if (Utilities.isScreenCellsLocked(this)) {
                    return false;
                }
                this.mWorkspace.performHapticFeedback(0, 1);
                this.mWorkspace.startDrag(cellInfo);
            } else if (!isInEditing()) {
                this.mWorkspace.performHapticFeedback(0, 1);
                AnalyticalDataCollector.trackEditMode("long_click");
                setEditingState(8);
            }
        }
        return true;
    }

    public void onItemStartDragged(View v) {
        if (((v.getTag() instanceof ShortcutInfo) || (v.getTag() instanceof FolderInfo)) && isInNormalEditing() && ((ItemInfo) v.getTag()).container != -101 && this.mWorkspace.getCurrentScreenType() != 2 && isMultiSelectEnabled() && !this.mWorkspace.inEditingModeAnimating()) {
            setEditingState(10);
        }
    }

    public void onItemEndDragged() {
        if (this.mEditingState == 10 && this.mMultiSelectContainer.isShowingTips()) {
            setEditingState(8);
        }
    }

    public void showPreview(boolean show, boolean withAnim) {
        if (!isWorkspaceLocked() && this.mIsPreviewShowing != show && !DeviceConfig.isLayoutRtl()) {
            if (!show) {
                this.mDragLayerBackground.setExitPreviewMode();
            } else if (!isPrivacyModeEnabled()) {
                this.mDragLayerBackground.setEnterPreviewMode();
            } else {
                return;
            }
            this.mHotSeats.setVisibility(show ? 4 : 0);
            if (withAnim) {
                this.mHotSeats.startAnimation(show ? this.mHotseatEditingExit : this.mHotseatEditingEnter);
            }
            this.mWorkspace.showPreview(show, withAnim);
            this.mIsPreviewShowing = show;
        }
    }

    public boolean isFreeStyleExists() {
        return this.mFreeStyleExists;
    }

    public boolean isPreviewShowing() {
        return this.mWorkspacePreview.isShowing();
    }

    public boolean isSceneShowing() {
        return getSceneScreenIfShowing() != null;
    }

    public SceneScreen getSceneScreen() {
        return this.mSceneScreenRef != null ? (SceneScreen) this.mSceneScreenRef.get() : null;
    }

    public SceneScreen getSceneScreenIfShowing() {
        SceneScreen sceneScreen;
        if (this.mSceneScreenRef != null) {
            sceneScreen = (SceneScreen) this.mSceneScreenRef.get();
        } else {
            sceneScreen = null;
        }
        if (sceneScreen == null || !sceneScreen.isShowing()) {
            return null;
        }
        return sceneScreen;
    }

    private void refreshChildrenFolder() {
        if (this.mChildrenFolderInfo != null) {
            loadChildrenAccessableApps();
            this.mChildrenFolderInfo.notifyDataSetChanged();
            showChildrenModeTips();
        }
    }

    public void showSceneScreen() {
        this.mDragLayer.removeCallbacks(this.mFreeStyleExitTimer);
        this.mSceneAnimating = true;
        SceneScreen sceneScreen = getSceneScreen();
        if (sceneScreen != null) {
            sceneScreen.reinit();
            showSceneScreenCore(sceneScreen);
        } else if (sChildrenModeEnabled) {
            this.mModel.loadFreeStyle();
        } else {
            showSceneScreenLoading();
        }
    }

    private void cacheSceneScreenRef(SceneScreen sceneScreen) {
        this.mSceneScreenRef = new CustomableReference(sceneScreen, SpecificDeviceConfig.isBigScreenLowMemory() ? 2 : 3);
    }

    private void showSceneScreenCore(SceneScreen sceneScreen) {
        AnalyticalDataCollector.enterFreeStyle(this, sceneScreen.getFreeStyle().getName());
        sEnteredSceneScreen = true;
        this.mScreen.addView(sceneScreen, 0, new LayoutParams(-1, -1));
        this.mDragController.addDropTarget(0, sceneScreen);
        this.mDragController.setDragScoller(sceneScreen.getDragScroller());
        if (sChildrenModeEnabled) {
            this.mScreenContent.setVisibility(4);
            this.mSceneAnimating = false;
            sceneScreen.notifyGadgets(4);
            return;
        }
        sceneScreen.onShowAnimationStart();
        sceneScreen.setTranslationY((float) (-this.mScreenContent.getHeight()));
        sceneScreen.post(new Runnable() {
            public void run() {
                Animator inAnimator = ObjectAnimator.ofFloat(Launcher.this.getSceneScreen(), "translationY", new float[]{0.0f});
                inAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        SceneScreen sceneScreen = Launcher.this.getSceneScreen();
                        Launcher.this.mScreenContent.setVisibility(4);
                        sceneScreen.onShowAnimationEnd();
                        Launcher.this.mSceneAnimating = false;
                        sceneScreen.notifyGadgets(4);
                        Launcher.this.showUpsideEnterOrExitTipIfNeed(false);
                    }
                });
                inAnimator.start();
                Launcher.this.goOutOldLayer();
            }
        });
    }

    public void showSceneScreenLoading() {
        this.mSceneScreenLoading = (ViewGroup) getLayoutInflater().inflate(R.layout.free_style_loading, this.mDragLayer, false);
        this.mDragLayer.addView(this.mSceneScreenLoading);
        this.mSceneScreenLoading.setTranslationY((float) (-this.mScreenContent.getHeight()));
        Animator inAnimator = ObjectAnimator.ofFloat(this.mSceneScreenLoading, "translationY", new float[]{0.0f});
        inAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                Launcher.this.mModel.loadFreeStyle();
            }
        });
        inAnimator.start();
        goOutOldLayer();
    }

    private void goOutOldLayer() {
        boolean isLoadingViewGoOut;
        View outView = this.mScreenContent;
        if (this.mSceneScreenLoading != null && this.mSceneScreenLoading.getTranslationY() == 0.0f) {
            outView = this.mSceneScreenLoading;
        }
        if (outView == this.mSceneScreenLoading) {
            isLoadingViewGoOut = true;
        } else {
            isLoadingViewGoOut = false;
        }
        Animator animator = ObjectAnimator.ofFloat(outView, "translationY", new float[]{(float) this.mScreenContent.getHeight()});
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (isLoadingViewGoOut) {
                    ((ViewGroup) Launcher.this.mSceneScreenLoading.getParent()).removeView(Launcher.this.mSceneScreenLoading);
                    Launcher.this.mSceneScreenLoading = null;
                }
            }
        });
        animator.start();
    }

    public void hideSceneScreen(boolean withAnim) {
        final SceneScreen sceneScreen = getSceneScreen();
        if (!sChildrenModeEnabled && sceneScreen != null) {
            AnalyticalDataCollector.exitFreeStyle(this);
            if (withAnim) {
                this.mSceneAnimating = true;
                this.mScreenContent.setVisibility(0);
                sceneScreen.onHideAnimationStart();
                Animator inAnimator = ObjectAnimator.ofFloat(sceneScreen, "translationY", new float[]{(float) (-sceneScreen.getHeight())});
                inAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        sceneScreen.onHideAnimationEnd();
                        Launcher.this.mSceneAnimating = false;
                        Launcher.this.removeSceneScreen(sceneScreen);
                    }
                });
                inAnimator.start();
                ObjectAnimator.ofFloat(this.mScreenContent, "translationY", new float[]{0.0f}).start();
                return;
            }
            this.mScreenContent.setVisibility(0);
            removeSceneScreen(sceneScreen);
        }
    }

    private void removeSceneScreen(SceneScreen sceneScreen) {
        sceneScreen.cleanUp();
        ((ViewGroup) sceneScreen.getParent()).removeView(sceneScreen);
        notifyGadgetStateChanged(4);
        this.mDragController.removeDropTarget(sceneScreen);
        this.mDragController.setDragScoller(this.mWorkspace);
    }

    public void exitChildrenMode() {
        startChildrenModeSettings();
    }

    public void forceHideErrorBar() {
        this.mErrorBar.forceToHide();
    }

    public void fadeInEditingTips(boolean withAnim) {
        this.mDeleteZone.fadeInEditingTips(withAnim);
    }

    public boolean isShowingEditingTips() {
        return this.mPredictedEditingState == 10 || this.mPredictedEditingState == 11 || this.mPredictedEditingState == 12 || this.mPredictedEditingState == 13;
    }

    public boolean isSceneAnimating() {
        return this.mSceneAnimating;
    }

    public boolean isInEditing() {
        return this.mEditingState != 7;
    }

    public boolean isMultiSelectEnabled() {
        return !Utilities.isScreenCellsLocked(this) && (this.mEditingState == 8 || this.mEditingState == 10);
    }

    public boolean isInNormalEditing() {
        return (this.mEditingState == 7 || this.mEditingState == 9) ? false : true;
    }

    public boolean isInMultiSelecting() {
        return this.mEditingState == 10;
    }

    public int getEditingState() {
        return this.mEditingState;
    }

    public long getHomeDataCreateTime() {
        return this.mHomeDataCreateTime;
    }

    public int getPredictedEditingState() {
        return this.mPredictedEditingState;
    }

    public boolean isPrivacyModeEnabled() {
        return false;
    }

    public static boolean isResumeWithUninstalling() {
        return sResumeWithUninstalling;
    }

    public static boolean isEditingModeExiting() {
        return sEditingModeExiting;
    }

    public void setEditingState(int state) {
        boolean z = true;
        if (state != this.mEditingState && !this.mWorkspace.inEditingModeAnimating() && !this.mIsPreviewShowing) {
            if (this.mEditingState != 7 || !isPrivacyModeEnabled()) {
                this.mPredictedEditingState = state;
                switch (state) {
                    case 7:
                        this.mFolderCling.updateLayout(false);
                        sEditingModeExiting = true;
                        sEditingModeExiting = switchThumbnailView(null, null);
                        if (sEditingModeExiting) {
                            this.mDeleteZone.showEditingTips(false);
                            showEditPanel(false, 9 == this.mEditingState);
                            if (isInNormalEditing()) {
                                this.mErrorBar.forceToHide();
                                showStatusBar(true);
                            } else if (!(this.mErrorBar.isShowing() || this.mDeleteZone.isUninstallDialogShowing())) {
                                showStatusBar(true);
                            }
                            if (!isSceneShowing()) {
                                exitTogglesSelectView(true);
                            }
                            notifyGadgetStateChanged(7);
                            Workspace workspace = this.mWorkspace;
                            if (this.mEditingState != 9) {
                                z = false;
                            }
                            workspace.setEditMode(state, z);
                            sEditingModeExiting = false;
                            break;
                        }
                        this.mPredictedEditingState = this.mEditingState;
                        return;
                    case 8:
                        AnalyticalDataCollector.trackEditMode("all");
                        this.mFolderCling.updateLayout(true);
                        if (this.mWorkspace.getCurrentScreenType() == 2) {
                            switchThumbnailView(this.mWidgetThumbnailView, this.mWidgetThumbnailViewAdapter);
                        } else {
                            switchThumbnailView(this.mEditingEntryView, null);
                        }
                        this.mDeleteZone.showEditingTips(false);
                        if (!isInNormalEditing()) {
                            showEditPanel(true, false);
                            this.mErrorBar.forceToHide();
                            showStatusBar(false);
                            notifyGadgetStateChanged(8);
                            this.mWorkspace.setEditMode(state, false);
                            this.mDragLayer.clearAllResizeFrames();
                            break;
                        }
                        break;
                    case 9:
                        showEditPanel(true, true);
                        this.mWorkspace.setEditMode(state, true);
                        break;
                    case 10:
                        if ($assertionsDisabled || this.mEditingState == 8) {
                            this.mDeleteZone.showEditingTips(true);
                            switchThumbnailView(this.mMultiSelectContainer, null);
                            break;
                        }
                        throw new AssertionError();
                    case 11:
                        this.mDeleteZone.showEditingTips(true);
                        setEditingTips(getString(R.string.editing_add_widget_tips));
                        switchThumbnailView(this.mWidgetThumbnailView, this.mWidgetThumbnailViewAdapter);
                        break;
                    case 12:
                        this.mDeleteZone.showEditingTips(true);
                        setEditingTips(getString(R.string.editing_set_wallpaper_tips));
                        PermissionUtils.requestAccessStoragePermissions(this);
                        switchThumbnailView(this.mWallpaperThumbnailView, this.mWallpaperThumbnailViewAdapter);
                        break;
                    case 13:
                        this.mDeleteZone.showEditingTips(true);
                        setEditingTips(getString(R.string.editing_set_transition_effect_tips));
                        switchThumbnailView(this.mTransEffectThumbnailView, null);
                        break;
                    case 14:
                        this.mDeleteZone.showEditingTips(true);
                        setEditingTips(getString(R.string.editing_set_screen_layout_cells));
                        switchThumbnailView(this.mScreenCellsThumbnailView, null);
                        break;
                }
                this.mEditingState = state;
            }
        }
    }

    private boolean switchThumbnailView(View nextView, ThumbnailViewAdapter adapter) {
        if (this.mCurrentThumbnailView != null) {
            if (this.mCurrentThumbnailView instanceof ScreenCellsThumbnailView) {
                this.mScreenCellsThumbnailView.confirmCellsSize();
            }
            if (this.mCurrentThumbnailView instanceof TransitionEffectThumbnailView) {
                int previousEffect = this.mWorkspace.getPreviousScreenTransitionType();
                int currentEffect = this.mWorkspace.getScreenTransitionType();
                if (previousEffect != currentEffect) {
                    AnalyticalDataCollector.trackTransitionEffectChanged(this.mTransEffectThumbnailView.getTransitionEffectName(currentEffect));
                }
                this.mWorkspace.setTransitionEffectEditingMode();
            } else if (this.mCurrentThumbnailView instanceof ThumbnailView) {
                ThumbnailViewAdapter thumbnailAdapter = ((ThumbnailView) this.mCurrentThumbnailView).mAdapter;
                if (thumbnailAdapter != null) {
                    thumbnailAdapter.stopLoading();
                }
            }
            if (this.mCurrentThumbnailView instanceof ScreenView) {
                ScreenView view = this.mCurrentThumbnailView;
                if (view.getScreenLayoutMode() == 5) {
                    ((ScreenView) this.mCurrentThumbnailView).foldingGroupMembers();
                }
                view.forceEndSlideBarHideAnim();
            }
            if (!(this.mCurrentThumbnailView instanceof MultiSelectContainerView)) {
                this.mCurrentThumbnailView.startAnimation(this.mThumbnailViewEditingExit);
                this.mCurrentThumbnailView.setVisibility(4);
            } else if (!((MultiSelectContainerView) this.mCurrentThumbnailView).hide()) {
                return false;
            } else {
                AnalyticalDataCollector.trackUsingMultiSelect();
            }
        }
        if (nextView != null) {
            if (nextView instanceof MultiSelectContainerView) {
                ((MultiSelectContainerView) nextView).show(true);
            } else {
                if (nextView instanceof ThumbnailView) {
                    ThumbnailView thumbnailView = (ThumbnailView) nextView;
                    if (adapter == this.mWidgetThumbnailViewAdapter) {
                        this.mWidgetThumbnailViewAdapter.setScreenType(this.mWorkspace.getCurrentScreenType());
                    }
                    if (thumbnailView.hasAdapter() || adapter == null) {
                        thumbnailView.reLoadThumbnails();
                    } else {
                        thumbnailView.setAdapter(adapter);
                    }
                }
                nextView.setVisibility(0);
                nextView.startAnimation(this.mThumbnailViewEditingEnter);
            }
        }
        this.mLastHideThumbnailView = this.mCurrentThumbnailView;
        this.mCurrentThumbnailView = nextView;
        return true;
    }

    public void setEditingTips(CharSequence tips) {
        this.mDeleteZone.setEditingTips(tips);
    }

    public void autoScrollWorkspace() {
        this.mWorkspace.autoShowTransitionEffectDemo();
    }

    public void appendWorkspaceTransitionType(int type) {
        this.mWorkspace.appendScreenTransitionType(type);
    }

    public void removeWorkspaceTransitionType(int type) {
        this.mWorkspace.removeScreenTransitionType(type);
    }

    public int getWorkspacePreviousTransitionType() {
        return this.mWorkspace.getPreviousScreenTransitionType();
    }

    public boolean isShowingTransitionEffectDemo() {
        return this.mWorkspace.isShowingTransitionEffectDemo();
    }

    public void showStatusBar(boolean show) {
        Window launcherWindow = getWindow();
        WindowManager.LayoutParams attrs = launcherWindow.getAttributes();
        attrs.flags = show ? attrs.flags & -1025 : attrs.flags | 1024;
        launcherWindow.setAttributes(attrs);
    }

    public void updateStatusBarClock() {
        StatusBarManager sbm = (StatusBarManager) getApplicationContext().getSystemService("statusbar");
        boolean disable = false;
        if (sbm != null) {
            if (!(this.mIsPause || isFolderShowing() || !this.mWorkspace.isScreenHasClockGadget(this.mWorkspace.getCurrentScreenId()))) {
                disable = true;
            }
            if (this.mMinusOneScreenView != null && this.mMinusOneScreenView.getCurrentScreenIndex() == 0) {
                disable = false;
            }
            sbm.disable(disable ? 8388608 : 0);
        }
    }

    public void changeStatusBarMode() {
        Window launcherWindow = getWindow();
        WindowManager.LayoutParams attrs = launcherWindow.getAttributes();
        attrs.extraFlags = WallpaperUtils.hasLightBgForStatusBar() ? attrs.extraFlags | 16 : attrs.extraFlags & -17;
        launcherWindow.setAttributes(attrs);
    }

    public void expandStatusBar() {
        StatusBarManager sbm = (StatusBarManager) getSystemService("statusbar");
        if (sbm != null) {
            try {
                Method expandMethod;
                if (VERSION.SDK_INT <= 16) {
                    expandMethod = sbm.getClass().getMethod("expand", new Class[0]);
                } else {
                    expandMethod = sbm.getClass().getMethod("expandNotificationsPanel", new Class[0]);
                }
                if (expandMethod != null) {
                    expandMethod.invoke(sbm, new Object[0]);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
    }

    private void showEditPanel(boolean show, boolean quickMode) {
        if (!quickMode) {
            if (show) {
                this.mDragLayerBackground.setEnterEditingMode();
            } else {
                this.mDragLayerBackground.setExitEditingMode();
            }
            this.mHotSeats.startAnimation(show ? this.mHotseatEditingExit : this.mHotseatEditingEnter);
            this.mHotSeats.setVisibility(show ? 4 : 0);
        }
    }

    Workspace getWorkspace() {
        return this.mWorkspace;
    }

    HotSeats getHotSeats() {
        return this.mHotSeats;
    }

    DeleteZone getDeleteZone() {
        return this.mDeleteZone;
    }

    public boolean updateWallpaperOffset(float xStep, float yStep, float xOffset, float yOffset) {
        return this.mDragLayer.updateWallpaperOffset(xStep, yStep, xOffset, yOffset);
    }

    public boolean updateWallpaperOffsetAnimate(float xStep, float yStep, float xOffset, float yOffset) {
        return this.mDragLayer.updateWallpaperOffsetAnimate(xStep, yStep, xOffset, yOffset);
    }

    public int getCurrentWorkspaceScreen() {
        if (this.mWorkspace != null) {
            return this.mWorkspace.getCurrentScreenIndex();
        }
        return -1;
    }

    private void clearForReload() {
        this.mDesktopItems.clear();
        this.mAllLoadedApps.clear();
        this.mNewInstalledApps.clear();
        mFolders.clear();
        this.mGadgets.clear();
        this.mMultiSelectContainer.clearAll();
        this.mHotSeats.removeAllScreens();
        this.mWorkspace.clearScreens();
    }

    private Bitmap getHomeSplash() {
        Bitmap bitmap = null;
        File file = new File(MIUI_HOME_SPLASH_PATH);
        if (file.exists()) {
            try {
                bitmap = BitmapFactory.decodeBitmap(this, Uri.fromFile(file), DeviceConfig.getDeviceWidth() * DeviceConfig.getDeviceHeight(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public void showLoadingDialog() {
        this.mWorkspaceLoading = true;
        Bitmap homeSplash = getHomeSplash();
        if (this.mLoadingDialog == null) {
            this.mLoadingDialog = new Dialog(this, R.style.loading_dialog);
            this.mLoadingDialog.setContentView(R.layout.loading_dialog);
        }
        this.mLoadingDialog.setCancelable(false);
        this.mLoadingDialog.getWindow().setLayout(-1, -1);
        this.mLoadingDialog.getWindow().setGravity(17);
        this.mLoadingDialog.getWindow().addExtraFlags(1);
        this.mLoadingDialog.getWindow().addFlags(260);
        this.mLoadingDialog.getWindow().clearFlags(2);
        ImageView splash = (ImageView) this.mLoadingDialog.findViewById(R.id.splash);
        ProgressBar progressBar = (ProgressBar) this.mLoadingDialog.findViewById(R.id.progress);
        if (homeSplash != null) {
            progressBar.setVisibility(8);
            splash.setVisibility(0);
            splash.setImageBitmap(homeSplash);
            this.mLoadingDialog.getWindow().setWindowAnimations(R.style.blur_panel_anim);
            splash.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!Launcher.this.mWorkspaceLoading) {
                        Launcher.this.dismissLoadingProgressDialog();
                    }
                }
            });
        } else {
            splash.setVisibility(8);
            progressBar.setVisibility(0);
            AnimatedRotateDrawable progress = (AnimatedRotateDrawable) progressBar.getIndeterminateDrawable();
            progress.setFramesCount(60);
            progress.setFramesDuration(20);
        }
        this.mLoadingDialog.show();
    }

    public void startLoading() {
        if (!this.mWorkspaceLoading) {
            showLoadingDialog();
        }
        clearForReload();
        this.mPosInvalidItems.clear();
        this.mWorkspace.loadScreens(true, true);
        this.mHotSeats.startLoading();
    }

    public boolean isReadyToBinding() {
        return (this.mWorkspace == null || this.mWorkspace.getCurrentScreenIndex() == -1) ? false : true;
    }

    public void startBinding() {
        notifyGadgetStateChanged(3);
        this.mGadgets.clear();
        Workspace workspace = this.mWorkspace;
        int count = workspace.getScreenCount();
        for (int i = 0; i < count; i++) {
            workspace.getCellLayout(i).removeAllViewsInLayout();
        }
    }

    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
        if (this.mWorkspace != null) {
            Workspace workspace = this.mWorkspace;
            for (int i = start; i < end; i++) {
                ItemInfo item = (ItemInfo) shortcuts.get(i);
                if (item.container == -100) {
                    this.mDesktopItems.add(item);
                }
                switch (item.itemType) {
                    case 0:
                    case 1:
                    case 2:
                    case 11:
                        if (item.container != -101) {
                            addItemToWorkspace(item, item.screenId, item.container, item.cellX, item.cellY, true, null);
                            break;
                        } else {
                            addItemToHotseats(item, item.cellX, true, null);
                            break;
                        }
                    default:
                        break;
                }
            }
            workspace.requestLayout();
        }
    }

    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        mFolders.clear();
        mFolders.putAll(folders);
        for (FolderInfo folderInfo : folders.values()) {
            Iterator i$ = folderInfo.contents.iterator();
            while (i$.hasNext()) {
                ShortcutInfo info = (ShortcutInfo) i$.next();
                LauncherModel.updateItemUserInDatabase(this, info);
                addToAppsList(info);
            }
        }
    }

    public void bindAppWidget(LauncherAppWidgetInfo item) {
        addItemToWorkspace(item, item.screenId, item.container, item.cellX, item.cellY, true, null);
    }

    public void addAppWidgetToWorkspace(LauncherAppWidgetInfo item) {
        long start = SystemClock.uptimeMillis();
        Log.d("Launcher", "bindAppWidget: " + item);
        Workspace workspace = this.mWorkspace;
        int appWidgetId = item.appWidgetId;
        AppWidgetProviderInfo appWidgetInfo = this.mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo == null) {
            Log.d("Launcher", "bindAppWidget: appWidgetId has not been bound to a provider yet,ignore it." + appWidgetId);
            return;
        }
        Log.d("Launcher", "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
        try {
            item.hostView = this.mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            if ("com.miui.notes".equals(appWidgetInfo.provider.getPackageName())) {
                sendBroadcast(new Intent("com.miui.notes.action.REFRESH_WIDGET"));
            }
            item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            item.hostView.setTag(item);
            workspace.addInScreen(item.hostView, item.screenId, item.cellX, item.cellY, item.spanX, item.spanY, false);
            workspace.requestLayout();
            this.mDesktopItems.add(item);
            Log.d("Launcher", "bound widget id=" + item.appWidgetId + " in " + (SystemClock.uptimeMillis() - start) + "ms");
        } catch (OutOfMemoryError e) {
            Log.d("Launcher", "bindAppWidget: out of memory,ignore it." + appWidgetId);
        } catch (RuntimeException e2) {
            Log.d("Launcher", "bindAppWidget: server side exception,ignore it." + appWidgetId);
        }
    }

    public void bindGadget(GadgetInfo item) {
        addItemToWorkspace(item, item.screenId, item.container, item.cellX, item.cellY, true, null);
    }

    public void finishBindingSavedItems() {
        if (this.mSavedState != null) {
            if (!this.mWorkspace.hasFocus()) {
                this.mWorkspace.getCurrentScreen().requestFocus();
            }
            this.mSavedState = null;
        }
        if (this.mSavedInstanceState != null) {
            super.onRestoreInstanceState(this.mSavedInstanceState);
            this.mSavedInstanceState = null;
        }
        this.mApplicationsMessage.requestUpdateMessages(true);
        if (this.mPosInvalidItems.size() > 0) {
            Iterator i$ = this.mPosInvalidItems.iterator();
            while (i$.hasNext()) {
                addItemToWorkspace((ItemInfo) i$.next(), -1, -100, 0, 0, null);
            }
        }
        this.mPosInvalidItems.clear();
    }

    public void finishBindingMissingItems() {
        checkNewInstalledAppsBeStarted();
    }

    public void finishLoading() {
        this.mWorkspaceLoading = false;
        this.mHomeDataCreateTime = PreferenceManager.getDefaultSharedPreferences(this).getLong("home_data_create_time_key", -1);
        sendBroadcast(new Intent("com.miui.home.intent.action.UPDATE_WALLPAPER_SURFACE"));
        this.mWorkspaceLoading = false;
        this.mWorkspace.onStart();
        this.mHotSeats.finishLoading();
        if (this.mLoadingDialog != null && ((ProgressBar) this.mLoadingDialog.findViewById(R.id.progress)).getVisibility() == 0) {
            dismissLoadingProgressDialog();
        }
        if (DeviceConfig.needShowMisplacedTips()) {
            new Builder(this).setCancelable(false).setIconAttribute(16843605).setMessage(getResources().getString(R.string.toast_screen_icons_misplaced)).setPositiveButton(R.string.confirm_btn_label, null).create().show();
        }
        ProgressManager.getManager(this).onLoadingFinished();
        this.mFreeStyleExists = new FreeStyleSerializer(getApplicationContext()).exists();
        if (this.mFreeStyleExists) {
            showUpsideEnterOrExitTipIfNeed(true);
            this.mDragLayer.postDelayed(this.mFreeStyleExitTimer, 20000);
        } else {
            this.mFreeStyleExitTimer.run();
        }
        AnalyticalDataCollector.registerAnalyticalAlarm(getApplicationContext());
        GadgetAutoChangeService.init(this);
        updateStatusBarClock();
    }

    private void dismissLoadingProgressDialog() {
        if (this.mLoadingDialog != null) {
            this.mLoadingDialog.dismiss();
            this.mLoadingDialog = null;
        }
    }

    public void bindAppsAdded(ArrayList<ShortcutInfo> apps, Intent intent) {
        Iterator it = apps.iterator();
        while (it.hasNext()) {
            ShortcutInfo info = (ShortcutInfo) it.next();
            boolean found = false;
            Iterator i$ = getAllLoadedApps().iterator();
            while (i$.hasNext()) {
                ShortcutInfo shortcutInfo = (ShortcutInfo) i$.next();
                if (TextUtils.equals(shortcutInfo.getPackageName(), info.getPackageName()) && info.itemType == shortcutInfo.itemType && info.mIconType == shortcutInfo.mIconType) {
                    if (!(info.getUser() == null || shortcutInfo.getUser() == null || !info.getUser().equals(shortcutInfo.getUser())) || (info.getUser() == null && shortcutInfo.getUser() == null)) {
                        if (shortcutInfo.intent == null && info.intent == null) {
                            found = true;
                        }
                        if (!(shortcutInfo.intent == null || info.intent == null || !shortcutInfo.intent.filterEquals(info.intent))) {
                            found = true;
                        }
                    }
                }
            }
            List<ComponentAndUser> installedApps = PortableUtils.launcherApps_getActivityList(getApplicationContext(), null, null);
            if (!(found || info.intent == null || !installedApps.contains(new ComponentAndUser(info.intent.getComponent(), info.getUser())))) {
                if (info.container == -101) {
                    addItemToHotseats(info, info.cellX, null);
                } else {
                    addItemToWorkspace(info, info.screenId, info.container, info.cellX, info.cellY, null);
                }
                Context context = getApplicationContext();
                if (intent != null && "android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
                    if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false) && Utilities.isXiaomiMarketInstalled(info.getPackageName(), context)) {
                        if (this.mIsPause) {
                            this.mInstalledAppToDelayNotificatoinList.add(info);
                        } else {
                            Utilities.doNotification(context, info.getTitle(context).toString(), context.getString(R.string.notification_installed), info);
                        }
                    }
                }
            }
        }
        final ArrayList<ShortcutInfo> arrayList = apps;
        this.mWorkspace.post(new Runnable() {
            public void run() {
                Iterator i$ = arrayList.iterator();
                while (i$.hasNext()) {
                    Launcher.this.removeRecommendAppsByPackageName(((ShortcutInfo) i$.next()).getPackageName());
                }
            }
        });
    }

    public void bindFreeStyleLoaded(FreeStyle freeStyle) {
        if (freeStyle == null) {
            goOutOldLayer();
            this.mScreenContent.setVisibility(0);
            this.mScreenContent.setTranslationY(0.0f);
            return;
        }
        SceneScreen sceneScreen = (SceneScreen) LayoutInflater.from(this).inflate(R.layout.free_style_scene_screen, null);
        sceneScreen.setLauncher(this);
        sceneScreen.setFreeStyle(freeStyle);
        sceneScreen.setDragController(this.mDragController);
        cacheSceneScreenRef(sceneScreen);
        showSceneScreenCore(sceneScreen);
    }

    private boolean showUpsideEnterOrExitTipIfNeed(boolean isEnter) {
        File freeStyleFile = new File(FreeStyleSerializer.DATA_PATH);
        if (!freeStyleFile.exists() || sChildrenModeEnabled) {
            return false;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long freeStyleModifiedTime = freeStyleFile.lastModified();
        if (freeStyleModifiedTime != prefs.getLong("pref_freestyle_last_modified_time", 0)) {
            prefs.edit().remove("pref_is_shown_upside_enter_tip").remove("pref_is_shown_upside_exit_tip").putLong("pref_freestyle_last_modified_time", freeStyleModifiedTime).apply();
        }
        String prefkey = isEnter ? "pref_is_shown_upside_enter_tip" : "pref_is_shown_upside_exit_tip";
        if (prefs.getBoolean(prefkey, false) || isInSnapshotMode()) {
            return false;
        }
        int imgResourceId = isEnter ? R.drawable.free_style_enter_tip : R.drawable.free_style_exit_tip;
        int strResourceId = isEnter ? R.string.free_style_enter_tip : R.string.free_style_exit_tip;
        final FrameLayout group = new FrameLayout(this);
        group.setBackgroundColor(-1442840576);
        ImageView imgView = new ImageView(this);
        imgView.setImageResource(imgResourceId);
        this.mDragLayer.addView(group, -1, -1);
        LayoutParams groupLayoutParams = new LayoutParams(-2, -2, 49);
        groupLayoutParams.topMargin = (int) (((float) (getResources().getDisplayMetrics().heightPixels - getResources().getDrawable(imgResourceId).getIntrinsicHeight())) * 0.7f);
        group.addView(imgView, groupLayoutParams);
        showUserGuideInner(0, strResourceId, imgView).setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                Launcher.this.mDragLayer.removeView(group);
            }
        });
        prefs.edit().putBoolean(prefkey, true).apply();
        return true;
    }

    private ArrowPopupWindow showUserGuideInner(int arrowMode, int resId, View anchor) {
        ArrowPopupWindow guideWindow = new ArrowPopupWindow(this);
        guideWindow.setArrowMode(arrowMode);
        TextView textView = new TextView(this);
        int padding = (int) ((getResources().getDisplayMetrics().density * 5.0f) + 0.5f);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextSize(22.0f);
        textView.setText(resId);
        guideWindow.setContentView(textView);
        guideWindow.setOutsideTouchable(true);
        guideWindow.show(anchor, 0, 0);
        return guideWindow;
    }

    private void prepairUserPersentAnimation() {
        if (this.mUserPersentAnimationPrepairedId == -1 && !isInEditing()) {
            CellLayout cl = this.mWorkspace.getCurrentCellLayout();
            if (cl != null) {
                for (int i = 0; i < cl.getChildCount(); i++) {
                    prepairUserPersentAnimation(cl.getChildAt(i));
                }
                if (cl.getScreenType() != 2) {
                    prepairUserPersentAnimation(this.mHotSeats);
                    prepairUserPersentAnimation(this.mWorkspace.getScreenIndicator());
                }
                this.mUserPersentAnimationPrepairedId = cl.getScreenId();
            }
        }
    }

    private void prepairUserPersentAnimation(View v) {
        if (v != null && !v.hasTransientState()) {
            v.setAlpha(0.0f);
        }
    }

    private void showUserPersentAnimation(boolean isCancel) {
        if (-1 != this.mUserPersentAnimationPrepairedId) {
            CellLayout cl = this.mWorkspace.getCellLayoutById(this.mUserPersentAnimationPrepairedId);
            for (int i = 0; i < cl.getChildCount(); i++) {
                performUserPersentAnimation(cl.getChildAt(i), isCancel);
            }
            if (cl.getScreenType() != 2) {
                performUserPersentAnimation(this.mWorkspace.getScreenIndicator(), isCancel);
                performUserPersentAnimation(this.mHotSeats, isCancel);
            }
            this.mUserPersentAnimationPrepairedId = -1;
        }
    }

    private static int calcDistance(float x, float y) {
        return (int) FloatMath.sqrt((x * x) + (y * y));
    }

    private void performUserPersentAnimation(View child, boolean isCancel) {
        if (isCancel) {
            child.animate().cancel();
            child.setAlpha(1.0f);
            child.invalidate();
            return;
        }
        if (this.mScreenDiagonalDistance == 0) {
            this.mScreenDiagonalDistance = calcDistance((float) DeviceConfig.getScreenWidth(), (float) DeviceConfig.getScreenHeight());
        }
        int centerX = DeviceConfig.getScreenWidth() / 2;
        int centerY = DeviceConfig.getScreenHeight() / 2;
        child.getLocationInWindow(this.mTmpLocation);
        int xDistanceToCenter = (this.mTmpLocation[0] + (child.getWidth() / 2)) - centerX;
        int yDistanceToCenter = (this.mTmpLocation[1] + (child.getHeight() / 2)) - centerY;
        int distanceToCenter = calcDistance((float) xDistanceToCenter, (float) yDistanceToCenter);
        int delayTime = (distanceToCenter * 600) / this.mScreenDiagonalDistance;
        float startScale = (((float) distanceToCenter) / ((float) this.mScreenDiagonalDistance)) + 0.6f;
        float startXOffset = ((float) (-xDistanceToCenter)) / (((float) delayTime) * 0.15f);
        float startYOffset = ((float) (-yDistanceToCenter)) / (((float) delayTime) * 0.15f);
        float oldTranslationX = child.getTranslationX();
        child.setTranslationX(oldTranslationX + startXOffset);
        child.setTranslationY(startYOffset);
        child.setScaleX(startScale);
        child.setScaleY(startScale);
        child.animate().setInterpolator(BackEaseOutInterpolater.sInstance).setStartDelay((long) Math.max(0, delayTime - 50)).setDuration(350).translationX(oldTranslationX).translationY(0.0f).scaleX(1.0f).scaleY(1.0f).alpha(1.0f).start();
    }

    public FolderInfo getParentFolderInfo(ShortcutInfo si) {
        if (si.container == -101 || si.container == -100) {
            return null;
        }
        return (FolderInfo) mFolders.get(Long.valueOf(si.container));
    }

    public FolderIcon getFolderIcon(FolderInfo fi) {
        if (fi == null) {
            return null;
        }
        if (fi.container == -100) {
            return (FolderIcon) this.mWorkspace.findViewWithTag(fi);
        }
        if (fi.container == -101) {
            return (FolderIcon) this.mHotSeats.getItemIcon(fi);
        }
        return null;
    }

    public FolderIcon getParentFolderIcon(ShortcutInfo si) {
        return getFolderIcon(getParentFolderInfo(si));
    }

    void fillEmpty(final ItemInfo deletedInfo) {
        if (Utilities.enableAutoFillEmpty(this)) {
            this.mWorkspace.post(new Runnable() {
                public void run() {
                    if (!Launcher.this.mDragController.isDragging()) {
                        Launcher.this.mInAutoFilling = true;
                        DeviceConfig.correntCellPositionRuntime(deletedInfo, false);
                        Launcher.this.mWorkspace.fillEmptyCellAuto(deletedInfo);
                        Launcher.this.mInAutoFilling = false;
                    }
                }
            });
        }
    }

    void addItem(ItemInfo info, boolean insert) {
        addItem(info, insert, false);
    }

    void addItem(ItemInfo info, boolean insert, boolean showInstallAnim) {
        if (info instanceof ShortcutInfo) {
            addToAppsList((ShortcutInfo) info);
        }
        if (info.container == -101) {
            this.mHotSeats.pushItem(info, info.cellX, showInstallAnim);
        } else if (info instanceof ShortcutInfo) {
            addShortcut((ShortcutInfo) info, insert, showInstallAnim);
        } else if (info instanceof FolderInfo) {
            this.mWorkspace.addInScreen(createItemIcon(this.mWorkspace.getCurrentCellLayout(), info), info.screenId, info.cellX, info.cellY, 1, 1, false, true, showInstallAnim);
        }
        if (this.mInstallPresetAppDialog != null && (info instanceof ShortcutInfo)) {
            this.mInstallPresetAppDialog.dismiss();
            this.mInstallPresetAppDialog = null;
            startActivity(((ShortcutInfo) info).intent, info, null);
        }
    }

    void addShortcut(ShortcutInfo info, boolean insert, boolean showInstallAnim) {
        if (getParentFolderIcon(info) != null) {
            FolderInfo fi = getParentFolderInfo(info);
            if (fi == null || !(fi instanceof FolderInfo)) {
                Log.e("Launcher", "Can't find user folder of id " + info.container);
                return;
            }
            fi.add(info);
            fi.notifyDataSetChanged();
            this.mApplicationsMessage.updateFolderMessage(fi);
            return;
        }
        this.mWorkspace.addInScreen(createItemIcon(this.mWorkspace.getCurrentCellLayout(), info), info.screenId, info.cellX, info.cellY, 1, 1, insert, true, showInstallAnim);
    }

    public void addItemToWorkspace(ItemInfo item, long screenId, long container, int cellX, int cellY, Runnable callback) {
        addItemToWorkspace(item, screenId, container, cellX, cellY, false, callback);
    }

    public void addItemToWorkspace(ItemInfo item, long screenId, long container, int cellX, int cellY, boolean addLaterIfInvalid, Runnable callback) {
        item.container = container;
        item.screenId = screenId;
        item.cellX = cellX;
        item.cellY = cellY;
        ItemInfo backup = item.clone();
        if (!this.mWorkspace.isPosValidate(item)) {
            if (addLaterIfInvalid) {
                this.mPosInvalidItems.add(item);
                return;
            }
            CellInfo cellInfo = this.mWorkspace.findEmptyCell(item);
            if (cellInfo == null) {
                LauncherModel.deleteItemFromDatabase(this, item);
                return;
            }
            item.cellX = cellInfo.cellX;
            item.cellY = cellInfo.cellY;
            item.screenId = cellInfo.screenId;
            item.container = -100;
            item.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
        }
        DeviceConfig.correntCellPositionRuntime(item, false);
        if (item.id == -1) {
            this.mModel.insertItemToDatabase(this, item);
        } else if (hasSamePosition(item, backup)) {
            LauncherModel.updateItemUserInDatabase(this, item);
        } else {
            LauncherModel.moveItemInDatabase(this, item, item.container, item.screenId, item.cellX, item.cellY);
        }
        if (item instanceof GadgetInfo) {
            addGadget((GadgetInfo) item, false);
        } else if (item instanceof LauncherAppWidgetInfo) {
            addAppWidgetToWorkspace((LauncherAppWidgetInfo) item);
        } else {
            addItem(item, false);
        }
        if (callback != null) {
            callback.run();
        }
    }

    public void addItemToHotseats(ItemInfo item, int cellX, Runnable callback) {
        addItemToHotseats(item, cellX, false, callback);
    }

    public void addItemToHotseats(ItemInfo item, int cellX, boolean addLaterIfInvalid, Runnable callback) {
        item.container = -101;
        item.screenId = -1;
        item.cellX = cellX;
        item.cellY = -1;
        if (this.mHotSeats.getScreenCount() <= DeviceConfig.getHotseatCount() && this.mHotSeats.acceptItem(item)) {
            DeviceConfig.correntCellPositionRuntime(item, false);
            if (item.id == -1) {
                this.mModel.insertItemToDatabase(this, item);
            }
            addItem(item, true);
            if (callback != null) {
                callback.run();
            }
        } else if (addLaterIfInvalid) {
            this.mPosInvalidItems.add(item);
        } else {
            addItemToWorkspace(item, -1, -100, 0, 0, callback);
        }
    }

    private ArrayList<ShortcutInfo> getShortcutInfoWithName(String name) {
        ArrayList<ShortcutInfo> result;
        synchronized (this.mAllLoadedApps) {
            ArrayList<ShortcutInfo> allApps = new ArrayList(this.mAllLoadedApps.values());
            result = new ArrayList();
            Iterator i$ = allApps.iterator();
            while (i$.hasNext()) {
                ShortcutInfo info = (ShortcutInfo) i$.next();
                if (info.itemType == 1 && info.getTitle(null).equals(name)) {
                    result.add(info);
                }
            }
        }
        return result;
    }

    private ArrayList<GadgetInfo> getGadgetList(int gadgetId) {
        ArrayList<GadgetInfo> results = new ArrayList();
        Iterator i$ = this.mGadgets.iterator();
        while (i$.hasNext()) {
            GadgetInfo info = (GadgetInfo) ((Gadget) i$.next()).getTag();
            if (info.getGadgetId() == gadgetId) {
                results.add(info);
            }
        }
        return results;
    }

    private void uninstallCleanButton(final Context context) {
        ContentResolver cr = context.getContentResolver();
        this.mWorkspace.post(new Runnable() {
            public void run() {
                ArrayList<GadgetInfo> removed = Launcher.this.getGadgetList(12);
                Launcher.this.bindGadgetsRemoved(removed);
                Iterator i$ = removed.iterator();
                while (i$.hasNext()) {
                    LauncherModel.deleteItemFromDatabase(context, (GadgetInfo) i$.next());
                }
            }
        });
    }

    private void uninstallPowerCleanButton(final Context context) {
        ContentResolver cr = context.getContentResolver();
        this.mWorkspace.post(new Runnable() {
            public void run() {
                ArrayList<GadgetInfo> removed = Launcher.this.getGadgetList(14);
                Launcher.this.bindGadgetsRemoved(removed);
                Iterator i$ = removed.iterator();
                while (i$.hasNext()) {
                    LauncherModel.deleteItemFromDatabase(context, (GadgetInfo) i$.next());
                }
            }
        });
    }

    void uninstallShortcut(final Context context, Intent data) {
        final String sender = data.getSender();
        Intent intent = (Intent) data.getParcelableExtra("android.intent.extra.shortcut.INTENT");
        if (intent != null) {
            if ("miui.intent.action.CREATE_QUICK_CLEANUP_SHORTCUT".equals(intent.getAction())) {
                uninstallCleanButton(context);
            } else if ("com.android.securitycenter.CREATE_DEEP_CLEAN_SHORTCUT".equals(intent.getAction())) {
                uninstallPowerCleanButton(context);
            } else {
                final String name = data.getStringExtra("android.intent.extra.shortcut.NAME");
                if (intent != null && name != null) {
                    this.mWorkspace.post(new Runnable() {
                        public void run() {
                            ArrayList<ShortcutInfo> shortcuts = Launcher.this.getShortcutInfoWithName(name);
                            ArrayList<ShortcutInfo> removed = new ArrayList();
                            Iterator i$ = shortcuts.iterator();
                            while (i$.hasNext()) {
                                ShortcutInfo info = (ShortcutInfo) i$.next();
                                if (!TextUtils.isEmpty(sender) && sender.equals(info.getIconPackage())) {
                                    LauncherModel.deleteItemFromDatabase(context, info);
                                    removed.add(info);
                                }
                            }
                            Launcher.this.bindShortcutsRemoved(removed);
                        }
                    });
                }
            }
        }
    }

    public ShortcutInfo getFirstAppInfo(String packageName, boolean matchAny) {
        synchronized (this.mAllLoadedApps) {
            ArrayList<ShortcutInfo> allApps = new ArrayList(this.mAllLoadedApps.values());
            if (allApps.isEmpty()) {
                return null;
            }
            int i = allApps.size() - 1;
            while (i >= 0) {
                ShortcutInfo info = (ShortcutInfo) allApps.get(i);
                String pn = info.getPackageName();
                if (pn == null || !((matchAny || info.itemType == 0) && pn.equals(packageName))) {
                    i--;
                } else {
                    return info;
                }
            }
            return null;
        }
    }

    public ShortcutInfo getShortcutInfo(ComponentName cn, int userId) {
        if (cn == null) {
            return null;
        }
        synchronized (this.mAllLoadedApps) {
            ArrayList<ShortcutInfo> allApps = new ArrayList(this.mAllLoadedApps.values());
            for (int i = allApps.size() - 1; i >= 0; i--) {
                ShortcutInfo info = (ShortcutInfo) allApps.get(i);
                if (info.getUserId(this) == userId && info.itemType == 0 && cn.getPackageName().equals(info.intent.getComponent().getPackageName())) {
                    if (TextUtils.isEmpty(cn.getClassName())) {
                        return info;
                    } else if (cn.equals(info.intent.getComponent())) {
                        return info;
                    }
                }
            }
            return null;
        }
    }

    public ArrayList<ShortcutInfo> getAllLoadedApps() {
        ArrayList<ShortcutInfo> arrayList;
        synchronized (this.mAllLoadedApps) {
            arrayList = new ArrayList(this.mAllLoadedApps.values());
        }
        return arrayList;
    }

    public ArrayList<Gadget> getAllGadgets() {
        return new ArrayList(this.mGadgets);
    }

    public ArrayList<FolderInfo> getAllFolders() {
        return new ArrayList(mFolders.values());
    }

    private void addToAppsList(ShortcutInfo info) {
        synchronized (this.mAllLoadedApps) {
            if (info.intent != null) {
                this.mAllLoadedApps.put(info.intent, info);
            }
            if (info.itemFlags == 4) {
                this.mNewInstalledApps.add(info);
            }
        }
    }

    public void removeFromAppsList(ShortcutInfo info, boolean cancelProgress) {
        synchronized (this.mAllLoadedApps) {
            this.mAllLoadedApps.remove(info.intent);
            removeRecommendAppsByPackageName(info.getPackageName());
            if (info.itemFlags == 4) {
                removeFromNewInstalledList(info);
            }
            if (!TextUtils.isEmpty(info.appProgressServer)) {
                if (cancelProgress) {
                    ProgressManager.getManager(this).onProgressIconDeleted(info);
                }
                ProgressManager.getManager(this).removeProgressingInfo(info.getPackageName());
            }
        }
    }

    public void removeFromNewInstalledList(ShortcutInfo info) {
        this.mNewInstalledApps.remove(info);
    }

    private void removeShortcutIcon(ShortcutInfo info, ArrayList<ShortcutInfo> appsAddedLater, boolean removeFromDB) {
        ViewParent parent = null;
        int removedIndex = -1;
        FolderInfo folderInfo = getParentFolderInfo(info);
        if (folderInfo != null) {
            folderInfo.remove(info.id);
        }
        ShortcutIcon icon = info.getBuddyIconView();
        if (icon != null) {
            parent = icon.getParent();
            if (parent instanceof IconContainer) {
                removedIndex = ((IconContainer) parent).removeShortcutIcon(icon);
            }
        }
        boolean z = (TextUtils.isEmpty(info.appProgressServer) || isInstalledByServer(info, info.appProgressServer)) ? false : true;
        removeFromAppsList(info, z);
        boolean addedLater = false;
        if (appsAddedLater != null) {
            int i = appsAddedLater.size() - 1;
            while (i >= 0) {
                ShortcutInfo added = (ShortcutInfo) appsAddedLater.get(i);
                if (added.id == info.id) {
                    if (ProgressManager.isProgressType(info) && !"com.miui.cloudbackup".equals(info.appProgressServer)) {
                        added.itemFlags = 4;
                        LauncherModel.updateItemInDatabase(this, added);
                    }
                    addedLater = true;
                    if (parent instanceof MultiSelectContainerView) {
                        this.mMultiSelectContainer.pushItem(createItemIcon(this.mMultiSelectContainer, added), 1, removedIndex);
                        addToAppsList(added);
                        appsAddedLater.remove(added);
                    }
                } else {
                    i--;
                }
            }
        }
        removeFromDB = !addedLater && removeFromDB;
        if (removeFromDB) {
            LauncherModel.deleteItemFromDatabase(this, info);
            fillEmpty(info);
        }
    }

    private boolean isInstalledByServer(ShortcutInfo info, String serverName) {
        boolean result = false;
        try {
            result = serverName.equals(getPackageManager().getInstallerPackageName(info.getPackageName()));
        } catch (Exception e) {
        }
        return result;
    }

    public static boolean hasSamePosition(ItemInfo source, ItemInfo target) {
        return source.container == target.container && source.screenId == target.screenId && source.cellX == target.cellX && source.cellY == target.cellY;
    }

    private boolean needRemoveFromDB(ShortcutInfo removedInfo, RemoveInfo ri) {
        boolean needRemove = true;
        if (removedInfo.isRetained || ri.dontKillApp) {
            needRemove = false;
        }
        String packageName = removedInfo.getPackageName();
        if (LauncherSettings.isRetainedPackage(packageName) || ScreenUtils.isPackageDisabled(this, packageName)) {
            return false;
        }
        return needRemove;
    }

    public void bindAppsRemoved(ArrayList<RemoveInfo> removedPackages, ArrayList<ShortcutInfo> appsAddedLater) {
        synchronized (this.mAllLoadedApps) {
            ArrayList<ShortcutInfo> allApps = new ArrayList(this.mAllLoadedApps.values());
            if (allApps.isEmpty()) {
                return;
            }
            int i;
            for (i = allApps.size() - 1; i >= 0; i--) {
                ShortcutInfo info = (ShortcutInfo) allApps.get(i);
                Iterator i$ = removedPackages.iterator();
                while (i$.hasNext()) {
                    RemoveInfo ri = (RemoveInfo) i$.next();
                    if (removeShortcutFromWorkspace(this, ri, info)) {
                        if (ScreenUtils.isPackageDisabled(this, ri.packageName) && ri.packageName.equals(BackupManager.getBackupManager(this).getCurrentRunningPackage())) {
                            info.mDisableByBackup = true;
                        } else {
                            removeShortcutIcon(info, appsAddedLater, needRemoveFromDB(info, ri));
                        }
                    }
                }
            }
            for (i = this.mDesktopItems.size() - 1; i >= 0; i--) {
                ItemInfo info2 = (ItemInfo) this.mDesktopItems.get(i);
                if (info2 instanceof LauncherAppWidgetInfo) {
                    LauncherAppWidgetInfo widgetInfo = (LauncherAppWidgetInfo) info2;
                    i$ = removedPackages.iterator();
                    while (i$.hasNext()) {
                        ri = (RemoveInfo) i$.next();
                        if (!ri.replacing && ri.packageName.equals(widgetInfo.hostView.getAppWidgetInfo().provider.getPackageName())) {
                            LauncherModel.deleteItemFromDatabase(this, widgetInfo);
                            this.mDesktopItems.remove(i);
                            ViewGroup parent = (ViewGroup) widgetInfo.hostView.getParent();
                            if (parent != null) {
                                parent.removeView(widgetInfo.hostView);
                            }
                        }
                    }
                }
            }
        }
    }

    public void bindAppsChanged(ArrayList<RemoveInfo> removedPackages, ArrayList<ShortcutInfo> appsAddedLater, Intent intent) {
        this.mDragController.cancelDrag();
        synchronized (this.mModel.getLocker()) {
            if (removedPackages != null) {
                if (removedPackages.size() > 0) {
                    bindAppsRemoved(removedPackages, appsAddedLater);
                }
            }
            bindAppsAdded(appsAddedLater, intent);
        }
    }

    public void bindIconsChanged(ArrayList<String> changedPackages) {
        if (changedPackages != null && changedPackages.size() != 0) {
            synchronized (this.mAllLoadedApps) {
                ArrayList<ShortcutInfo> allApps = new ArrayList(this.mAllLoadedApps.values());
                if (allApps.isEmpty()) {
                    return;
                }
                for (int i = allApps.size() - 1; i >= 0; i--) {
                    ShortcutInfo info = (ShortcutInfo) allApps.get(i);
                    if (changedPackages.contains(info.getPackageName()) && (info.itemType == 0 || info.itemType == 11)) {
                        info.setIcon(null);
                        ShortcutIcon icon = info.getBuddyIconView();
                        if (icon != null) {
                            icon.updateInfo(this, info);
                        }
                    }
                }
            }
        }
    }

    public void onProgressFinished(ShortcutInfo info) {
        if (ProgressManager.isProgressType(info) && !ScreenUtils.isAlreadyInstalled(info.getPackageName(), this)) {
            LauncherModel.deleteItemFromDatabase(this, info);
            info.appProgressServer = null;
            removeShortcutIcon(info, null, true);
        } else if (info.itemType != 11) {
            info.setIcon(null);
            info.getAppInfo().pkgName = "-1";
            info.progressStatus = -5;
            info.itemType = 0;
            FolderInfo folderInfo = getParentFolderInfo(info);
            if (folderInfo != null) {
                folderInfo.notifyDataSetChanged();
            } else if (info.getBuddyIconView() != null) {
                info.getBuddyIconView().updateInfo(this, info);
            }
            LauncherModel.updateItemInDatabase(this, info);
        }
    }

    public void bindShortcutsRemoved(ArrayList<ShortcutInfo> shortcuts) {
        this.mDragController.cancelDrag();
        Iterator i$ = shortcuts.iterator();
        while (i$.hasNext()) {
            ShortcutInfo info = (ShortcutInfo) i$.next();
            if (info.container > 0) {
                FolderInfo finfo = getParentFolderInfo(info);
                if (finfo != null) {
                    finfo.remove(info.id);
                }
            }
        }
        this.mWorkspace.removeShortcuts(shortcuts);
        this.mHotSeats.removeShortcuts(shortcuts);
        i$ = shortcuts.iterator();
        while (i$.hasNext()) {
            removeFromAppsList((ShortcutInfo) i$.next(), false);
        }
    }

    public void bindGadgetsRemoved(ArrayList<GadgetInfo> gadgets) {
        this.mDragController.cancelDrag();
        this.mWorkspace.removeGadgets(gadgets);
        Iterator i$ = gadgets.iterator();
        while (i$.hasNext()) {
            removeGadget((GadgetInfo) i$.next());
        }
    }

    public void updateFolderMessage(FolderInfo info) {
        this.mApplicationsMessage.updateFolderMessage(info);
    }

    public static void performLayoutNow(View rootView) {
        if (rootView != null) {
            rootView.measure(MeasureSpec.makeMeasureSpec(rootView.getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(rootView.getMeasuredHeight(), 1073741824));
            rootView.layout(0, 0, rootView.getMeasuredWidth(), rootView.getMeasuredHeight());
        }
    }

    public void alignCurrentScreen() {
        if (!isWorkspaceLocked() && !Utilities.isScreenCellsLocked(this)) {
            this.mWorkspace.getCurrentCellLayout().alignIconsToTop();
        }
    }

    private void openTmpFile() throws FileNotFoundException {
        this.mTmpFile = new File(getCacheDir().getAbsolutePath() + "/.tempfile");
        this.mTmpRAFile = new RandomAccessFile(this.mTmpFile, "rws");
    }

    public RandomAccessFile getTempFile() {
        return this.mTmpRAFile;
    }

    private void closeTmpFile() throws IOException {
        this.mTmpRAFile.close();
        if (this.mTmpFile != null) {
            this.mTmpFile.delete();
        }
    }

    public void dumpState() {
        Log.d("Launcher", "BEGIN launcher2 dump state for launcher " + this);
        Log.d("Launcher", "mSavedState=" + this.mSavedState);
        Log.d("Launcher", "mWorkspaceLoading=" + this.mWorkspaceLoading);
        Log.d("Launcher", "mWaitingForResult=" + this.mWaitingForResult);
        Log.d("Launcher", "mSavedInstanceState=" + this.mSavedInstanceState);
        Log.d("Launcher", "mDesktopItems.size=" + this.mDesktopItems.size());
        Log.d("Launcher", "mFolders.size=" + mFolders.size());
        this.mModel.dumpState();
        Log.d("Launcher", "END launcher2 dump state");
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        KeyguardManager km = (KeyguardManager) getSystemService("keyguard");
        if (hasFocus) {
            WallpaperUtils wallpaperUtils = this.mWallpaperUtils;
            if (!(!WallpaperUtils.isKeyguardShowLiveWallpaper() && km.isKeyguardSecure() && -1 == this.mUserPersentAnimationPrepairedId)) {
                showUserPersentAnimation(false);
            }
        }
        if (this.mForceTouchLayer.isShowing() && !hasFocus) {
            this.mForceTouchLayer.closeForceTouch();
        }
    }

    public ForegroundTaskQueue getForegroundTaskQueue() {
        return this.mForegroundTaskQueue;
    }

    public boolean isShortcutIconInFolder(ShortcutInfo info) {
        return info.container > 0;
    }

    public FolderInfo getFolderInfoById(long id) {
        return (FolderInfo) mFolders.get(Long.valueOf(id));
    }

    private void removeRecommendAppsByPackageName(String packageName) {
        for (FolderInfo folderInfo : mFolders.values()) {
            folderInfo.getRecommendInfo(this).removedRecommendAppsByPackageName(packageName);
        }
    }

    public boolean addRecommendAppToWorkspace(View v) {
        ShortcutInfo info = (ShortcutInfo) v.getTag();
        final ShortcutInfo existItem = getFirstAppInfo(info.getPackageName(), true);
        if (existItem != null) {
            Toast.makeText(this, R.string.app_already_exist, 500);
            if (!this.mFolderCling.isOpened()) {
                return false;
            }
            closeFolder();
            this.mDragLayer.postDelayed(new Runnable() {
                public void run() {
                    Launcher.this.locateAppInner(existItem);
                }
            }, (long) Folder.DEFAULT_FOLDER_CLOSE_DURATION);
            return false;
        }
        info.itemType = 11;
        info.mIconType = 4;
        info.progressTitle = getResources().getString(R.string.status_pending);
        info.progressStatus = -1;
        info.appProgressServer = Utilities.getMarketPackageName(this);
        this.mModel.insertItemToDatabase(getApplicationContext(), info);
        addToAppsList(info);
        DropTarget dropTarget = this.mFolderCling.getFolder().getContent();
        this.mDragController.startAutoDrag(new View[]{v}, this.mFolderCling.getRecommendScreen(), dropTarget, 0, 4);
        ProgressManager.getManager(this).bindAppProgressItem(info, false);
        return true;
    }

    public ForceTouchLayer getForceTouchLayer() {
        return this.mForceTouchLayer;
    }

    private void showInstalledAppToDelayNotification() {
        if (!this.mInstalledAppToDelayNotificatoinList.isEmpty()) {
            Context context = getApplicationContext();
            String content = context.getString(R.string.notification_installed);
            for (int i = 0; i < this.mInstalledAppToDelayNotificatoinList.size(); i++) {
                Utilities.doNotification(context, ((ShortcutInfo) this.mInstalledAppToDelayNotificatoinList.get(i)).getTitle(context).toString(), content, (ShortcutInfo) this.mInstalledAppToDelayNotificatoinList.get(i));
            }
            this.mInstalledAppToDelayNotificatoinList.clear();
        }
    }

    public void removeShortcutInfoFromDelayNotificatoinList(ShortcutInfo info) {
        this.mInstalledAppToDelayNotificatoinList.remove(info);
    }
}
