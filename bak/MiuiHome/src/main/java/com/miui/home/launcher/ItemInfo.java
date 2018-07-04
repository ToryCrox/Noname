package com.miui.home.launcher;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import com.miui.home.launcher.common.Utilities;

public class ItemInfo implements Cloneable {
    private static String[] sColumnsWithScreenType = null;
    public int cellX;
    public int cellY;
    public long container;
    public long id;
    public boolean isGesture;
    public boolean isLandscapePos;
    public boolean isRetained;
    public int itemFlags;
    public int itemType;
    public int launchCount;
    public long screenId;
    public int spanX;
    public int spanY;
    private UserHandle user;

    public ItemInfo() {
        this.id = -1;
        this.container = -1;
        this.screenId = -1;
        this.cellX = -1;
        this.cellY = -1;
        this.spanX = 1;
        this.spanY = 1;
        this.launchCount = 0;
        this.isGesture = false;
        this.isRetained = false;
        this.isLandscapePos = false;
        this.user = Process.myUserHandle();
    }

    public ItemInfo(int x, int y, int sX, int sY) {
        this.id = -1;
        this.container = -1;
        this.screenId = -1;
        this.cellX = -1;
        this.cellY = -1;
        this.spanX = 1;
        this.spanY = 1;
        this.launchCount = 0;
        this.isGesture = false;
        this.isRetained = false;
        this.isLandscapePos = false;
        this.cellX = x;
        this.cellY = y;
        this.spanX = sX;
        this.spanY = sY;
        this.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
        this.user = Process.myUserHandle();
    }

    public void load(Context context, Cursor c) {
        load(context, c, false);
    }

    public void load(Context context, Cursor c, boolean landscapePos) {
        int i = 0;
        this.id = c.getLong(0);
        this.cellX = c.isNull(11) ? 0 : c.getInt(11);
        if (!c.isNull(12)) {
            i = c.getInt(12);
        }
        this.cellY = i;
        this.spanX = c.getInt(13);
        this.spanY = c.getInt(14);
        this.screenId = c.isNull(10) ? -1 : c.getLong(10);
        this.itemType = c.getInt(8);
        this.container = c.getLong(7);
        this.launchCount = c.getInt(17);
        this.itemFlags = c.getInt(19);
        this.isLandscapePos = landscapePos;
        this.user = ((UserManager) context.getSystemService("user")).getUserForSerialNumber((long) c.getInt(20));
    }

    public UserHandle getUser() {
        return this.user;
    }

    public void setUser(UserHandle u) {
        this.user = u;
    }

    public void updateUser(Intent intent) {
        if (intent != null && intent.hasExtra("profile")) {
            this.user = (UserHandle) intent.getParcelableExtra("profile");
        }
    }

    public int getUserId(Context context) {
        if (this.user != null) {
            return this.user.getIdentifier();
        }
        return Process.myUserHandle().getIdentifier();
    }

    public void onAddToDatabase(Context context, ContentValues values) {
        values.put("itemType", Integer.valueOf(this.itemType));
        if (!this.isGesture) {
            values.put("container", Long.valueOf(this.container));
            values.put("screen", Long.valueOf(this.screenId));
            values.put("cellX", Integer.valueOf(this.cellX));
            values.put("cellY", Integer.valueOf(this.cellY));
            values.put("spanX", Integer.valueOf(this.spanX));
            values.put("spanY", Integer.valueOf(this.spanY));
            values.put("launchCount", Integer.valueOf(this.launchCount));
            values.put("itemFlags", Integer.valueOf(this.itemFlags));
            values.put("profileId", Long.valueOf(((UserManager) context.getSystemService("user")).getSerialNumberForUser(this.user != null ? this.user : Process.myUserHandle())));
        }
    }

    public void upateUserToDatabase(Context context, ContentValues values) {
        values.put("profileId", Long.valueOf(((UserManager) context.getSystemService("user")).getSerialNumberForUser(this.user == null ? Process.myUserHandle() : this.user)));
    }

    public static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            values.put("icon", Utilities.flattenBitmap(bitmap));
        }
    }

    public boolean isPresetApp() {
        return (this.itemFlags & 1) != 0;
    }

    public boolean isNeedLargeCell() {
        return this.spanX > 1 || this.spanY > 1;
    }

    public boolean isWidget() {
        return this.itemType == 4 || this.itemType == 5;
    }

    public void unbind() {
    }

    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + ")";
    }

    public void onLaunch() {
        this.launchCount++;
    }

    public ItemInfo clone() {
        try {
            return (ItemInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void copyPosition(ItemInfo src) {
        this.container = src.container;
        this.screenId = src.screenId;
        this.cellX = src.cellX;
        this.cellY = src.cellY;
    }

    public static String[] getColumnsWithScreenType() {
        if (sColumnsWithScreenType == null) {
            sColumnsWithScreenType = new String[(ItemQuery.COLUMNS.length + 1)];
            System.arraycopy(ItemQuery.COLUMNS, 0, sColumnsWithScreenType, 0, ItemQuery.COLUMNS.length);
            sColumnsWithScreenType[ItemQuery.COLUMNS.length] = "screenType";
        }
        return sColumnsWithScreenType;
    }
}
