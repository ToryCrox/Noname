package com.miui.home.launcher.common;

import android.app.MiuiThemeHelper;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.MiuiSettings.Secure;
import android.provider.MiuiSettings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.market.sdk.MarketManager;
import com.miui.home.R;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.Launcher;
import com.miui.home.launcher.LauncherApplication;
import com.miui.home.launcher.ShortcutInfo;
import com.miui.home.launcher.structures.ForceTouchItem;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import libcore.io.IoUtils;
import miui.content.res.IconCustomizer;
import miui.graphics.BitmapFactory;
import miui.os.Build;
import miui.os.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

public class Utilities {
    private static Object sBoostGPUInstance;
    private static Method sBoostMethod;
    private static final Canvas sCanvas = new Canvas();
    private static HashMap<ComponentName, Drawable> sComponentBackgroundColorMap = null;
    private static Interpolator sDefaultAnimatorInterPolator = null;
    private static Paint sIconDarkShadowPaint = null;
    private static Paint sIconShadowBlurPaint = null;
    private static boolean sIsStaging;
    private static final Rect sOldBounds = new Rect();

    static {
        sIsStaging = false;
        sIsStaging = new File("/data/system/miuihome_staging").exists();
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        if (MiuiThemeHelper.isInternationalBuildWithDefaultTheme()) {
            sBoostGPUInstance = null;
            sBoostMethod = null;
        } else {
            sBoostGPUInstance = null;
            sBoostMethod = null;
        }
        try {
            Class boostFrameworkClass = Class.forName("android.util.BoostFramework");
            sBoostMethod = boostFrameworkClass.getMethod("perfLockAcquire", new Class[]{Integer.TYPE, int[].class});
            sBoostGPUInstance = boostFrameworkClass.newInstance();
        } catch (Exception e) {
            Log.d("com.miui.home.launcher.common,Utilities", "boost reflaction error", e);
        }
    }

    public static boolean isStaging() {
        return sIsStaging;
    }

