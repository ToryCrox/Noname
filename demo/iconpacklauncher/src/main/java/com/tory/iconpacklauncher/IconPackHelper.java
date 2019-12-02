package com.tory.iconpacklauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tao.xu2
 * @date 2018/1/24
 */

public class IconPackHelper {
    private static final String TAG = "IconPackHelper";


    public static void parseIconPack(@NonNull Context context, @NonNull String pkg,
                                              @NonNull IconPackTheme packTheme){
        if(TextUtils.equals(pkg, packTheme.mPackageName)){
            return;
        }
        packTheme.onDestroy();
        final PackageManager pm = context.getPackageManager();

        int versionCode = 0;
        XmlPullParser parser = null;
        InputStream is;
        Resources res = null;
        try {
            versionCode = pm.getPackageInfo(pkg, 0).versionCode;
            res = pm.getResourcesForApplication(pkg);
            int resId = res.getIdentifier("appfilter", "xml", pkg);
            if(resId != 0){
                parser = res.getXml(resId);
            }else{
                is = res.getAssets().open("appfilter.xml");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xmlPullParser = factory.newPullParser();
                xmlPullParser.setInput(is, "UTF-8");
                parser = xmlPullParser;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        if(res == null || parser == null){
            return;
        }

        int iconbackResId = 0;
        int iconuponResId = 0;
        int iconmaskResId = 0;
        float scale = 1f;
        Map<ComponentName, Integer> iconPacks = new HashMap<>();
        Map<ComponentName, SparseIntArray> calendarPacks = new HashMap<>();
        SparseArray<String> iconResNames = new SparseArray<>();

        //TODO: remove
        ArrayMap<String, Integer> aliasComponets = new ArrayMap<>();
        SparseIntArray calendarIcons = null;

        try {
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if(event != XmlPullParser.START_TAG){
                    event = parser.next();
                    continue;
                }
                String name = parser.getName();
                if("scale".equals(name)){
                    String value = parser.getAttributeValue(0);
                    if(!TextUtils.isEmpty(value)){
                        scale = Float.parseFloat(value);
                    }
                }else if("iconback".equals(name)){
                    iconbackResId = getAttrDrawableResId(pkg, res, parser, null, iconResNames);
                }else if("iconupon".equals(name)){
                    iconuponResId = getAttrDrawableResId(pkg, res, parser, null, iconResNames);
                }else if("iconmask".equals(name)){
                    iconmaskResId = getAttrDrawableResId(pkg, res, parser, null, iconResNames);
                }else if("item".equals(name)){
                    String component = parser.getAttributeValue(null, "component");
                    int resId = getAttrDrawableResId(pkg, res, parser, "drawable", iconResNames);
                    if(!TextUtils.isEmpty(component) && resId != 0){
                        if (component.startsWith("ComponentInfo{")) {
                            ComponentName cn = ComponentName.unflattenFromString(component.substring(14, component.length() - 1));
                            if(cn != null){
                                iconPacks.put(cn, resId);
                            }
                        }else if(component.startsWith(":")){
                            String tag = component.substring(0, component.length());
                            aliasComponets.put(tag, resId);
                        }
                    }
                }else if("calendar".equals(name)){//日历标签
                    String component = parser.getAttributeValue(null, "component");
                    String prefix = parser.getAttributeValue(null, "prefix");
                    if(!TextUtils.isEmpty(component) && !TextUtils.isEmpty(component)){
                        SparseIntArray calIds = new SparseIntArray(31);
                        boolean validCal = true;
                        for (int i = 0; i < 31; i++) {
                            String resName = prefix+String.valueOf(i + 1);
                            int id = res.getIdentifier(resName, "drawable", pkg);
                            calIds.put(i, id);
                            if(id != 0){
                                iconResNames.put(id, resName);
                            }else{
                                validCal = false;
                                break;
                            }
                        }
                        if (validCal && component.startsWith("ComponentInfo{")) {
                            ComponentName cn = ComponentName.unflattenFromString(component.substring(14, component.length() - 1));
                            if(cn != null){
                                calendarPacks.put(cn, calIds);
                            }
                        }else if(validCal && ":CALENDAR".equals(name)){
                            calendarIcons = calIds;
                        }
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IconPackTheme theme = packTheme;
        theme.mVersionCode = versionCode;
        theme.mResources = res;
        theme.mPackageName = pkg;
        theme.iconbackResId = iconbackResId;
        theme.iconuponResId = iconuponResId;
        theme.iconmaskResId = iconmaskResId;
        theme.scale = scale;
        theme.iconPacks = iconPacks;
        theme.calendarPacks = calendarPacks;
        theme.iconResNames = iconResNames;

        extendIconPacks(context, iconPacks, calendarPacks, calendarIcons, aliasComponets);

    }

    private static void extendIconPacks(@NonNull Context context,
                                        @NonNull Map<ComponentName, Integer> iconPacks,
                                        @NonNull Map<ComponentName, SparseIntArray> calendarPacks,
                                        @Nullable SparseIntArray calendarIcons,
                                        @NonNull ArrayMap<String, Integer> aliasComponets) {
        ArrayMap<String, ArrayList<ComponentName>> aliasCns = new ArrayMap<>();
        try {
            XmlPullParser parser = context.getResources().getXml(R.xml.icon_pack_extention);
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT){
                if(event != XmlPullParser.START_TAG){
                    event = parser.next();
                    continue;
                }
                String name = parser.getName();
                if("cns".equals(name)){
                    String alias = parser.getAttributeValue(null, "name");
                    String cnstr = parser.getAttributeValue(null, "cn");
                    ComponentName cn = TextUtils.isEmpty(cnstr) ? null : ComponentName.unflattenFromString(cnstr);
                    if(!TextUtils.isEmpty(alias) && cn != null){
                        ArrayList<ComponentName> cns = aliasCns.get(alias);
                        if(cns == null){
                            cns = new ArrayList<>();
                            aliasCns.put(alias, cns);
                        }
                        cns.add(cn);
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(aliasCns.isEmpty()){
            return;
        }
        ArrayList<ComponentName> tempCns = new ArrayList<>();
        for (Map.Entry<String, ArrayList<ComponentName>> entry : aliasCns.entrySet()) {//等价处理
            String name = entry.getKey();
            tempCns.clear();
            ArrayList<ComponentName> cns = entry.getValue();
            if("CALENDAR".equals(name)){
                SparseIntArray calIds = null;
                for (ComponentName cn : cns) {
                    if(calendarPacks.containsKey(cn)){
                        if(calIds == null){
                            calIds = calendarPacks.get(cn);
                        }
                    }else{
                        tempCns.add(cn);
                    }
                }
                if(calendarIcons != null){
                    calIds = calendarIcons;
                }
                if(calIds != null){
                    for (ComponentName cn : tempCns) {
                        calendarPacks.put(cn, calIds);
                    }
                    continue;
                }
                //如果没有动态图片，就继续找静态图片
            }
            int resId = 0;
            for (ComponentName cn : cns) {//找到一组Component中没有图标的
                Integer resVal = iconPacks.get(cn);
                if(resVal != null && resVal != 0){
                    if(resId == 0){//只取第一张图片
                        resId = resVal.intValue();
                    }
                }else{
                    tempCns.add(cn);
                }
            }
            Integer aliasRes = aliasComponets.get(name);
            if(aliasRes != null && aliasRes != 0){
                resId = aliasRes;
            }
            for (ComponentName cn : tempCns) {
                iconPacks.put(cn, resId);
            }
        }

    }

    private static int getAttrDrawableResId(@NonNull String pkg, @NonNull Resources res,
                                     @NonNull XmlPullParser parser,
                                     @Nullable String name, @NonNull SparseArray<String> iconResNames){
        String value;
        if(TextUtils.isEmpty(name)){
            value = parser.getAttributeValue(0);
        }else{
            value = parser.getAttributeValue(null, name);
        }
        if(!TextUtils.isEmpty(value)){
            int index = indexOfVaue(iconResNames, value);
            int resId = 0;
            if(index != -1){
                resId = iconResNames.keyAt(index);
            }else{
                resId = res.getIdentifier(value, "drawable", pkg);
                Log.d(TAG, "getAttrDrawableResId: name="+name+", value="+value+", resId="+resId);
                if(resId != 0){
                    iconResNames.put(resId, value);
                }
            }
            return resId;
        }
        return 0;
    }

    private static int indexOfVaue(@NonNull SparseArray<String> iconResNames, String value){
        int size = iconResNames.size();
        for (int i = 0; i < size; i++) {
            if(value == null){
                if(iconResNames.valueAt(i) == null){
                    return i;
                }
            }else{
                if(value.equals(iconResNames.valueAt(i))){
                    return i;
                }
            }
        }
        return -1;
    }


    public static ArrayList<AppInfo> queryAllIconPackApps(@NonNull Context context){
        PackageManager pm = context.getPackageManager();
        ArrayList<AppInfo> appInfos = new ArrayList<>();
        loadAppInfo(pm, appInfos, new Intent("com.novalauncher.THEME"));
        loadAppInfo(pm, appInfos, new Intent("com.gau.go.launcherex.theme"));
        loadAppInfo(pm, appInfos, new Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME"));
        loadAppInfo(pm, appInfos, new Intent("org.adw.launcher.icons.ACTION_PICK_ICON"));
        Collections.sort(appInfos);
        return appInfos;
    }

    public static ArrayList<AppInfo> loadAppInfo(@NonNull PackageManager pm,@NonNull ArrayList<AppInfo> appInfos,
                                          @NonNull Intent intent){
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkg = resolveInfo.activityInfo.packageName;
            if(!resolveInfo.activityInfo.exported || contains(appInfos, pkg)){
                continue;
            }
            String name = resolveInfo.activityInfo.loadLabel(pm).toString();
            Drawable icon = resolveInfo.activityInfo.loadIcon(pm);
            int versionCode = 0;
            try {
                versionCode = pm.getPackageInfo(pkg, 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appInfos.add(new AppInfo(name, pkg, versionCode, icon));
        }
        L.d("doBusiness apps="+appInfos);
        return appInfos;
    }

    public static boolean contains(List<AppInfo> list, String pkg){
        for (AppInfo appInfo : list) {
            if(TextUtils.equals(appInfo.pkg, pkg)){
                return true;
            }
        }
        return false;
    }


    public void turnToAppMarket(@NonNull Context context){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://search?q=icon pack")); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "您的系统中没有安装应用市场", Toast.LENGTH_SHORT).show();
        }
    }

    private static Bitmap maskBitmap(@NonNull Bitmap src, @NonNull Bitmap mask){
        int width = Math.min(src.getWidth(), mask.getWidth());
        int height = Math.min(src.getHeight(),mask.getHeight());

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);
        final Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        Rect maskDst = new Rect(0, 0, width, height);
        Rect maskSrc = new Rect(0, 0, src.getWidth(), src.getHeight());
        canvas.drawBitmap(src, maskSrc, maskDst, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        maskSrc.set(0, 0, mask.getWidth(), mask.getHeight());
        canvas.drawBitmap(mask, maskSrc, maskDst,paint);
        canvas.setBitmap(null);
        return output;
    }

    private static Bitmap centerBlend(Bitmap src, Bitmap back, float scale) {
        int width = back.getWidth();
        int height = back.getHeight();
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);
        final Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        canvas.drawBitmap(back,0, 0, paint);
        Rect maskSrc = new Rect(0, 0, src.getWidth(), src.getHeight());
        int sw = (int) (width * scale);
        int sh = (int) (height * scale);
        int dx = (width - sw) / 2;
        int dy = (height - sh) / 2;
        Rect maskDst = new Rect(dx, dy, dx + sw, dy + sh);
        canvas.drawBitmap(src, maskSrc, maskDst, paint);
        canvas.setBitmap(null);
        return output;
    }

    public static Bitmap getThirdPartApp(@NonNull Drawable drawable, @NonNull IconPackTheme theme){
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        final Bitmap output = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        drawable.draw(canvas);
        canvas.setBitmap(null);
        return getThirdPartApp(output, theme);
    }

    public static Bitmap getThirdPartApp(@NonNull Bitmap bitmap, @NonNull IconPackTheme theme){
        if(bitmap.isRecycled()) return null;
        Bitmap iconBack = theme.getIconBack();
        Bitmap iconMask = theme.getIconMask();
        Bitmap iconUpon = theme.getIconUpon();
        float scale = theme.scale;

        Bitmap result = bitmap;
        if(iconMask != null){
            Bitmap temp = maskBitmap(result, iconMask);
            result.recycle();
            result = temp;
        }
        if(iconBack != null){
            Bitmap temp = centerBlend(result, iconBack, scale);
            result.recycle();
            result = temp;
        }
        if(iconUpon != null){
            Bitmap temp = centerBlend(iconUpon, result, 1.0f);
            result.recycle();
            result = temp;
        }
        return result;
    }


}
