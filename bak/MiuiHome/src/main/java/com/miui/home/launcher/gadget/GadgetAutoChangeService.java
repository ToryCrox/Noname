package com.miui.home.launcher.gadget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.miui.home.launcher.Launcher;
import com.miui.home.launcher.LauncherApplication;
import com.miui.home.launcher.common.Utilities;
import java.io.File;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

public class GadgetAutoChangeService extends IntentService {
    public GadgetAutoChangeService() {
        super("GadgetAutoChangeService");
    }

    protected void onHandleIntent(Intent intent) {
        Log.i("Launcher.gadgetServer", "receive " + intent.getAction());
        Context context = getApplicationContext();
        String gadgetPath = intent.getStringExtra("path");
        if (!TextUtils.isEmpty(gadgetPath) && new File(gadgetPath).exists()) {
            Log.i("Launcher.gadgetServer", "src path is " + gadgetPath);
            String md5 = intent.getStringExtra("md5");
            if ("com.miui.home.action_GADGET_MTZ_READY".equals(intent.getAction())) {
                if (!TextUtils.isEmpty(md5) && md5.equals(Utilities.getFileMd5(new File(gadgetPath)))) {
                    String dstFileName = null;
                    String ex = null;
                    String extra = intent.getStringExtra("extra");
                    if (extra != null) {
                        try {
                            JSONObject json = new JSONObject(extra);
                            dstFileName = json.getString("fileName");
                            ex = json.getString("ex");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (TextUtils.isEmpty(dstFileName) || TextUtils.isEmpty(ex)) {
                        Log.i("Launcher.gadgetServer", "fileName or ex is empty! fileName:" + dstFileName + " ex:" + ex);
                        return;
                    }
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString("gadget_ex_" + md5, ex).commit();
                    onGadgetMtzReady(context, gadgetPath, dstFileName);
                }
            } else if ("com.miui.home.action_GADGET_ENABLE".equals(intent.getAction())) {
                reloadGadget(context, gadgetPath);
            } else if ("com.miui.home.action_GADGET_DISABLE".equals(intent.getAction())) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().remove("gadget_ex_" + md5).commit();
                onGadgetDisable(context, gadgetPath);
            }
        }
    }

    public static void init(Context context) {
        File dir = new File(GadgetFactory.getGadgetDir(context));
        if (dir.isDirectory()) {
            String[] gadgetMtzFiles = dir.list();
            if (gadgetMtzFiles != null && gadgetMtzFiles.length > 0) {
                for (int i = 0; i < gadgetMtzFiles.length; i++) {
                    onGadgetMtzReady(context, dir.getAbsolutePath() + "/" + gadgetMtzFiles[i], gadgetMtzFiles[i]);
                }
                return;
            }
            return;
        }
        dir.mkdir();
    }

    public static void onGadgetMtzReady(Context context, String srcPath, String dstFileName) {
        File file = new File(srcPath);
        GadgetInfo info = new GadgetInfo(Uri.fromFile(file));
        Date startTime = info.getDate("enableTime");
        Date endTime = info.getDate("disableTime");
        boolean autoChange = info.getBoolean("autoChange");
        if (startTime == null || endTime == null || !autoChange) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("gadget_ex_" + Utilities.getFileMd5(file)).commit();
            return;
        }
        String gadgetDir = GadgetFactory.getGadgetDir(context);
        File dir = new File(gadgetDir);
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        String gadgetPath = srcPath;
        if (!file.getParent().equals(gadgetDir)) {
            String absolutePath = dir.getAbsolutePath();
            if (TextUtils.isEmpty(dstFileName)) {
                dstFileName = file.getName();
            }
            File dstFile = new File(absolutePath, dstFileName);
            if (Utilities.copyFile(dstFile.getAbsolutePath(), file.getAbsolutePath())) {
                file.delete();
            }
            gadgetPath = dstFile.getAbsolutePath();
        }
        if (System.currentTimeMillis() >= endTime.getTime() || endTime.getTime() <= startTime.getTime()) {
            Log.i("Launcher.gadgetServer", "gadget out of time:" + gadgetPath);
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("gadget_ex_" + Utilities.getFileMd5(new File(gadgetPath))).commit();
            onGadgetDisable(context, gadgetPath);
            return;
        }
        registerGadgetAlarm(context, startTime.getTime(), gadgetPath, "com.miui.home.action_GADGET_ENABLE");
        registerGadgetAlarm(context, endTime.getTime(), gadgetPath, "com.miui.home.action_GADGET_DISABLE");
    }

    private static void registerGadgetAlarm(Context context, long triggerTime, String gadgetPath, String extraAction) {
        AlarmManager alermManager = (AlarmManager) context.getSystemService("alarm");
        final Intent intent = new Intent(extraAction);
        intent.setPackage(context.getPackageName());
        intent.putExtra("path", gadgetPath);
        intent.putExtra("md5", Utilities.getFileMd5(new File(gadgetPath)));
        if (triggerTime - System.currentTimeMillis() < 900000) {
            final Launcher launcher = LauncherApplication.getLauncher(context);
            if (launcher != null && launcher.getDragLayer() != null) {
                launcher.getDragLayer().postDelayed(new Runnable() {
                    public void run() {
                        launcher.startService(intent);
                    }
                }, triggerTime - System.currentTimeMillis());
                return;
            }
            return;
        }
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alermManager.cancel(pendingIntent);
        alermManager.setExact(0, triggerTime, pendingIntent);
    }

    public static void onGadgetDisable(Context context, String gadgetPath) {
        if (!TextUtils.isEmpty(gadgetPath)) {
            File file = new File(gadgetPath);
            String md5 = Utilities.getFileMd5(file);
            String name = file.getName();
            File dir = new File(GadgetFactory.getGadgetDir(context));
            if (dir.isDirectory()) {
                File dstFile = new File(dir.getAbsolutePath(), name);
                if (dstFile.exists() && Utilities.getFileMd5(dstFile).equals(md5)) {
                    dstFile.delete();
                }
            }
            reloadGadget(context, gadgetPath);
        }
    }

    public static void reloadGadget(Context context, String gadgetPath) {
        final int gadgetId = ConfigableGadget.getGadgetIdByName(new File(gadgetPath).getName());
        if (gadgetId != -1) {
            ClockGadgetDelegate.updateBackup(context);
            final Launcher launcher = LauncherApplication.getLauncher(context);
            if (launcher != null) {
                launcher.runOnUiThread(new Runnable() {
                    public void run() {
                        launcher.reloadGadget(gadgetId);
                    }
                });
            }
        }
    }
}
