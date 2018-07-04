package com.miui.home.launcher;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.miui.home.R;
import com.miui.home.launcher.common.AsyncTaskExecutorHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WallpaperThumbnailView extends ThumbnailView implements OnClickListener, OnLongClickListener, DragSource {
    private float mCameraDistanceCache;
    private final Map<View, ArrayList<View>> mCustomGroups;
    private DragController mDragController;
    private int mDraggedUpPos;
    private View mDraggingView;
    private long mLastClickTime;
    private Launcher mLauncher;
    private TextView mOriginalSelected;
    private boolean mPickingWallpaperByPicker;
    private String mSelectedWallpaperPath;
    private Intent mStartedIntent;

    public WallpaperThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WallpaperThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDraggedUpPos = -1;
        this.mOriginalSelected = null;
        this.mSelectedWallpaperPath = null;
        this.mStartedIntent = null;
        this.mPickingWallpaperByPicker = false;
        this.mCustomGroups = new HashMap();
        this.mCameraDistanceCache = 0.0f;
        initWallpaperThumbnailView();
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public void setAdapter(ThumbnailViewAdapter adapter) {
        if (adapter == null && this.mAdapter != null) {
            String backupPath = ((WallpaperThumbnailViewAdapter) this.mAdapter).getBackupPath();
            if (backupPath != null) {
                File currentWallpaper = new File(backupPath);
                if (currentWallpaper.exists()) {
                    currentWallpaper.delete();
                }
            }
        }
        removeAllScreens();
        super.setAdapter(adapter);
    }

    private void initWallpaperThumbnailView() {
        setScrollWholeScreen(true);
        setPushGestureEnabled(true);
        setScreenTransitionType(10);
        setScreenLayoutMode(5);
        setLayoutScreenSeamless(true);
        setEnableReverseDrawingMode(true);
        LayoutParams params = new LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.slide_bar_height));
        params.width = -1;
        params.gravity = 80;
        setSlideBarPosition(params, R.drawable.editing_mode_slidebar_fg, 0, true);
        if (this.mSlideBar != null) {
            this.mSlideBar.setOnTouchListener(null);
        }
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    protected void setCurrentScreenInner(int screenIndex) {
        int releaseScreen = -1;
        if (this.mCurrentScreen != -1) {
            if (this.mCurrentScreen > screenIndex) {
                releaseScreen = this.mCurrentScreen + this.mVisibleRange;
                AsyncTaskExecutorHelper.clearExcutorQueue();
            } else if (this.mCurrentScreen < screenIndex) {
                releaseScreen = this.mCurrentScreen - this.mVisibleRange;
                AsyncTaskExecutorHelper.clearExcutorQueue();
            }
        }
        int screenLeft = screenIndex - this.mVisibleRange;
        int screenRight = screenIndex + this.mVisibleRange;
        int iterateDirection = DeviceConfig.getIterateDirection(false);
        if (screenIndex >= 0 && screenIndex < getScreenCount()) {
            preloadThumbnailContent(screenIndex, iterateDirection);
        }
        if (screenLeft >= 0) {
            preloadThumbnailContent(screenLeft, iterateDirection);
        }
        if (screenRight < getScreenCount()) {
            preloadThumbnailContent(screenRight, iterateDirection);
        }
        if (releaseScreen >= 0 && releaseScreen < getScreenCount()) {
            releaseThumbnailContent(releaseScreen, iterateDirection);
        }
        super.setCurrentScreenInner(screenIndex);
    }

    private void releaseThumbnailContent(int screenStartIndex, int direction) {
        WallpaperThumbnailViewAdapter thumbnailAdapter = this.mAdapter;
        int i = screenStartIndex;
        while (true) {
            if (direction == -1) {
                if (i <= screenStartIndex - this.mVisibleRange) {
                    return;
                }
            } else if (i >= this.mVisibleRange + screenStartIndex) {
                return;
            }
            thumbnailAdapter.releaseThumbnailContent(getScreen(i));
            i += direction;
        }
    }

    private void preloadThumbnailContent(int screenStartIndex, int direction) {
        WallpaperThumbnailViewAdapter thumbnailAdapter = this.mAdapter;
        int i = screenStartIndex;
        while (true) {
            if (direction == -1) {
                if (i <= screenStartIndex - this.mVisibleRange) {
                    return;
                }
            } else if (i >= (this.mVisibleRange + screenStartIndex) + 1) {
                return;
            }
            thumbnailAdapter.loadThumbnailContent(getScreen(i));
            i += direction;
        }
    }

    private void insertViewToGroups(View v) {
        if (v.getTag() == null) {
            this.mCustomGroups.put(v, new ArrayList());
        } else if ((v.getTag() instanceof Intent) && this.mCustomGroups.keySet().size() > 0) {
            ((ArrayList) this.mCustomGroups.get((View) this.mCustomGroups.keySet().iterator().next())).add(v);
        }
    }

    protected void reorderAndAddAllViews(ArrayList<View> allViews) {
        if (allViews != null) {
            for (int i = 0; i < allViews.size(); i++) {
                View v = (View) allViews.get(i);
                addView(v);
                TextView selected = (TextView) v.findViewById(R.id.title);
                selected.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                if (i == this.mAdapter.getSelectedIndex()) {
                    setCurrentSelected(selected);
                    this.mOriginalSelected = this.mCurrentSelected;
                    if (v.getTag() instanceof String) {
                        this.mSelectedWallpaperPath = (String) v.getTag();
                    }
                }
                if (i >= ((WallpaperThumbnailViewAdapter) this.mAdapter).getCustomHeaderIndex() && (v.getTag() == null || (v.getTag() instanceof Intent))) {
                    insertViewToGroups(v);
                }
            }
        }
    }

    public void refreshWallpaperThumbnail() {
        int i = 0;
        if (this.mPickingWallpaperByPicker && this.mStartedIntent != null) {
            Intent onlinePickIntent = WallpaperUtils.getThemeManagerWallpaperPickerIntent(this.mLauncher, null, true);
            if (onlinePickIntent == null || !onlinePickIntent.getComponent().equals(this.mStartedIntent.getComponent())) {
                return;
            }
        }
        WallpaperThumbnailViewAdapter adapter = this.mAdapter;
        adapter.refreshList();
        ArrayList<String> wallpaperUriList = new ArrayList();
        WallpaperUtils.loadLocalWallpaperList(wallpaperUriList);
        for (int i2 = getScreenCount() - 1; i2 >= 0; i2--) {
            View child = getChildAt(i2);
            String wallpaperPath = getWallpaperPath(child);
            if (!TextUtils.isEmpty(wallpaperPath)) {
                if (wallpaperUriList.contains(wallpaperPath)) {
                    wallpaperUriList.remove(wallpaperPath);
                } else if (!wallpaperPath.equals(adapter.getBackupPath())) {
                    removeView(child);
                    clearThumbnail(child);
                }
            }
        }
        if (wallpaperUriList.size() > 0) {
            int i3;
            if (adapter.isOnlineWallpaperExist()) {
                i3 = 1;
            } else {
                i3 = 0;
            }
            int index = -1 + i3;
            if (!TextUtils.isEmpty(adapter.getBackupPath())) {
                i = 1;
            }
            index += i;
            Iterator i$ = wallpaperUriList.iterator();
            while (i$.hasNext()) {
                View v = adapter.getThumbnailView((String) i$.next());
                v.setOnClickListener(this.mClickListener);
                v.setOnLongClickListener(this.mLongClickListener);
                addView(v, index + 1);
            }
        }
        correctCurrentSelected();
    }

    protected void reLoadThumbnails() {
        this.mPickingWallpaperByPicker = false;
        this.mCurrentSelected = null;
        super.reLoadThumbnails();
        if (this.mAdapter != null) {
            setCurrentScreen(0);
            insertGroups(this.mCustomGroups);
            hideGroupMember(this.mCustomGroups);
            this.mCustomGroups.clear();
        }
    }

    private void hideGroupMember(Map<View, ArrayList<View>> groups) {
        for (View header : groups.keySet()) {
            ArrayList<View> members = (ArrayList) groups.get(header);
            if (members.size() != 0) {
                onFoldGroup(header, members, true);
            }
        }
    }

    protected void onFoldGroup(View header, ArrayList<View> members, boolean showAnim) {
        header.setAlpha(1.0f);
        Iterator i$ = members.iterator();
        while (i$.hasNext()) {
            ((View) i$.next()).setAlpha(0.0f);
        }
        super.onFoldGroup(header, members, showAnim);
    }

    public void onClick(View v) {
        if (isShown() && !this.mLauncher.isPrivacyModeEnabled() && !disableClickAction(v)) {
            if (this.mLauncher.isFolderShowing()) {
                this.mLauncher.closeFolder();
            }
            this.mLastClickTime = System.currentTimeMillis();
            if (v.getTag() instanceof Intent) {
                this.mPickingWallpaperByPicker = true;
                this.mStartedIntent = (Intent) v.getTag();
                this.mLauncher.startWallpaper((Intent) v.getTag());
            } else if (v.getTag() instanceof WallpaperInfo) {
                this.mPickingWallpaperByPicker = false;
                selected = (TextView) v.findViewById(R.id.title);
                WallpaperUtils.setLiveWallpaper(((WallpaperInfo) v.getTag()).getComponent(), v.getWindowToken());
                setCurrentSelected(selected);
                this.mSelectedWallpaperPath = null;
                AnalyticalDataCollector.trackWallpaperChanged("home");
            } else if (!TextUtils.isEmpty(getWallpaperPath(v))) {
                this.mPickingWallpaperByPicker = false;
                String wallpaperPath = getWallpaperPath(v);
                if ("".equals(wallpaperPath)) {
                    Toast.makeText(this.mContext, R.string.bad_wallpaper_source_prompt, 200).show();
                    return;
                }
                selected = (TextView) v.findViewById(R.id.title);
                setWallpaper(wallpaperPath);
                setCurrentSelected(selected);
                this.mSelectedWallpaperPath = wallpaperPath;
                AnalyticalDataCollector.setWallpaperEntryType(this.mContext, "editing_mode");
                AnalyticalDataCollector.trackWallpaperChanged("home");
            } else if (v.getTag() == null) {
                unfoldingGroupMembers(v, true);
            }
        }
    }

    private String getWallpaperPath(View v) {
        if (v.getTag() instanceof WallpaperThumbnailInfo) {
            return ((WallpaperThumbnailInfo) v.getTag()).getWallpaperPath();
        }
        return null;
    }

    private boolean canBeDrag(View v) {
        if (isGroupHeader(v)) {
            return false;
        }
        String wallpaperPath = getWallpaperPath(v);
        if (!TextUtils.isEmpty(wallpaperPath)) {
            String backupPath = ((WallpaperThumbnailViewAdapter) this.mAdapter).getBackupPath();
            if (backupPath == null || !backupPath.equals(wallpaperPath)) {
                return true;
            }
        }
        return false;
    }

    public boolean onLongClick(View v) {
        if (!isShown() || this.mLauncher.isPrivacyModeEnabled() || this.mDragController.isDragging()) {
            return false;
        }
        if (canBeDrag(v)) {
            this.mDraggedUpPos = getChildIndex(v);
            if (this.mDragController.startDrag(v, true, this, 0)) {
                this.mDraggingView = v;
                return true;
            }
        }
        this.mDraggedUpPos = -1;
        this.mDraggingView = null;
        return false;
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
        if (d.isAllDropedSuccess()) {
            clearThumbnail(this.mDraggingView);
            this.mDraggingView = null;
            this.mDraggedUpPos = -1;
        }
    }

    public void onDropBack(DragObject d) {
        addView(this.mDraggingView, this.mDraggedUpPos);
        this.mDraggingView = null;
        this.mDraggedUpPos = -1;
    }

    public void setDragController(DragController dragger) {
        this.mDragController = dragger;
    }

    private void setWallpaper(String imagePath) {
        WallpaperUtils.setWallpaper(imagePath, Uri.fromFile(new File(imagePath)).toString(), true);
    }

    private boolean disableClickAction(View v) {
        TextView selected = (TextView) v.findViewById(R.id.title);
        if (selected != null && this.mCurrentSelected == selected) {
            return true;
        }
        if (!(v.getTag() instanceof WallpaperInfo) && !(v.getTag() instanceof WallpaperThumbnailInfo)) {
            return false;
        }
        if (System.currentTimeMillis() - this.mLastClickTime >= 1000) {
            return false;
        }
        return true;
    }

    public void onWallpaperChanged() {
        if (this.mPickingWallpaperByPicker) {
            correctCurrentSelected();
        }
    }

    private void correctCurrentSelected() {
        int i;
        View v;
        String currentWallpaper = WallpaperUtils.getWallpaperSourcePath("pref_key_current_wallpaper_path");
        setCurrentSelected(null);
        if (!TextUtils.isEmpty(currentWallpaper)) {
            int childCount = getChildCount();
            for (i = 0; i < childCount; i++) {
                v = getChildAt(i);
                if (currentWallpaper.equals(getWallpaperPath(v))) {
                    setCurrentSelected((TextView) v.findViewById(R.id.title));
                    return;
                }
            }
        }
        WallpaperManager wpm = (WallpaperManager) this.mLauncher.getSystemService("wallpaper");
        if (wpm.getWallpaperInfo() != null) {
            ComponentName cn = wpm.getWallpaperInfo().getComponent();
            for (i = 0; i < getChildCount(); i++) {
                v = getChildAt(i);
                if ((v.getTag() instanceof WallpaperInfo) && ((WallpaperInfo) v.getTag()).getComponent().equals(cn)) {
                    setCurrentSelected((TextView) v.findViewById(R.id.title));
                    return;
                }
            }
        }
    }

    public void setCameraDistance(float distance) {
        if (distance != this.mCameraDistanceCache) {
            this.mCameraDistanceCache = distance;
            super.setCameraDistance(this.mCameraDistanceCache);
        }
    }

    protected void onPinchIn(ScaleGestureDetector detector) {
        if (hasGroupUnfolding()) {
            finishCurrentGesture();
            foldingGroupMembers();
            this.mPickingWallpaperByPicker = false;
        }
        super.onPinchIn(detector);
    }

    protected void onPushGesture(int direction) {
        this.mGestureTrigged = true;
        int scrollStartX = getScrollStartX();
        if (!hasGroupUnfolding()) {
            return;
        }
        if (scrollStartX > this.mScrollRightBound || scrollStartX < this.mScrollLeftBound) {
            onPinchIn(null);
        }
    }
}
