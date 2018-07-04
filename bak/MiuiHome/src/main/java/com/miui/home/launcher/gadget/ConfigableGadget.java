package com.miui.home.launcher.gadget;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.FileUtils;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.common.Utilities;
import java.io.File;
import java.util.Date;

public abstract class ConfigableGadget extends Gadget implements OnClickListener {
    private static final String TAG = ConfigableGadget.class.getCanonicalName();
    protected BackupManager mBackupManager;
    protected Boolean mConfigurable;

    public static class BackupManager {
        private final int mGadgetId;

        public BackupManager(int gadgetId) {
            this.mGadgetId = gadgetId;
        }

        public String getSizeDescript() {
            switch (this.mGadgetId) {
                case 4:
                    return "1x2";
                case 5:
                    return "2x2";
                case 6:
                    return "2x4";
                default:
                    throw new UnsupportedOperationException(String.format("Unknown gadget id %d", new Object[]{Integer.valueOf(this.mGadgetId)}));
            }
        }

        public String getTypeName() {
            switch (this.mGadgetId) {
                case 4:
                case 5:
                case 6:
                    return "clock";
                default:
                    throw new UnsupportedOperationException(String.format("Unknown gadget id %d", new Object[]{Integer.valueOf(this.mGadgetId)}));
            }
        }

        public String getBackupNamePrefix() {
            return String.format("%s_%s_", new Object[]{getTypeName(), getSizeDescript()});
        }

        public String getBackupName(long itemId) {
            return getBackupNamePrefix() + itemId;
        }

        public String getBackupPath(Context context, long itemId) {
            return String.format("%s/%s", new Object[]{getBackupDir(context), getBackupName(itemId)});
        }

        public String getEntryName() {
            return String.format("%s_%s", new Object[]{getTypeName(), getSizeDescript()});
        }

        public String getBackupDir(Context context) {
            return context.getDir(getTypeName() + "_bak", DeviceConfig.TEMP_SHARE_MODE_FOR_WORLD_READABLE).getAbsolutePath();
        }

        public String getPathInTheme() {
            return String.format("%s/%s", new Object[]{"/data/system/theme/", getEntryName()});
        }

        public String getPathInHome(Context context) {
            return String.format("%s/%s", new Object[]{GadgetFactory.getGadgetDir(context), getEntryName()});
        }

        public String getSystemGadgetTheme() {
            String typeName = getTypeName();
            return String.format("/system/media/theme/.data/content/%s_%s/%s.mrc", new Object[]{typeName, getSizeDescript(), typeName});
        }

        public boolean prepareBackup(Context context, long itemId) {
            String dst = getBackupPath(context, itemId);
            File backup = new File(dst);
            if (backup.isFile()) {
                return true;
            }
            File dir = backup.getParentFile();
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File file = new File(getPathInTheme());
            boolean canAutoChange = false;
            boolean hasValidFileInHome = false;
            if (file.exists()) {
                canAutoChange = new GadgetInfo(Uri.fromFile(file)).getBoolean("autoChange");
            }
            File homeFile = new File(getPathInHome(context));
            if (homeFile.exists()) {
                GadgetInfo info = new GadgetInfo(Uri.fromFile(homeFile));
                Date startTime = info.getDate("enableTime");
                Date endTime = info.getDate("disableTime");
                if (startTime == null || endTime == null) {
                    Log.d(ConfigableGadget.TAG, "startTime == null || endTime == null");
                    return false;
                }
                boolean autoChange = info.getBoolean("autoChange");
                long currentTime = System.currentTimeMillis();
                hasValidFileInHome = autoChange && startTime.getTime() <= currentTime && endTime.getTime() > currentTime;
            }
            Log.d(ConfigableGadget.TAG, "canAutoChange:" + canAutoChange + " hasValidFileInHome:" + hasValidFileInHome + " dst:" + dst);
            if (canAutoChange) {
                if (!((hasValidFileInHome && Utilities.copyFile(dst, getPathInHome(context))) || Utilities.copyFile(dst, getPathInTheme()))) {
                    Utilities.extract(dst, getSystemGadgetTheme(), getSystemGadgetTheme());
                }
            } else if (!(Utilities.copyFile(dst, getPathInTheme()) || (hasValidFileInHome && Utilities.copyFile(dst, getPathInHome(context))))) {
                Utilities.extract(dst, getSystemGadgetTheme(), getSystemGadgetTheme());
            }
            if (!backup.exists()) {
                return false;
            }
            FileUtils.setPermissions(dst, 388, -1, -1);
            return true;
        }
    }

    protected abstract View getEditView();

    public ConfigableGadget(Context context) {
        this(context, null);
    }

    public ConfigableGadget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConfigableGadget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mConfigurable = Boolean.valueOf(true);
    }

    public void onCreate() {
        setOnClickListener(this);
        this.mBackupManager = new BackupManager(((GadgetInfo) getTag()).getGadgetId());
    }

    public void onDeleted() {
        Log.d(TAG, "remove gadget " + getItemId());
        new File(this.mBackupManager.getBackupPath(this.mContext, getItemId())).delete();
        deleteConfig();
    }

    public void onAdded() {
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void onResume() {
    }

    public void onEditDisable() {
        View editView = getEditView();
        if (editView != null) {
            editView.setVisibility(8);
        }
        this.mConfigurable = Boolean.valueOf(true);
    }

    public void onEditNormal() {
        View editView = getEditView();
        if (editView != null) {
            editView.setVisibility(0);
            editView.bringToFront();
            editView.setSelected(false);
        }
    }

    public void onClick(View v) {
    }

    protected String getPrefKey() {
        return this.mBackupManager.getBackupName(getItemId());
    }

    public boolean saveConfig(String config) {
        if (getItemId() == -1) {
            return false;
        }
        Editor e = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        e.putString(getPrefKey(), config);
        return e.commit();
    }

    public String loadConfig() {
        return PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(getPrefKey(), null);
    }

    public void deleteConfig() {
        deleteConfig(this.mContext, getPrefKey());
    }

    public static void deleteConfig(Context context, String key) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        e.remove(key);
        e.commit();
    }

    public long getItemId() {
        if (getTag() instanceof GadgetInfo) {
            return ((GadgetInfo) getTag()).id;
        }
        return -1;
    }

    public static int getGadgetIdByName(String name) {
        if ("clock_1x2".equals(name)) {
            return 4;
        }
        if ("clock_2x2".equals(name)) {
            return 5;
        }
        if ("clock_2x4".equals(name)) {
            return 6;
        }
        return -1;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    public boolean isRestrictClick() {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        View editView = getEditView();
        if (!(editView == null || editView.getVisibility() != 0 || isRestrictClick())) {
            switch (event.getAction() & 255) {
                case 0:
                    editView.setSelected(true);
                    break;
                case 1:
                case 3:
                    editView.setSelected(false);
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
}
