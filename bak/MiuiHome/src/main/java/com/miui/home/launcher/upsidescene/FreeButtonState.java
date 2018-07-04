package com.miui.home.launcher.upsidescene;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Process;
import android.view.View;
import com.miui.home.R;
import com.miui.home.launcher.Folder.FolderCallback;
import com.miui.home.launcher.FolderInfo;
import com.miui.home.launcher.Launcher;
import com.miui.home.launcher.LauncherApplication;
import com.miui.home.launcher.ShortcutInfo;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.upsidescene.data.Function.AppFunction;
import com.miui.home.launcher.upsidescene.data.Function.FolderFunction;
import com.miui.home.launcher.upsidescene.data.Function.ToggleFunction;
import com.miui.home.launcher.upsidescene.data.Sprite;
import java.util.ArrayList;
import java.util.List;
import miui.app.ToggleManager;
import miui.app.ToggleManager.OnToggleChangedListener;

public class FreeButtonState implements FolderCallback, OnToggleChangedListener {
    private static float[] sTmpPos = new float[2];
    private static ToggleManager sToggleManager;
    private Context mContext;
    private String mCurrentState;
    private View mOwnerView;
    private SceneScreen mSceneScreen;
    private Sprite mSprite;
    private StateUpdateListener mStateUpdateListener;

    public interface StateUpdateListener {
        int onStateUpdated(String str, String str2);
    }

    public FreeButtonState(Context context, Sprite sprite, View ownerView, SceneScreen sceneScreen, StateUpdateListener listener) {
        this.mContext = context;
        this.mOwnerView = ownerView;
        this.mSprite = sprite;
        this.mSceneScreen = sceneScreen;
        this.mStateUpdateListener = listener;
        sToggleManager = ToggleManager.createInstance(context.getApplicationContext());
        if (this.mSprite.getFunction().getType() == 7) {
            ownerView.setTag(((ToggleFunction) this.mSprite.getFunction()).getShortcutInfo());
            sToggleManager.setOnToggleChangedListener(this);
            refreshToggleState();
            return;
        }
        updateState("normal", "normal_pressed");
    }

    private void refreshToggleState() {
        if (this.mSprite.getFunction().getType() == 7) {
            ToggleFunction toggleFunc = (ToggleFunction) this.mSprite.getFunction();
            if (toggleFunc.getToggleId() <= 0) {
                return;
            }
            if (ToggleManager.getStatus(toggleFunc.getToggleId())) {
                updateState("open", "open_pressed");
            } else {
                updateState("normal", "normal_pressed");
            }
        }
    }

    public void trigger() {
        if (this.mSprite.getAppearance().getType() == 2) {
            if (this.mCurrentState == "normal") {
                this.mOwnerView.postDelayed(new Runnable() {
                    public void run() {
                        FreeButtonState.this.normalToOpen();
                    }
                }, (long) updateState("normal_to_open", null));
            } else if (this.mCurrentState == "open") {
                this.mOwnerView.postDelayed(new Runnable() {
                    public void run() {
                        FreeButtonState.this.openToNormal();
                    }
                }, (long) updateState("open_to_normal", null));
            }
        } else if (this.mSprite.getFunction().getType() == 3 || this.mSprite.getFunction().getType() == 9) {
            normalToOpen();
        } else if (this.mSprite.getFunction().getType() == 8) {
            requestExitChildrenMode();
        }
    }

    private void requestExitChildrenMode() {
        Launcher launcher = LauncherApplication.getLauncher(this.mContext);
        if (launcher != null) {
            launcher.exitChildrenMode();
        }
    }

    private int updateState(String state, String pressedState) {
        this.mCurrentState = state;
        if (this.mStateUpdateListener != null) {
            return this.mStateUpdateListener.onStateUpdated(state, pressedState);
        }
        return 0;
    }

    private void openToNormal() {
        switch (this.mSprite.getFunction().getType()) {
            case 0:
                updateState("normal", "normal_pressed");
                return;
            case 7:
                this.mSceneScreen.getLauncher().onClick(this.mOwnerView);
                refreshToggleState();
                return;
            default:
                return;
        }
    }

