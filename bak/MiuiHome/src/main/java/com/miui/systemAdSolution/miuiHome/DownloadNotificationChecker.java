package com.miui.systemAdSolution.miuiHome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import com.miui.home.launcher.DeviceConfig;
import com.miui.systemAdSolution.miuiHome.IMiuiHomeDownloadActivateService.Stub;

public class DownloadNotificationChecker {
    private static MyServiceConnection sMyServiceConnection = new MyServiceConnection();

    private static class MyServiceConnection implements ServiceConnection {
        private Context mTempContext;

        private MyServiceConnection() {
            this.mTempContext = null;
        }

        public void bind(Context context) {
            if (context != null && this.mTempContext == null) {
                this.mTempContext = context.getApplicationContext();
                Intent intent = new Intent("miui.intent.action.ad.MIUI_HOME_DOWNLOAD_ACTIVATE");
                intent.setPackage("com.miui.systemAdSolution");
                this.mTempContext.bindService(intent, DownloadNotificationChecker.sMyServiceConnection, 1);
            }
        }

        public void unBind() {
            if (this.mTempContext != null) {
                this.mTempContext.unbindService(DownloadNotificationChecker.sMyServiceConnection);
                this.mTempContext = null;
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            final IMiuiHomeDownloadActivateService miuiHomeDownloadActivateService = Stub.asInterface(service);
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    try {
                        switch (miuiHomeDownloadActivateService.getServiceVersion()) {
                            case 1:
                                DeviceConfig.setIsShowNotification(miuiHomeDownloadActivateService.showDownloadNotification());
                                break;
                        }
                    } catch (Exception e) {
                        Log.d("DownloadActivateChecker", "onServiceConnected", e);
                    }
                    return null;
                }

                protected void onPostExecute(Void result) {
                    MyServiceConnection.this.unBind();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d("DownloadActivateChecker", "onServiceDisconnected");
        }
    }

    public static void checkShowNotificationFlag(Context context) {
        sMyServiceConnection.bind(context);
    }

    public static void unbind() {
        sMyServiceConnection.unBind();
    }
}