    public static Bitmap createIconBitmap(Drawable icon) {
        if (icon == null) {
            return null;
        }
        if (icon instanceof BitmapDrawable) {
            return ((BitmapDrawable) icon).getBitmap();
        }
        Bitmap bitmap;
        synchronized (sCanvas) {
            int width = DeviceConfig.getIconWidth();
            int height = DeviceConfig.getIconHeight();
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0 && (width < sourceWidth || height < sourceHeight)) {
                float ratio = ((float) sourceWidth) / ((float) sourceHeight);
                if (sourceWidth > sourceHeight) {
                    height = (int) (((float) width) / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (((float) height) * ratio);
                }
            }
            bitmap = Bitmap.createBitmap(DeviceConfig.getIconWidth(), DeviceConfig.getIconHeight(), Config.ARGB_8888);
            Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);
            int left = (DeviceConfig.getIconWidth() - width) / 2;
            int top = (DeviceConfig.getIconHeight() - height) / 2;
            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            icon.setFilterBitmap(true);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
        }
        return bitmap;
    }

    public static float getDescendantCoordRelativeToAncestor(View descendant, View ancestor, float[] coord, boolean useTransformation, boolean ignoreScale) {
        coord[1] = 0.0f;
        coord[0] = 0.0f;
        if (useTransformation) {
            descendant.getMatrix().mapPoints(coord);
        }
        float scale = 1.0f * descendant.getScaleX();
        coord[0] = coord[0] + ((float) descendant.getLeft());
        coord[1] = coord[1] + ((float) descendant.getTop());
        View viewParent = descendant.getParent();
        while ((viewParent instanceof View) && viewParent != ancestor) {
            View view = viewParent;
            if (useTransformation) {
                view.getMatrix().mapPoints(coord);
                scale *= view.getScaleX();
            }
            coord[0] = coord[0] + ((float) (view.getLeft() - view.getScrollX()));
            coord[1] = coord[1] + ((float) (view.getTop() - view.getScrollY()));
            if (view.getId() == R.id.screen) {
                coord[1] = coord[1] - view.getTranslationY();
            }
            viewParent = view.getParent();
        }
        if (ignoreScale) {
            coord[0] = coord[0] - ((((float) descendant.getWidth()) * (1.0f - scale)) / 2.0f);
            coord[1] = coord[1] - ((((float) descendant.getHeight()) * (1.0f - scale)) / 2.0f);
        }
        return scale;
    }

    public static int getDipPixelSize(int dip) {
        return Math.round(((float) dip) * DeviceConfig.getScreenDensity());
    }

    public static int getDipPixelSize(float dip) {
        return Math.round(DeviceConfig.getScreenDensity() * dip);
    }

    public static Drawable loadThemeCompatibleDrawable(Context context, int resId) {
        Drawable d = context.getResources().getDrawable(resId);
        return (d == null || d.getMinimumWidth() <= 1 || d.getMinimumHeight() <= 1) ? null : d;
    }

    public static BitmapDrawable loadToggleBackground(Context context) {
        BitmapDrawable bg = IconCustomizer.getRawIconDrawable("com.miui.home.toggle_bg.png");
        if (bg == null) {
            return (BitmapDrawable) context.getResources().getDrawable(R.drawable.toggle_bg);
        }
        return bg;
    }

    public static boolean hasDefaultIconBackground(ComponentName cn) {
        if (!MiuiThemeHelper.isInternationalBuildWithDefaultTheme()) {
            return false;
        }
        if (sComponentBackgroundColorMap == null) {
            sComponentBackgroundColorMap = new HashMap();
            Drawable icon545454 = IconCustomizer.generateIconStyleDrawable(new PaintDrawable(Color.rgb(54, 54, 54)));
            Drawable icon255255255 = IconCustomizer.generateIconStyleDrawable(new PaintDrawable(Color.rgb(255, 255, 255)));
            Drawable icon226226226 = IconCustomizer.generateIconStyleDrawable(new PaintDrawable(Color.rgb(226, 226, 226)));
            Drawable icon2126653 = IconCustomizer.generateIconStyleDrawable(new PaintDrawable(Color.rgb(221, 66, 53)));
            Drawable icon202206215 = IconCustomizer.generateIconStyleDrawable(new PaintDrawable(Color.rgb(202, 206, 215)));
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.gm/.ConversationListActivityGmail"), icon545454);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.android.vending/.AssetBrowserActivity"), icon202206215);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.magazines/com.google.apps.dots.android.app.activity.CurrentsStartActivity"), icon545454);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/.VoiceSearchActivity"), icon2126653);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.youtube/.app.honeycomb.Shell$HomeActivity"), icon226226226);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.docs/.app.NewMainProxyActivity"), icon255255255);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.plus/.phone.HomeActivity"), icon255255255);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.talk/.SigningInActivity"), icon255255255);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.videos/com.google.android.youtube.videos.EntryPoint"), icon226226226);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.maps/com.google.android.maps.MapsActivity"), icon226226226);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.photos/.home.HomeActivity"), icon545454);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.plus/.phone.ConversationListActivity"), icon545454);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.play.games/com.google.android.gms.games.ui.destination.main.MainActivity"), icon255255255);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.music/com.android.music.activitymanagement.TopLevelActivity"), icon255255255);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.books/.app.BooksActivity"), icon545454);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.android.chrome/com.google.android.apps.chrome.Main"), icon255255255);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.maps/com.google.android.maps.PlacesActivity"), icon545454);
            sComponentBackgroundColorMap.put(ComponentName.unflattenFromString("com.google.android.apps.maps/com.google.android.maps.driveabout.app.DestinationActivity"), icon226226226);
        }
        return sComponentBackgroundColorMap.containsKey(cn);
    }

    public static Drawable getDefaultIconBackground(Context context, ComponentName cn) {
        if (!MiuiThemeHelper.isInternationalBuildWithDefaultTheme() || sComponentBackgroundColorMap == null) {
            return null;
        }
        return (Drawable) sComponentBackgroundColorMap.get(cn);
    }

    public static Paint getIconShadowBlurPaint(float shadowSize) {
        if (sIconShadowBlurPaint == null) {
            sIconShadowBlurPaint = new Paint();
            sIconShadowBlurPaint.setMaskFilter(new BlurMaskFilter(shadowSize, Blur.INNER));
        }
        return sIconShadowBlurPaint;
    }

    public static Paint getIconDarkShadowPaint(float shadowSize, int color) {
        if (sIconDarkShadowPaint == null) {
            sIconDarkShadowPaint = new Paint();
            sIconDarkShadowPaint.setColor(0);
            sIconDarkShadowPaint.setShadowLayer(shadowSize, 1.0f, shadowSize, color);
        }
        return sIconDarkShadowPaint;
    }

    public static Interpolator getDefaultAnimatorInterPolator() {
        if (sDefaultAnimatorInterPolator == null) {
            sDefaultAnimatorInterPolator = new LinearInterpolator();
        }
        return sDefaultAnimatorInterPolator;
    }

    public static void resetResourceDependenceItem() {
        sIconShadowBlurPaint = null;
        sIconDarkShadowPaint = null;
    }

    public static Element parseManifestInZip(String zipPath) {
        Element documentElement;
        Exception e;
        Throwable th;
        ZipFile zip = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        InputStream is = null;
        try {
            ZipFile zip2 = new ZipFile(zipPath);
            try {
                is = zip2.getInputStream(zip2.getEntry("manifest.xml"));
                documentElement = dbf.newDocumentBuilder().parse(is).getDocumentElement();
                IoUtils.closeQuietly(is);
                if (zip2 != null) {
                    try {
                        zip2.close();
                    } catch (IOException e2) {
                    }
                }
                zip = zip2;
            } catch (Exception e3) {
                e = e3;
                zip = zip2;
                try {
                    Log.e("com.miui.home.launcher.common,Utilities", e.toString());
                    documentElement = null;
                    IoUtils.closeQuietly(is);
                    if (zip != null) {
                        try {
                            zip.close();
                        } catch (IOException e4) {
                        }
                    }
                    return documentElement;
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(is);
                    if (zip != null) {
                        try {
                            zip.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zip = zip2;
                IoUtils.closeQuietly(is);
                if (zip != null) {
                    zip.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            Log.e("com.miui.home.launcher.common,Utilities", e.toString());
            documentElement = null;
            IoUtils.closeQuietly(is);
            if (zip != null) {
                zip.close();
            }
            return documentElement;
        }
        return documentElement;
    }

    public static String dumpsys(Launcher launcher, String serviceName, String[] args) {
        try {
            RandomAccessFile f = launcher.getTempFile();
            if (f != null) {
                f.seek(0);
                ServiceManager.getService(serviceName).dump(f.getFD(), args);
                f.seek(0);
                byte[] buffer = new byte[((int) f.length())];
                return new String(buffer, 0, f.read(buffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        }
        return null;
    }

    public static boolean extract(String dst, String zipPath, String dftZip) {
        File src = new File(zipPath);
        if (!(src.exists() && src.canRead())) {
            src = new File(dftZip);
        }
        return FileUtils.copyFile(src, new File(dst));
    }

    public static boolean copyFile(String dstPath, String srcPath) {
        if (srcPath.equals(dstPath)) {
            return false;
        }
        return FileUtils.copyFile(new File(srcPath), new File(dstPath));
    }

    public static Intent getDeskClockTabActivityIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.android.deskclock", "com.android.deskclock.DeskClockTabActivity");
        intent.addFlags(270532608);
        return intent;
    }

    public static boolean canPickTheme(Context context) {
        if (Build.IS_TABLET || context == null) {
            return false;
        }
        if (context.getPackageManager().queryIntentActivities(new Intent("miui.intent.action.PICK_GADGET"), 0).size() > 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getImageRotation(java.io.InputStream r14) {
        /*
        if (r14 != 0) goto L_0x0004;
    L_0x0002:
        r11 = 0;
    L_0x0003:
        return r11;
    L_0x0004:
        r11 = 8;
        r0 = new byte[r11];
        r5 = 0;
    L_0x0009:
        r11 = 2;
        r11 = read(r14, r0, r11);
        if (r11 == 0) goto L_0x0078;
    L_0x0010:
        r11 = 0;
        r11 = r0[r11];
        r11 = r11 & 255;
        r12 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r11 != r12) goto L_0x0078;
    L_0x0019:
        r11 = 1;
        r11 = r0[r11];
        r7 = r11 & 255;
        r11 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r7 == r11) goto L_0x0009;
    L_0x0022:
        r11 = 216; // 0xd8 float:3.03E-43 double:1.067E-321;
        if (r7 == r11) goto L_0x0009;
    L_0x0026:
        r11 = 1;
        if (r7 == r11) goto L_0x0009;
    L_0x0029:
        r11 = 217; // 0xd9 float:3.04E-43 double:1.07E-321;
        if (r7 == r11) goto L_0x0031;
    L_0x002d:
        r11 = 218; // 0xda float:3.05E-43 double:1.077E-321;
        if (r7 != r11) goto L_0x0033;
    L_0x0031:
        r11 = 0;
        goto L_0x0003;
    L_0x0033:
        r11 = 2;
        r11 = read(r14, r0, r11);
        if (r11 != 0) goto L_0x003c;
    L_0x003a:
        r11 = 0;
        goto L_0x0003;
    L_0x003c:
        r11 = 0;
        r12 = 2;
        r13 = 0;
        r5 = pack(r0, r11, r12, r13);
        r11 = 2;
        if (r5 >= r11) goto L_0x004f;
    L_0x0046:
        r11 = "com.miui.home.launcher.common,Utilities";
        r12 = "Invalid length";
        android.util.Log.e(r11, r12);
        r11 = 0;
        goto L_0x0003;
    L_0x004f:
        r5 = r5 + -2;
        r11 = 225; // 0xe1 float:3.15E-43 double:1.11E-321;
        if (r7 != r11) goto L_0x0088;
    L_0x0055:
        r11 = 6;
        if (r5 < r11) goto L_0x0088;
    L_0x0058:
        r11 = 6;
        r11 = read(r14, r0, r11);
        if (r11 != 0) goto L_0x0061;
    L_0x005f:
        r11 = 0;
        goto L_0x0003;
    L_0x0061:
        r5 = r5 + -6;
        r11 = 0;
        r12 = 4;
        r13 = 0;
        r11 = pack(r0, r11, r12, r13);
        r12 = 1165519206; // 0x45786966 float:3974.5874 double:5.758429993E-315;
        if (r11 != r12) goto L_0x0088;
    L_0x006f:
        r11 = 4;
        r12 = 2;
        r13 = 0;
        r11 = pack(r0, r11, r12, r13);
        if (r11 != 0) goto L_0x0088;
    L_0x0078:
        r11 = 8;
        if (r5 <= r11) goto L_0x0111;
    L_0x007c:
        r8 = 0;
        r4 = new byte[r5];
        r11 = read(r14, r4, r5);
        if (r11 != 0) goto L_0x0093;
    L_0x0085:
        r11 = 0;
        goto L_0x0003;
    L_0x0088:
        r12 = (long) r5;
        r14.skip(r12);	 Catch:{ IOException -> 0x008f }
        r5 = 0;
        goto L_0x0009;
    L_0x008f:
        r3 = move-exception;
        r11 = 0;
        goto L_0x0003;
    L_0x0093:
        r11 = 4;
        r12 = 0;
        r10 = pack(r4, r8, r11, r12);
        r11 = 1229531648; // 0x49492a00 float:823968.0 double:6.074693478E-315;
        if (r10 == r11) goto L_0x00ad;
    L_0x009e:
        r11 = 1296891946; // 0x4d4d002a float:2.14958752E8 double:6.40749757E-315;
        if (r10 == r11) goto L_0x00ad;
    L_0x00a3:
        r11 = "com.miui.home.launcher.common,Utilities";
        r12 = "Invalid byte order";
        android.util.Log.e(r11, r12);
        r11 = 0;
        goto L_0x0003;
    L_0x00ad:
        r11 = 1229531648; // 0x49492a00 float:823968.0 double:6.074693478E-315;
        if (r10 != r11) goto L_0x00cb;
    L_0x00b2:
        r6 = 1;
    L_0x00b3:
        r11 = 4;
        r12 = 4;
        r11 = pack(r4, r11, r12, r6);
        r1 = r11 + 2;
        r11 = 10;
        if (r1 < r11) goto L_0x00c1;
    L_0x00bf:
        if (r1 <= r5) goto L_0x00cd;
    L_0x00c1:
        r11 = "com.miui.home.launcher.common,Utilities";
        r12 = "Invalid offset";
        android.util.Log.e(r11, r12);
        r11 = 0;
        goto L_0x0003;
    L_0x00cb:
        r6 = 0;
        goto L_0x00b3;
    L_0x00cd:
        r8 = r8 + r1;
        r5 = r5 - r1;
        r11 = r8 + -2;
        r12 = 2;
        r1 = pack(r4, r11, r12, r6);
        r2 = r1;
    L_0x00d7:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x0111;
    L_0x00db:
        r11 = 12;
        if (r5 < r11) goto L_0x0111;
    L_0x00df:
        r11 = 2;
        r10 = pack(r4, r8, r11, r6);
        r11 = 274; // 0x112 float:3.84E-43 double:1.354E-321;
        if (r10 != r11) goto L_0x010b;
    L_0x00e8:
        r11 = r8 + 8;
        r12 = 2;
        r9 = pack(r4, r11, r12, r6);
        switch(r9) {
            case 1: goto L_0x00fc;
            case 2: goto L_0x00f2;
            case 3: goto L_0x00ff;
            case 4: goto L_0x00f2;
            case 5: goto L_0x00f2;
            case 6: goto L_0x0103;
            case 7: goto L_0x00f2;
            case 8: goto L_0x0107;
            default: goto L_0x00f2;
        };
    L_0x00f2:
        r11 = "com.miui.home.launcher.common,Utilities";
        r12 = "Unsupported orientation";
        android.util.Log.i(r11, r12);
        r11 = 0;
        goto L_0x0003;
    L_0x00fc:
        r11 = 0;
        goto L_0x0003;
    L_0x00ff:
        r11 = 180; // 0xb4 float:2.52E-43 double:8.9E-322;
        goto L_0x0003;
    L_0x0103:
        r11 = 90;
        goto L_0x0003;
    L_0x0107:
        r11 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
        goto L_0x0003;
    L_0x010b:
        r8 = r8 + 12;
        r5 = r5 + -12;
        r2 = r1;
        goto L_0x00d7;
    L_0x0111:
        r11 = "com.miui.home.launcher.common,Utilities";
        r12 = "Orientation not found";
        android.util.Log.i(r11, r12);
        r11 = 0;
        goto L_0x0003;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.common.Utilities.getImageRotation(java.io.InputStream):int");
    }

    private static int pack(byte[] bytes, int offset, int length, boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }
        int value = 0;
        int length2 = length;
        while (true) {
            length = length2 - 1;
            if (length2 <= 0) {
                return value;
            }
            value = (value << 8) | (bytes[offset] & 255);
            offset += step;
            length2 = length;
        }
    }

    private static boolean read(InputStream is, byte[] buf, int length) {
        try {
            return is.read(buf, 0, length) == length;
        } catch (IOException e) {
            return false;
        }
    }

    public static void closeFileSafely(Closeable file) {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap createBitmapSafely(int width, int height, Config config) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap createBitmapSafely(Bitmap source, int x, int y, int width, int height) {
        try {
            return Bitmap.createBitmap(source, x, y, width, height);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isChildrenModeEnabled(Context context) {
        return Secure.getBoolean(context.getContentResolver(), "children_mode_enabled", false);
    }

    public static boolean isScreenCellsLocked(Context context) {
        return System.getBoolean(context.getContentResolver(), "miui_home_lock_screen_cells", false);
    }

    public static boolean enableAutoFillEmpty(Context context) {
        return System.getBoolean(context.getContentResolver(), "miui_home_enable_auto_fill_empty_cells", true);
    }

    public static Intent generateShowFragmentIntent(Intent intent, String lable) {
        Bundle bundle = new Bundle();
        bundle.putString(":miui:starting_window_label", lable);
        intent.putExtra(":android:show_fragment_args", bundle);
        return intent;
    }

    public static void startActivity(Context context, String uri, View v) {
        if (TextUtils.isEmpty(uri)) {
            Log.w("com.miui.home.launcher.common,Utilities", "Has no intent uri.");
            return;
        }
        try {
            Intent intent = Intent.parseUri(uri, 0);
            intent.addFlags(270532608);
            LauncherApplication.startActivity(context, intent, v);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        InputStream is = null;
        Bitmap result = null;
        if (uri == null) {
            return result;
        }
        try {
            is = context.getContentResolver().openInputStream(uri);
            result = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeFileSafely(is);
        }
        return result;
    }

    public static String getMarketPackageName(Context context) {
        MarketManager.getManager(context);
        return MarketManager.getMarketPackageName();
    }

    public static boolean isRecommendationEnabled(Context context) {
        return MarketManager.getManager(context).isAppStoreEnabled() && DeviceConfig.isRecommendServerEnable();
    }

    public static String getFileMd5(File file) {
        ByteArrayOutputStream byteArrayOutputStream;
        Exception e;
        Throwable th;
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream2 = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(file);
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
            } catch (Exception e2) {
                e = e2;
                fileInputStream = fileInputStream2;
                try {
                    e.printStackTrace();
                    closeFileSafely(fileInputStream);
                    closeFileSafely(byteArrayOutputStream2);
                    return "";
                } catch (Throwable th2) {
                    th = th2;
                    closeFileSafely(fileInputStream);
                    closeFileSafely(byteArrayOutputStream2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                closeFileSafely(fileInputStream);
                closeFileSafely(byteArrayOutputStream2);
                throw th;
            }
            try {
                byte[] buffer = new byte[4096];
                while (true) {
                    int n = fileInputStream2.read(buffer);
                    if (n >= 0) {
                        byteArrayOutputStream.write(buffer, 0, n);
                    } else {
                        String md5 = getMd5(byteArrayOutputStream.toByteArray());
                        closeFileSafely(fileInputStream2);
                        closeFileSafely(byteArrayOutputStream);
                        byteArrayOutputStream2 = byteArrayOutputStream;
                        fileInputStream = fileInputStream2;
                        return md5;
                    }
                }
            } catch (Exception e3) {
                e = e3;
                byteArrayOutputStream2 = byteArrayOutputStream;
                fileInputStream = fileInputStream2;
                e.printStackTrace();
                closeFileSafely(fileInputStream);
                closeFileSafely(byteArrayOutputStream2);
                return "";
            } catch (Throwable th4) {
                th = th4;
                byteArrayOutputStream2 = byteArrayOutputStream;
                fileInputStream = fileInputStream2;
                closeFileSafely(fileInputStream);
                closeFileSafely(byteArrayOutputStream2);
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            closeFileSafely(fileInputStream);
            closeFileSafely(byteArrayOutputStream2);
            return "";
        }
    }

    public static String getMd5(byte[] data) {
        String hashString = "";
        try {
            MessageDigest lDigest = MessageDigest.getInstance("MD5");
            lDigest.update(data);
            BigInteger lHashInt = new BigInteger(1, lDigest.digest());
            hashString = String.format("%1$032X", new Object[]{lHashInt});
        } catch (Exception e) {
        }
        return hashString.toLowerCase();
    }

    public static String readOtherApplicationsRawFile(String packageName, String resName, Resources res) {
        int id = getResId(packageName, "raw", resName, res);
        if (id == 0 || res == null) {
            return "";
        }
        return readRaw(res, id);
    }

    public static String readOtherApplicationsString(String packageName, String resName, Resources res) {
        int id = getResId(packageName, "string", resName, res);
        if (id == 0 || res == null) {
            return "";
        }
        return res.getString(id);
    }

    public static Drawable readOtherApplicationsDrawable(String packageName, String resName, Resources res, int standardWidth, int standardHeight) {
        int id = getResId(packageName, "drawable", resName, res);
        if (id == 0 || res == null) {
            return null;
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeResource(res, id, options);
        optmiseOptions(options, standardWidth, standardHeight);
        Bitmap b = BitmapFactory.decodeResource(res, id, options);
        if (b == null) {
            return null;
        }
        return new BitmapDrawable(b);
    }

    private static int getResId(String packageName, String type, String resName, Resources res) {
        if (res != null) {
            return res.getIdentifier(resName, type, packageName);
        }
        return 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String readRaw(android.content.res.Resources r6, int r7) {
        /*
        r3 = r6.openRawResource(r7);
        r0 = new java.io.ByteArrayOutputStream;
        r0.<init>();
        r2 = r3.read();	 Catch:{ IOException -> 0x0023 }
    L_0x000d:
        r4 = -1;
        if (r2 == r4) goto L_0x0018;
    L_0x0010:
        r0.write(r2);	 Catch:{ IOException -> 0x0023 }
        r2 = r3.read();	 Catch:{ IOException -> 0x0023 }
        goto L_0x000d;
    L_0x0018:
        r3.close();	 Catch:{ IOException -> 0x003e }
    L_0x001b:
        r0.close();	 Catch:{ IOException -> 0x0040 }
    L_0x001e:
        r4 = r0.toString();
        return r4;
    L_0x0023:
        r1 = move-exception;
        r4 = "com.miui.home.launcher.common,Utilities";
        r5 = r1.getMessage();	 Catch:{ all -> 0x0036 }
        android.util.Log.w(r4, r5, r1);	 Catch:{ all -> 0x0036 }
        r3.close();	 Catch:{ IOException -> 0x0042 }
    L_0x0030:
        r0.close();	 Catch:{ IOException -> 0x0034 }
        goto L_0x001e;
    L_0x0034:
        r4 = move-exception;
        goto L_0x001e;
    L_0x0036:
        r4 = move-exception;
        r3.close();	 Catch:{ IOException -> 0x0044 }
    L_0x003a:
        r0.close();	 Catch:{ IOException -> 0x0046 }
    L_0x003d:
        throw r4;
    L_0x003e:
        r4 = move-exception;
        goto L_0x001b;
    L_0x0040:
        r4 = move-exception;
        goto L_0x001e;
    L_0x0042:
        r4 = move-exception;
        goto L_0x0030;
    L_0x0044:
        r5 = move-exception;
        goto L_0x003a;
    L_0x0046:
        r5 = move-exception;
        goto L_0x003d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.common.Utilities.readRaw(android.content.res.Resources, int):java.lang.String");
    }

    public static ArrayList<ForceTouchItem> parseForceTouchStatic(String packageName, String title, Context context) {
        Bundle metaData = getMetaData(context, packageName, title);
        String resName = "";
        if (metaData != null) {
            resName = metaData.getString("forceTouchStatic");
        }
        try {
            Resources res = context.getApplicationContext().getPackageManager().getResourcesForApplication(packageName);
            if (res == null) {
                return null;
            }
            if (TextUtils.isEmpty(resName)) {
                resName = "xiaomi_static_config";
            }
            return parseForceTouch(context, packageName, readOtherApplicationsRawFile(packageName, resName, res), res);
        } catch (NameNotFoundException e) {
            Log.w("com.miui.home.launcher.common,Utilities", e.getMessage(), e);
            return null;
        }
    }

    private static ArrayList<ForceTouchItem> parseForceTouch(Context context, String packageName, String configFileText, Resources res) {
        if (TextUtils.isEmpty(configFileText)) {
            return null;
        }
        try {
            JSONArray ja = new JSONObject(configFileText).getJSONArray("data");
            ArrayList<ForceTouchItem> forceTouchItemArrayList = new ArrayList();
            int destHeight = context.getResources().getDimensionPixelSize(R.dimen.force_touch_icon_standard_height);
            int destWidth = context.getResources().getDimensionPixelSize(R.dimen.force_touch_icon_standard_width);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject joTemp = ja.getJSONObject(i);
                ForceTouchItem forceTouchItem = new ForceTouchItem();
                String tempTxt = JsonUtils.getStringSafely(joTemp, "title_raw");
                if (TextUtils.isEmpty(tempTxt)) {
                    tempTxt = readOtherApplicationsString(packageName, JsonUtils.getStringSafely(joTemp, "title"), res);
                }
                forceTouchItem.setTitle(tempTxt);
                tempTxt = JsonUtils.getStringSafely(joTemp, "desc_raw");
                if (TextUtils.isEmpty(tempTxt)) {
                    tempTxt = readOtherApplicationsString(packageName, JsonUtils.getStringSafely(joTemp, "desc"), res);
                }
                forceTouchItem.setDesc(tempTxt);
                tempTxt = JsonUtils.getStringSafely(joTemp, "image_path");
                forceTouchItem.setDrawableIcon(TextUtils.isEmpty(tempTxt) ? readOtherApplicationsDrawable(packageName, JsonUtils.getStringSafely(joTemp, "image"), res, destWidth, destHeight) : getDrawableFromPath(context, tempTxt, destHeight, destWidth));
                forceTouchItem.setIntent(Intent.parseUri(JsonUtils.getStringSafely(joTemp, "intent"), 0));
                forceTouchItem.setType(JsonUtils.getStringSafely(joTemp, "type"));
                forceTouchItemArrayList.add(forceTouchItem);
            }
            if (forceTouchItemArrayList.isEmpty()) {
                return null;
            }
            return forceTouchItemArrayList;
        } catch (Exception e) {
            Log.w("com.miui.home.launcher.common,Utilities", e.getMessage(), e);
            return null;
        }
    }

    private static Drawable getDrawableFromPath(Context context, String path, int standardHeight, int standardWidth) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        optmiseOptions(options, standardWidth, standardHeight);
        Bitmap b = BitmapFactory.decodeFile(path, options);
        return b == null ? null : new BitmapDrawable(b);
    }

    public static void optmiseOptions(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
    }

    private static Bundle getMetaData(Context context, String packageName, String title) {
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.setPackage(packageName);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> mainActivities = pm.queryIntentActivities(mainIntent, 128);
        Bundle metaData = null;
        if (mainActivities.size() > 1) {
            for (ResolveInfo resolveInfo : mainActivities) {
                if (resolveInfo.activityInfo != null && TextUtils.equals(title, resolveInfo.activityInfo.loadLabel(pm))) {
                    metaData = resolveInfo.activityInfo.metaData;
                }
            }
            return metaData;
        } else if (mainActivities.size() != 1) {
            return null;
        } else {
            if (((ResolveInfo) mainActivities.get(0)).activityInfo != null) {
                return ((ResolveInfo) mainActivities.get(0)).activityInfo.metaData;
            }
            return null;
        }
    }

    public static ArrayList<ForceTouchItem> parseForceTouchDynamic(String packageName, String title, Context context) {
        ArrayList<ForceTouchItem> arrayList = null;
        Bundle metaData = getMetaData(context, packageName, title);
        try {
            Resources res = context.getApplicationContext().getPackageManager().getResourcesForApplication(packageName);
            if (metaData != null) {
                try {
                    arrayList = parseForceTouch(context, packageName, FileUtils.readFileAsString(metaData.getString("forceTouchDynamic")), res);
                } catch (Exception e) {
                    Log.w("com.miui.home.launcher.common,Utilities", "IOException", e);
                }
            }
        } catch (NameNotFoundException e2) {
            Log.w("com.miui.home.launcher.common,Utilities", e2.getMessage(), e2);
        }
        return arrayList;
    }

    public static boolean isXiaomiMarketInstalled(String packageName, Context context) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        String installerPackageName = null;
        try {
            installerPackageName = context.getPackageManager().getInstallerPackageName(packageName);
        } catch (IllegalArgumentException e) {
            Log.d("com.miui.home.launcher.common,Utilities", "isXiaomiMarketInstalled", e);
        }
        return TextUtils.equals("com.xiaomi.market", installerPackageName);
    }

    public static void doNotification(Context context, String title, String content, ShortcutInfo shortcutInfo) {
        if (shortcutInfo != null && context != null && DeviceConfig.isShowNotification()) {
            Intent homeIntent = new Intent("com.miui.home.notification");
            homeIntent.putExtra("com.miui.home.notification.extra", shortcutInfo.intent);
            homeIntent.putExtra("com.miui.home.notification.title", title);
            homeIntent.putExtra("com.miui.home.notification.userId", shortcutInfo.getUserId(context));
            PendingIntent pi = PendingIntent.getBroadcast(context, new Random(System.currentTimeMillis()).nextInt(), homeIntent, 134217728);
            Builder builder = new Builder(context);
            builder.setSmallIcon(R.drawable.icon_launcher).setContentTitle(title).setContentText(content).setContentIntent(pi).setAutoCancel(true).setLargeIcon(drawable2Bitmap(shortcutInfo.getIcon())).setWhen(System.currentTimeMillis());
            if (pi != null) {
                builder.addAction(0, context.getString(R.string.notification_open_button), pi);
                Bundle bundle = new Bundle();
                bundle.putBoolean("miui.showAction", true);
                builder.setExtras(bundle);
            }
            Notification notification = builder.getNotification();
            notification.extraNotification.setCustomizedIcon(true);
            if (getFloatingNotificationTimeDelta(context) == 0 || System.currentTimeMillis() - getFloatingNotificationTimeDelta(context) > 300000) {
                notification.extraNotification.setEnableFloat(true);
                saveNotificationTime(context);
            } else {
                notification.extraNotification.setEnableFloat(false);
            }
            ((NotificationManager) context.getSystemService("notification")).notify(title, 0, notification);
        }
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static void saveNotificationTime(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong("floating_notification_time", System.currentTimeMillis());
        editor.commit();
    }

    private static long getFloatingNotificationTimeDelta(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong("floating_notification_time", 0);
    }

    public static byte[] flattenBitmap(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }

    public static void boostGPU() {
        if (sBoostGPUInstance != null && sBoostMethod != null && DeviceConfig.NEED_BOOST_GPU) {
            int[] params = new int[]{1115701248, 0};
            try {
                sBoostMethod.invoke(sBoostGPUInstance, new Object[]{Integer.valueOf(100), params});
            } catch (Exception e) {
                Log.d("com.miui.home.launcher.common,Utilities", "invoke error", e);
            }
        }
    }

    public static void queryIfAllowToStartGlobalSearch(final Context context) {
        new AsyncTask<Void, Void, Integer>() {
            protected Integer doInBackground(Void... params) {
                Cursor cursor = null;
                try {
                    cursor = context.getApplicationContext().getContentResolver().query(DeviceConfig.GLOBAL_SEARCH_SWITCH_URI, null, null, null, null);
                    if (cursor == null || cursor.getCount() <= 0) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return null;
                    }
                    cursor.moveToFirst();
                    Integer valueOf = Integer.valueOf(cursor.getInt(0));
                    if (cursor == null) {
                        return valueOf;
                    }
                    cursor.close();
                    return valueOf;
                } catch (Exception e) {
                    Log.d("com.miui.home.launcher.common,Utilities", "queryIfAllowToStartGlobalSearch", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }

            protected void onPostExecute(Integer result) {
                if (result != null) {
                    DeviceConfig.setAllowedSlidingUpToStartGolbalSearch(result.intValue() == 0);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }
}