    private void normalToOpen() {
        switch (this.mSprite.getFunction().getType()) {
            case 0:
                updateState("open", "open_pressed");
                return;
            case 1:
                AppFunction appFunc = (AppFunction) this.mSprite.getFunction();
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.LAUNCHER");
                intent.addFlags(270532608);
                intent.setComponent(appFunc.getComponentName());
                int[] pos = new int[2];
                this.mOwnerView.getLocationOnScreen(pos);
                intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0] + getWidth(), pos[1] + getHeight()));
                LauncherApplication.startActivity(this.mContext, intent, this.mOwnerView);
                this.mOwnerView.postDelayed(new Runnable() {
                    public void run() {
                        FreeButtonState.this.updateState("normal", "normal_pressed");
                    }
                }, 1000);
                return;
            case 2:
            case 3:
            case 9:
                ResolveInfo resolveInfo;
                String title = "";
                List<ComponentName> componentNames;
                if (this.mSprite.getFunction().getType() == 3) {
                    title = this.mContext.getResources().getString(R.string.free_style_drawer_title);
                    Intent mainIntent = new Intent("android.intent.action.MAIN", null);
                    mainIntent.addCategory("android.intent.category.LAUNCHER");
                    List<ResolveInfo> installedApps = ActivityThread.currentApplication().getPackageManager().queryIntentActivities(mainIntent, 0);
                    componentNames = new ArrayList();
                    for (ResolveInfo resolveInfo2 : installedApps) {
                        componentNames.add(new ComponentName(resolveInfo2.activityInfo.packageName, resolveInfo2.activityInfo.name));
                    }
                } else {
                    FolderFunction folderFunc = (FolderFunction) this.mSprite.getFunction();
                    title = folderFunc.getFolderName();
                    if (title == null) {
                        title = this.mContext.getResources().getString(R.string.folder_name);
                    }
                    componentNames = folderFunc.getComponentNames(this.mContext);
                }
                FolderInfo info = new FolderInfo();
                info.setTitle(title, this.mContext);
                PackageManager pm = this.mContext.getPackageManager();
                for (ComponentName componentName : componentNames) {
                    Intent shortcutIntent = new Intent();
                    shortcutIntent.setComponent(componentName);
                    resolveInfo2 = pm.resolveActivity(shortcutIntent, 0);
                    if (resolveInfo2 != null) {
                        info.add(new ShortcutInfo(this.mContext, resolveInfo2, Process.myUserHandle()));
                    }
                }
                info.icon = this;
                this.mSceneScreen.getLauncher().openFolder(info, this.mOwnerView);
                return;
            case 7:
                this.mSceneScreen.getLauncher().onClick(this.mOwnerView);
                refreshToggleState();
                return;
            default:
                return;
        }
    }

    public void onOpen() {
        updateState("open", "open_pressed");
    }

    public void onClose() {
        this.mOwnerView.postDelayed(new Runnable() {
            public void run() {
                FreeButtonState.this.updateState("normal", "normal_pressed");
            }
        }, (long) updateState("open_to_normal", null));
    }

    public void setTitle(CharSequence title) {
        if (this.mSprite.getFunction().getType() == 2) {
            ((FolderFunction) this.mSprite.getFunction()).setFolderName(title.toString());
            this.mSceneScreen.save();
        }
    }

    public void loadItemIcons() {
    }

    public void deleteSelf() {
    }

    public float getPreviewPosition(Rect rect) {
        float[] fArr = sTmpPos;
        sTmpPos[1] = 0.0f;
        fArr[0] = 0.0f;
        float scale = Utilities.getDescendantCoordRelativeToAncestor(this.mOwnerView, this.mSceneScreen, sTmpPos, this.mSceneScreen.isInEditMode(), false);
        rect.set(Math.round(sTmpPos[0]), Math.round(sTmpPos[1]), Math.round(sTmpPos[0] + (((float) getWidth()) * scale)), Math.round(sTmpPos[1] + (((float) getHeight()) * scale)));
        int xOffset = (int) (((float) rect.width()) * 0.5f);
        int yOffset = (int) (((float) rect.height()) * 0.5f);
        rect.set(rect.left + (xOffset / 2), rect.top + (yOffset / 2), rect.right - (xOffset / 2), rect.bottom - (yOffset / 2));
        return scale * 0.5f;
    }

    public void showPreview(boolean isShow) {
    }

    public int getPreviewCount() {
        return 0;
    }

    public int getWidth() {
        return this.mOwnerView.getWidth();
    }

    public int getHeight() {
        return this.mOwnerView.getHeight();
    }

    public void OnToggleChanged(int id) {
        if (this.mSprite.getFunction().getType() == 7 && ((ToggleFunction) this.mSprite.getFunction()).getToggleId() == id) {
            refreshToggleState();
        }
    }
}
