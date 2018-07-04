package com.miui.home.launcher.snapshot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import com.miui.home.launcher.LauncherApplication;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.ConfigableGadget;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.upsidescene.data.FreeStyle;
import com.miui.home.launcher.upsidescene.data.FreeStyleSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HomeSnapshotHelperService extends Service {
    private Binder mBinder = new Binder() {
        private final int TRANSACTION_CODE_GET_FREEHOME_INFO = 2;
        private final int TRANSACTION_CODE_SNAPSHOT_GADGET = 1;
        private final String TRANSACTION_DESCRIPTOR = "com.miui.home.snapshot_helper";

        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1598968902) {
                reply.writeString("com.miui.home.snapshot_helper");
                return true;
            } else if (code == 1) {
                data.enforceInterface("com.miui.home.snapshot_helper");
                HomeSnapshotHelperService.this.mHandler.post(new GadgetSnapshotRunnable(HomeSnapshotHelperService.this, data.readInt(), data.readString(), data.readString()));
                reply.writeNoException();
                reply.writeInt(1);
                return true;
            } else if (code != 2) {
                return super.onTransact(code, data, reply, flags);
            } else {
                data.enforceInterface("com.miui.home.snapshot_helper");
                reply.writeNoException();
                reply.writeInt(HomeSnapshotHelperService.this.getCurrenFreeHomeInfo());
                return true;
            }
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private class GadgetSnapshotRunnable implements Runnable {
        private Context mContext;
        private int mGadgetType;
        private String mGadgetZipFilePath;
        private String mSaveSnapshotPath;

        public GadgetSnapshotRunnable(Context context, int gadgetType, String gadgetZipFilePath, String saveSnapshotPath) {
            this.mContext = context;
            this.mGadgetType = gadgetType;
            this.mGadgetZipFilePath = gadgetZipFilePath;
            this.mSaveSnapshotPath = saveSnapshotPath;
        }

        public void run() {
            Log.d("snapshot", "gadgetZipPath: " + this.mGadgetZipFilePath + " saveShotPath: " + this.mSaveSnapshotPath);
            final ConfigableGadget gadget = (ConfigableGadget) GadgetFactory.createGadget(this.mContext, GadgetFactory.getInfo(this.mGadgetType), -1);
            final LinearLayout root = new LinearLayout(this.mContext);
            root.addView(gadget, HomeSnapshotHelperService.this.getGadgetLayout(this.mGadgetZipFilePath));
            root.setGravity(17);
            final WindowManager wm = (WindowManager) HomeSnapshotHelperService.this.getSystemService("window");
            LayoutParams lp = new LayoutParams(1003, 0);
            lp.format = -3;
            lp.setTitle("GadgetSnapshot");
            lp.flags &= -9;
            lp.token = LauncherApplication.getLauncher(this.mContext).getWindow().getDecorView().getWindowToken();
            wm.addView(root, lp);
            gadget.onCreate();
            gadget.onResume();
            new Handler() {
                public void handleMessage(Message msg) {
                    int w = gadget.getWidth();
                    int h = gadget.getHeight();
                    Log.d("snapshot", "Gadet size getLayout: width = " + w + " height = " + h);
                    Bitmap tmpBmp = Utilities.createBitmapSafely(w, h, Config.ARGB_8888);
                    if (tmpBmp != null) {
                        gadget.draw(new Canvas(tmpBmp));
                        HomeSnapshotHelperService.saveBitmapToLocal(tmpBmp, GadgetSnapshotRunnable.this.mSaveSnapshotPath);
                    }
                    wm.removeView(root);
                    gadget.onDestroy();
                }
            }.sendEmptyMessageDelayed(0, 2000);
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    private int getCurrenFreeHomeInfo() {
        FreeStyle s = new FreeStyleSerializer(this).load();
        if (s != null) {
            return ((((s.getDriftScreen().getWidth() + s.getWidth()) - 1) / s.getWidth()) << 16) | (1048575 & s.getDriftScreen().getHome());
        }
        Log.e("snapshot", "    Cannot create FreeHome screen because of invalid format.");
        return 0;
    }

    private LinearLayout.LayoutParams getGadgetLayout(String gadgetZipPath) {
        float cellWidth;
        float cellHeight;
        String fileName = new File(gadgetZipPath).getName();
        if (fileName.indexOf(46) > 0) {
            fileName = fileName.substring(0, fileName.lastIndexOf(46));
        }
        String tmp = fileName.substring(fileName.lastIndexOf("_") + 1);
        int xcells = tmp.charAt(tmp.length() - 1) - 48;
        int ycells = tmp.charAt(0) - 48;
        float density = getResources().getDisplayMetrics().density;
        if (density > 2.0f) {
            cellWidth = 240.0f;
            cellHeight = 332.0f;
        } else if (density > 1.5f) {
            cellWidth = 180.0f;
            cellHeight = 252.0f;
        } else {
            cellWidth = 120.0f;
            cellHeight = 166.0f;
        }
        int w = (int) (((float) xcells) * cellWidth);
        int h = (int) (((float) ycells) * cellHeight);
        Log.d("snapshot", "Gadet size computed: width = " + w + " height = " + h);
        return new LinearLayout.LayoutParams(w, h);
    }

    private static boolean saveBitmapToLocal(Bitmap bmp, String storePath) {
        Throwable th;
        IOException e;
        if (storePath.endsWith(".jpg") || storePath.endsWith(".png")) {
            boolean forJPG = storePath.endsWith(".jpg");
            FileOutputStream outputStream = null;
            try {
                FileOutputStream outputStream2 = new FileOutputStream(storePath);
                if (bmp != null) {
                    CompressFormat compressFormat;
                    if (forJPG) {
                        try {
                            compressFormat = CompressFormat.JPEG;
                        } catch (Throwable th2) {
                            th = th2;
                            outputStream = outputStream2;
                            if (bmp != null) {
                                try {
                                    bmp.recycle();
                                } catch (IOException e2) {
                                    e = e2;
                                    e.printStackTrace();
                                    Log.e("snapshot", "HomeSnaphotHelperService.saveBitmapToLocal() has exception: " + e);
                                    return true;
                                }
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            throw th;
                        }
                    }
                    compressFormat = CompressFormat.PNG;
                    bmp.compress(compressFormat, forJPG ? 80 : 10, outputStream2);
                }
                if (bmp != null) {
                    try {
                        bmp.recycle();
                    } catch (IOException e3) {
                        e = e3;
                        outputStream = outputStream2;
                        e.printStackTrace();
                        Log.e("snapshot", "HomeSnaphotHelperService.saveBitmapToLocal() has exception: " + e);
                        return true;
                    }
                }
                if (outputStream2 != null) {
                    outputStream2.close();
                }
                return true;
            } catch (Throwable th3) {
                th = th3;
                if (bmp != null) {
                    bmp.recycle();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                throw th;
            }
        }
        Log.i("snapshot", "Error: not valid file name (*.jpg or *.png): " + storePath);
        return false;
    }
}
