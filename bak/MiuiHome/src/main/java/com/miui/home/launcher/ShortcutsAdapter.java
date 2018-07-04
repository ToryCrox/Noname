package com.miui.home.launcher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.miui.home.R;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ShortcutsAdapter extends ArrayAdapter<ShortcutInfo> {
    private static PositionComparator PC = new PositionComparator();
    private boolean mDisableSaveWhenDatasetChanged = false;
    HashSet<ShortcutInfo> mDragOverItems = new HashSet();
    HashMap<ShortcutInfo, DragView> mDroppingDragViews = new HashMap();
    ShortcutInfo mFirstDragItem;
    private ShortcutInfo mForceTouchSelectedShortcutInfo;
    private boolean mForceTouchStarted = false;
    private FolderInfo mInfo;
    private final Launcher mLauncher;
    private Object[] mPositionMap;

    public static class PositionComparator implements Comparator<ShortcutInfo> {
        public int compare(ShortcutInfo s1, ShortcutInfo s2) {
            return s1.cellX <= s2.cellX ? -1 : 1;
        }
    }

    public ShortcutsAdapter(Context context, FolderInfo info) {
        super(context, 0, info.contents);
        this.mInfo = info;
        this.mLauncher = Application.getLauncherApplication(context).getLauncher();
        buildSortingMap();
    }

    public FolderInfo getFolderInfo() {
        return this.mInfo;
    }

    public void notifyDataSetChanged() {
        if (!this.mLauncher.getForceTouchLayer().isInterceptedByForceTouchLayer() || (this.mForceTouchStarted && this.mForceTouchSelectedShortcutInfo != null)) {
            buildSortingMap();
            super.notifyDataSetChanged();
            this.mLauncher.updateFolderMessage(this.mInfo);
            this.mInfo.refreshPreviewIcons();
        }
    }

    public ShortcutInfo getItem(int position) {
        return (ShortcutInfo) super.getItem(((Integer) this.mPositionMap[position]).intValue());
    }

    public long getItemId(int position) {
        return getItem(position).id;
    }

    public void reorderItemByInsert(ShortcutInfo dst) {
        if (this.mFirstDragItem != dst) {
            int i;
            ShortcutInfo cur;
            int srcStartIndex = -1;
            int dstIndex = -1;
            for (i = 0; i < getCount(); i++) {
                cur = getItem(i);
                cur.cellX = i;
                if (cur == this.mFirstDragItem) {
                    srcStartIndex = i;
                }
                if (cur == dst) {
                    dstIndex = i;
                }
            }
            if (dst == null) {
                dstIndex = getCount() - 1;
            }
            int srcsCount = this.mDragOverItems.size();
            int startIndex = Math.min(dstIndex, srcStartIndex);
            int endIndex = Math.min(Math.max((dstIndex + srcsCount) - 1, (srcStartIndex + srcsCount) - 1), getCount() - 1);
            int moveOffset = Math.abs(dstIndex - srcStartIndex);
            boolean forward = startIndex == dstIndex;
            for (i = startIndex; i <= endIndex; i++) {
                cur = getItem(i);
                if (forward) {
                    if (this.mDragOverItems.contains(cur)) {
                        cur.cellX -= moveOffset;
                    } else {
                        cur.cellX += srcsCount;
                    }
                } else if (this.mDragOverItems.contains(cur)) {
                    cur.cellX += moveOffset;
                } else {
                    cur.cellX -= srcsCount;
                }
            }
            notifyDataSetChanged();
        }
    }

    public void saveContentPosition() {
        LauncherModel.updateFolderItems(getContext(), this.mInfo);
    }

    public void disableSaveWhenDatasetChanged(boolean isDisable) {
        this.mDisableSaveWhenDatasetChanged = isDisable;
    }

    private void buildSortingMap() {
        SortedMap<ShortcutInfo, Integer> posMap = new TreeMap(PC);
        int i = 0;
        Iterator i$ = this.mInfo.contents.iterator();
        while (i$.hasNext()) {
            int i2 = i + 1;
            posMap.put((ShortcutInfo) i$.next(), Integer.valueOf(i));
            i = i2;
        }
        this.mPositionMap = posMap.values().toArray();
        if (!this.mDisableSaveWhenDatasetChanged && formatToSequence()) {
            saveContentPosition();
        }
    }

    private boolean formatToSequence() {
        boolean isPosDiffFromDatabase = false;
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (getItem(i).cellX != i) {
                isPosDiffFromDatabase = true;
            }
            getItem(i).cellX = i;
        }
        return isPosDiffFromDatabase;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ShortcutInfo info = getItem(position);
        View icon = ShortcutIcon.fromXml(R.layout.application, this.mLauncher, parent, info);
        if (icon.getGhostView() != null) {
            icon.setVisibility(4);
        }
        if (this.mDragOverItems.contains(info)) {
            icon.setLayerType(1, null);
            icon.setAlpha(0.2f);
        } else {
            icon.setLayerType(icon.getDefaultLayerType(), null);
            icon.setAlpha(1.0f);
        }
        if (this.mDroppingDragViews.containsKey(info)) {
            DragView dragView = (DragView) this.mDroppingDragViews.get(info);
            if (dragView.isTargetAnimating()) {
                dragView.updateAnimateTarget(icon);
                icon.setVisibility(4);
            }
        }
        if ((this.mLauncher.isInNormalEditing() && info.container == this.mInfo.id) || Launcher.isChildrenModeEnabled()) {
            icon.onWallpaperColorChanged();
        } else {
            icon.initDefaultTitle();
        }
        if (this.mForceTouchSelectedShortcutInfo != null && this.mForceTouchSelectedShortcutInfo == info && this.mForceTouchStarted) {
            icon.setVisibility(4);
        }
        return icon;
    }

    public void removeAllDrags() {
        if (this.mDragOverItems.size() != 0) {
            setNotifyOnChange(false);
            Iterator i$ = this.mDragOverItems.iterator();
            while (i$.hasNext()) {
                remove((ShortcutInfo) i$.next());
            }
            notifyDataSetChanged();
            this.mDragOverItems.clear();
        }
    }

    public void setForceTouchSelectedShortcutInfo(ShortcutInfo shortcutInfo) {
        this.mForceTouchSelectedShortcutInfo = shortcutInfo;
    }

    public ShortcutInfo getForceTouchSelectedShortcutInfo() {
        return this.mForceTouchSelectedShortcutInfo;
    }

    public void setForceTouchStarted(boolean forceTouchStarted) {
        this.mForceTouchStarted = forceTouchStarted;
    }
}
