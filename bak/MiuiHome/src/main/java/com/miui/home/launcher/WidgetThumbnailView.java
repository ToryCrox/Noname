package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.gadget.GadgetInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WidgetThumbnailView extends ThumbnailView implements OnClickListener, OnLongClickListener, DragSource {
    private float mCameraDistanceCache;
    private Context mContext;
    private final int[] mCoordinatesTemp;
    private DragController mDragController;
    private Launcher mLauncher;
    private final Map<View, ArrayList<View>> mWidgetGroups;
    private final ArrayList<View> orderedViews;

    public WidgetThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.orderedViews = new ArrayList();
        this.mWidgetGroups = new HashMap();
        this.mCoordinatesTemp = new int[2];
        this.mCameraDistanceCache = 0.0f;
        this.mContext = context;
        initWidgetThumbnailView();
        setOnLongClickListener(this);
        setOnClickListener(this);
    }

    private void initWidgetThumbnailView() {
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

    protected void reorderAndAddAllViews(ArrayList<View> allViews) {
        if (allViews != null) {
            this.orderedViews.clear();
            for (int i = 0; i < allViews.size(); i++) {
                insertViewToGroups((View) allViews.get(i));
            }
            Iterator it = this.orderedViews.iterator();
            while (it.hasNext()) {
                View v = (View) it.next();
                addView(v);
                if (this.mWidgetGroups.containsKey(v)) {
                    Iterator i$ = ((ArrayList) this.mWidgetGroups.get(v)).iterator();
                    while (i$.hasNext()) {
                        addView((View) i$.next());
                    }
                }
            }
            this.orderedViews.clear();
        }
    }

    protected void reLoadThumbnails() {
        boolean startFromHead;
        int i = 0;
        foldingGroupMembers();
        int lastCurrentScreen = this.mCurrentScreen;
        if (getScreenCount() == 0) {
            startFromHead = true;
        } else {
            startFromHead = false;
        }
        super.reLoadThumbnails();
        if (this.mAdapter != null) {
            insertGroups(this.mWidgetGroups);
            showGroupStyle(this.mWidgetGroups);
            this.mWidgetGroups.clear();
            if (!startFromHead) {
                i = Math.min(lastCurrentScreen, getScreenCount() - 1);
            }
            setCurrentScreen(i);
        }
    }

    private void insertViewToGroups(View v) {
        View header = null;
        if (v.getTag() instanceof LauncherAppWidgetProviderInfo) {
            for (View key : this.mWidgetGroups.keySet()) {
                if (isSameWidgetCategory(v.getTag(), key.getTag())) {
                    header = key;
                    break;
                }
            }
        } else if (v.getTag() instanceof ShortcutPlaceholderProviderInfo) {
            ShortcutPlaceholderProviderInfo info = (ShortcutPlaceholderProviderInfo) v.getTag();
            for (View key2 : this.mWidgetGroups.keySet()) {
                if ((key2.getTag() instanceof ShortcutPlaceholderProviderInfo) && ((ShortcutPlaceholderProviderInfo) key2.getTag()).addType == info.addType) {
                    header = key2;
                    break;
                }
            }
        } else if (v.getTag() instanceof GadgetInfo) {
            for (View key22 : this.mWidgetGroups.keySet()) {
                if (isSameWidgetCategory(v.getTag(), key22.getTag())) {
                    header = key22;
                    break;
                }
            }
        }
        if (header != null) {
            ((ArrayList) this.mWidgetGroups.get(header)).add(v);
            return;
        }
        this.mWidgetGroups.put(v, new ArrayList());
        this.orderedViews.add(v);
    }

    private boolean isSameWidgetCategory(Object src, Object dst) {
        if (src instanceof GadgetInfo) {
            if (dst instanceof GadgetInfo) {
                if (((GadgetInfo) src).getCategoryId() == ((GadgetInfo) dst).getCategoryId()) {
                    return true;
                }
                return false;
            } else if (dst instanceof LauncherAppWidgetProviderInfo) {
                if (((GadgetInfo) src).getCategoryId() != ((LauncherAppWidgetProviderInfo) dst).mWidgetCategory) {
                    return false;
                }
                return true;
            }
        } else if (src instanceof LauncherAppWidgetProviderInfo) {
            if (dst instanceof GadgetInfo) {
                if (((LauncherAppWidgetProviderInfo) src).mWidgetCategory != ((GadgetInfo) dst).getCategoryId()) {
                    return false;
                }
                return true;
            } else if (dst instanceof LauncherAppWidgetProviderInfo) {
                return ((LauncherAppWidgetProviderInfo) dst).providerInfo.provider.getPackageName().equals(((LauncherAppWidgetProviderInfo) src).providerInfo.provider.getPackageName());
            }
        }
        return false;
    }

    private void showGroupStyle(Map<View, ArrayList<View>> groups) {
        for (View header : groups.keySet()) {
            ArrayList<View> members = (ArrayList) groups.get(header);
            header.setAlpha(1.0f);
            if (members.size() != 0) {
                onFoldGroup(header, members, false);
            }
        }
    }

    protected void onFoldGroup(View header, ArrayList<View> members, boolean showAnim) {
        TextView title = (TextView) header.findViewById(R.id.title);
        CharSequence titleName = "";
        if (header.getTag() instanceof LauncherAppWidgetProviderInfo) {
            titleName = ScreenUtils.getProviderName(this.mContext, ((LauncherAppWidgetProviderInfo) header.getTag()).providerInfo.provider.getPackageName());
        } else if (header.getTag() instanceof ShortcutPlaceholderProviderInfo) {
            ShortcutPlaceholderProviderInfo info = (ShortcutPlaceholderProviderInfo) header.getTag();
            if (info.addType == 4) {
                titleName = this.mContext.getResources().getString(R.string.toggle_title);
            } else if (info.addType == 5) {
                titleName = this.mContext.getResources().getString(R.string.settings_shortcut);
            }
        } else if (header.getTag() instanceof GadgetInfo) {
            titleName = ((GadgetInfo) header.getTag()).getCategoryTitle(this.mContext);
        }
        title.setText(titleName + "(" + (members.size() + 1) + ")");
        Iterator i$ = members.iterator();
        while (i$.hasNext()) {
            ((View) i$.next()).setAlpha(0.0f);
        }
        super.onFoldGroup(header, members, showAnim);
    }

    protected void onUnfoldGroup(View header, ArrayList<View> members, boolean showAnim) {
        TextView title = (TextView) header.findViewById(R.id.title);
        if (header.getTag() instanceof LauncherAppWidgetProviderInfo) {
            title.setText(((LauncherAppWidgetProviderInfo) header.getTag()).providerInfo.label);
        } else if (header.getTag() instanceof ShortcutPlaceholderProviderInfo) {
            title.setText(((ShortcutPlaceholderProviderInfo) header.getTag()).getTitle(this.mContext));
        } else if (header.getTag() instanceof GadgetInfo) {
            title.setText(((GadgetInfo) header.getTag()).getTitle(this.mContext));
        }
        super.onUnfoldGroup(header, members, showAnim);
    }

    public void onClick(View v) {
        if (isShown() && !this.mLauncher.isPrivacyModeEnabled()) {
            if (this.mLauncher.isFolderShowing()) {
                this.mLauncher.closeFolder();
            }
            if (isGroupHeader(v)) {
                unfoldingGroupMembers(v, false);
            } else if (((AutoLayoutThumbnailItem) v).icon.getDrawable() != null) {
                ItemInfo info = ((ItemInfo) v.getTag()).clone();
                try {
                    this.mDragController.startAutoDrag(new View[]{v}, this, LauncherApplication.getLauncher(getContext()).getWorkspace(), 2, 0);
                } catch (Exception e) {
                    Log.e("WidgetThumbnailView", "Adding widget fail", e);
                }
                v.setTag(info);
            }
        }
    }

    public boolean onLongClick(View v) {
        if (!isShown() || this.mLauncher.isPrivacyModeEnabled() || this.mDragController.isDragging()) {
            return false;
        }
        if (this.mLauncher.isInNormalEditing()) {
            this.mLauncher.closeFolder();
        }
        if (isGroupHeader(v)) {
            return false;
        }
        if (((AutoLayoutThumbnailItem) v).icon.getDrawable() == null) {
            return false;
        }
        ItemInfo dragInfo = v.getTag();
        if (dragInfo instanceof ItemInfo) {
            ItemInfo info = dragInfo;
            Drawable preview = null;
            if (dragInfo instanceof LauncherAppWidgetProviderInfo) {
                LauncherAppWidgetProviderInfo widgetInfo = (LauncherAppWidgetProviderInfo) dragInfo;
                if (widgetInfo.providerInfo.previewImage != 0) {
                    preview = this.mContext.getPackageManager().getDrawable(widgetInfo.providerInfo.provider.getPackageName(), widgetInfo.providerInfo.previewImage, null);
                    if (preview == null) {
                        Log.w("WidgetThumbnailView", "Can't load icon drawable 0x" + Integer.toHexString(widgetInfo.providerInfo.icon) + " for provider: " + widgetInfo.providerInfo.provider);
                        return false;
                    }
                }
            } else if (dragInfo instanceof GadgetInfo) {
                preview = ((GadgetInfo) dragInfo).getPreviewImage(this.mContext);
            }
            if (preview != null) {
                v.getLocationOnScreen(this.mCoordinatesTemp);
                Rect oldBounds = preview.getBounds();
                int widgetWidth = info.spanX * getChildScreenMeasureWidth();
                int widgetHeight = (preview.getIntrinsicHeight() * widgetWidth) / preview.getIntrinsicWidth();
                int left = (this.mCoordinatesTemp[0] + (v.getMeasuredWidth() / 2)) - (widgetWidth / 2);
                int top = (this.mCoordinatesTemp[1] + (v.getMeasuredHeight() / 2)) - (widgetHeight / 2);
                preview.setBounds(0, 0, widgetWidth, widgetHeight);
                v.setPressed(false);
                this.mDragController.startDrag(preview, info.clone(), left, top, 0.0f, (DragSource) this, 2);
                preview.setBounds(oldBounds);
            } else {
                this.mDragController.startDrag(v, false, this, 2);
                v.setTag(info.clone());
            }
        }
        return true;
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
    }

    public void setDragController(DragController dragger) {
        this.mDragController = dragger;
    }

    public void setCameraDistance(float distance) {
        if (distance != this.mCameraDistanceCache) {
            this.mCameraDistanceCache = distance;
            super.setCameraDistance(this.mCameraDistanceCache);
        }
    }

    public void onDropBack(DragObject d) {
    }

    protected void onPinchIn(ScaleGestureDetector detector) {
        if (hasGroupUnfolding()) {
            finishCurrentGesture();
            foldingGroupMembers();
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
