package com.miui.home.launcher.upsidescene.data;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.miui.home.launcher.Launcher;
import com.miui.home.launcher.ShortcutInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import miui.app.ToggleManager;
import miui.security.SecurityManager;

public class Function {
    int mType;
    UrlData mUrlData;

    public static class AppFunction extends Function {
        protected AppFunction(UrlData urlData) {
            super(urlData, 1);
        }

        public ComponentName getComponentName() {
            if (TextUtils.isEmpty(this.mUrlData.data)) {
                return null;
            }
            return ComponentName.unflattenFromString(this.mUrlData.data);
        }

        public void setComponentName(ComponentName componentName) {
            this.mUrlData.data = componentName.flattenToString();
        }

        public boolean isShowIcon() {
            return Function.getBooleanFromParams("is_show_icon", this.mUrlData, true);
        }

        public boolean isShowTitle() {
            return Function.getBooleanFromParams("is_show_title", this.mUrlData, true);
        }
    }

    public static class FolderFunction extends Function {
        public FolderFunction(UrlData urlData) {
            super(urlData, 2);
        }

        public FolderFunction(UrlData urlData, int type) {
            super(urlData, type);
        }

        public String getFolderName() {
            String codedfolderName = (String) this.mUrlData.params.get("folder_name");
            if (TextUtils.isEmpty(codedfolderName)) {
                return "";
            }
            return Function.decode(codedfolderName);
        }

        public void setFolderName(String folderName) {
            this.mUrlData.params.put("folder_name", Function.encode(folderName));
        }

        public List<ComponentName> getComponentNames(Context context) {
            List<ComponentName> list = new ArrayList();
            if (this.mType == 9) {
                if (Launcher.isChildrenModeEnabled()) {
                    Intent mainIntent = new Intent("android.intent.action.MAIN", null);
                    mainIntent.addCategory("android.intent.category.LAUNCHER");
                    SecurityManager manager = (SecurityManager) context.getSystemService("security");
                    for (ResolveInfo resolveInfo : ActivityThread.currentApplication().getPackageManager().queryIntentActivities(mainIntent, 0)) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        if (manager.getApplicationChildrenControlEnabled(packageName)) {
                            list.add(new ComponentName(packageName, resolveInfo.activityInfo.name));
                        }
                    }
                }
            } else if (!TextUtils.isEmpty(this.mUrlData.data)) {
                for (String strComponentName : this.mUrlData.data.split("\\;")) {
                    list.add(ComponentName.unflattenFromString(strComponentName));
                }
            }
            return list;
        }

        public void setComponentNames(List<ComponentName> list) {
            StringBuilder sb = new StringBuilder();
            for (ComponentName componentName : list) {
                sb.append(componentName.flattenToShortString()).append(";");
            }
            if (list.size() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            this.mUrlData.data = sb.toString();
        }
    }

    public static class MtzGadgetFunction extends Function {
        protected MtzGadgetFunction(UrlData urlData) {
            super(urlData, 6);
            if (this.mUrlData != null && this.mUrlData.data != null) {
                this.mUrlData.data = this.mUrlData.data.replace('\\', '/');
            }
        }

        public String getMtzRelativePath() {
            return this.mUrlData.data;
        }

        public void setMtzRelativePath(String relativePath) {
            this.mUrlData.data = relativePath;
        }
    }

    public static class SystemGadgetFunction extends Function {
        protected SystemGadgetFunction(UrlData urlData) {
            super(urlData, 4);
        }

        public int getGadgetId() {
            if ("clear_button".equals(this.mUrlData.data)) {
                return 12;
            }
            if ("clock_1x2".equals(this.mUrlData.data)) {
                return 4;
            }
            if ("clock_2x2".equals(this.mUrlData.data)) {
                return 5;
            }
            if ("clock_2x4".equals(this.mUrlData.data)) {
                return 6;
            }
            if ("global_search".equals(this.mUrlData.data)) {
                return 3;
            }
            if ("player".equals(this.mUrlData.data)) {
                return 2;
            }
            return -1;
        }

        public int getId() {
            return Function.getIntFromParams("_id", this.mUrlData, 0);
        }

        public String getResourcePath() {
            String path = (String) this.mUrlData.params.get("resource_path");
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            return Function.decode(path);
        }

        public void setResourcePath(String path) {
            if (path == null) {
                path = "";
            }
            this.mUrlData.params.put("resource_path", Function.encode(path));
        }

        public void setGadgetInfo(int gadgetId, int id) {
            switch (gadgetId) {
                case 2:
                    this.mUrlData.data = "player";
                    break;
                case 3:
                    this.mUrlData.data = "global_search";
                    break;
                case 4:
                    this.mUrlData.data = "clock_1x2";
                    break;
                case 5:
                    this.mUrlData.data = "clock_2x2";
                    break;
                case 6:
                    this.mUrlData.data = "clock_2x4";
                    break;
                case 12:
                    this.mUrlData.data = "clear_button";
                    break;
                default:
                    throw new RuntimeException("unknown gadgetId:" + gadgetId);
            }
            this.mUrlData.params.put("_id", Integer.toString(id));
        }
    }

    public static class ToggleFunction extends Function {
        protected ToggleFunction(UrlData urlData) {
            super(urlData, 7);
        }

        public int getToggleId() {
            return ToggleManager.getToggleIdFromString(this.mUrlData.data);
        }

        public void setToggleId(int id) {
            this.mUrlData.data = ToggleManager.getToggleStringFromId(id);
        }

        public ShortcutInfo getShortcutInfo() {
            Intent intent = new Intent("com.miui.action.TOGGLE_SHURTCUT");
            intent.putExtra("ToggleId", getToggleId());
            new Intent().putExtra("android.intent.extra.shortcut.INTENT", intent);
            ShortcutInfo shortcutInfo = new ShortcutInfo();
            shortcutInfo.intent = intent;
            shortcutInfo.mIconType = 3;
            return shortcutInfo;
        }

        public boolean isShowIcon() {
            return Function.getBooleanFromParams("is_show_icon", this.mUrlData, true);
        }

        public boolean isShowTitle() {
            return Function.getBooleanFromParams("is_show_title", this.mUrlData, true);
        }
    }

    static class UrlData {
        String data;
        HashMap<String, String> params = new HashMap();
        String scheme;

        public UrlData(String scheme) {
            this.scheme = scheme;
        }

        public String toString() {
            if (TextUtils.isEmpty(this.scheme)) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(this.scheme);
            if (this.data != null) {
                sb.append(this.data);
            }
            if (this.params.size() > 0) {
                sb.append('?');
                for (Entry<String, String> keyValue : this.params.entrySet()) {
                    sb.append((String) keyValue.getKey()).append('=').append((String) keyValue.getValue()).append('|');
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }

        public static UrlData parse(String url) {
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            int colonIndex = url.indexOf(":");
            if (colonIndex == -1) {
                Log.e("FreeStyle.Function", "parse url failed. not found :. url:" + url);
                return null;
            }
            int questionMarkIndex = url.indexOf("?");
            UrlData urlData = new UrlData();
            urlData.scheme = url.substring(0, colonIndex + 1);
            if (colonIndex >= url.length() - 1) {
                urlData.data = "";
                return urlData;
            }
            int dataEnd;
            if (questionMarkIndex == -1) {
                dataEnd = url.length();
            } else {
                dataEnd = questionMarkIndex;
            }
            urlData.data = url.substring(colonIndex + 1, dataEnd);
            if (questionMarkIndex == -1 || questionMarkIndex == url.length() - 1) {
                return urlData;
            }
            for (String param : url.substring(questionMarkIndex + 1).split("\\|")) {
                String[] keyValue = param.split("\\=");
                if (keyValue.length == 1) {
                    urlData.params.put(keyValue[0], "");
                } else if (keyValue.length == 2) {
                    urlData.params.put(keyValue[0], keyValue[1]);
                }
            }
            return urlData;
        }
    }

    public static class WidgetFunction extends Function {
        protected WidgetFunction(UrlData urlData) {
            super(urlData, 5);
        }

        public ComponentName getProviderComponent() {
            return ComponentName.unflattenFromString(this.mUrlData.data);
        }

        public int getId() {
            return Function.getIntFromParams("_id", this.mUrlData, 0);
        }

        public void setId(int id) {
            this.mUrlData.params.put("_id", Integer.toString(id));
        }
    }

    protected Function(UrlData urlData, int type) {
        this.mUrlData = urlData;
        this.mType = type;
    }

    public static Function parse(String url) {
        UrlData urlData = UrlData.parse(url);
        if (urlData == null || TextUtils.isEmpty(urlData.scheme)) {
            return new Function(urlData, 0);
        }
        if ("app:".equals(urlData.scheme)) {
            return new AppFunction(urlData);
        }
        if ("folder:".equals(urlData.scheme)) {
            return new FolderFunction(urlData);
        }
        if ("drawer:".equals(urlData.scheme)) {
            return new Function(urlData, 3);
        }
        if ("children_folder:".equals(urlData.scheme)) {
            return new FolderFunction(urlData, 9);
        }
        if ("children_mode_exit:".equals(urlData.scheme)) {
            return new Function(urlData, 8);
        }
        if ("system_gadget:".equals(urlData.scheme)) {
            return new SystemGadgetFunction(urlData);
        }
        if ("widget:".equals(urlData.scheme)) {
            return new WidgetFunction(urlData);
        }
        if ("mtz_gadget:".equals(urlData.scheme)) {
            return new MtzGadgetFunction(urlData);
        }
        if ("toggle:".equals(urlData.scheme)) {
            return new ToggleFunction(urlData);
        }
        Log.w("FreeStyle.Function", "unknown function type:" + urlData.scheme);
        return new Function(urlData, 0);
    }

    public static Function createFunction(int type) {
        switch (type) {
            case 0:
                return new Function(null, 0);
            case 1:
                return new AppFunction(new UrlData("app:"));
            case 2:
                return new FolderFunction(new UrlData("folder:"));
            case 3:
                return new Function(new UrlData("drawer:"), 3);
            case 4:
                return new SystemGadgetFunction(new UrlData("system_gadget:"));
            case 5:
                return new WidgetFunction(new UrlData("widget:"));
            case 6:
                return new MtzGadgetFunction(new UrlData("mtz_gadget:"));
            case 7:
                return new ToggleFunction(new UrlData("toggle:"));
            case 8:
                return new Function(new UrlData("children_mode_exit:"), 8);
            case 9:
                return new FolderFunction(new UrlData("children_folder:"), 9);
            default:
                throw new RuntimeException("unknown function type:" + type);
        }
    }

    public int getType() {
        return this.mType;
    }

    public String toString() {
        return this.mUrlData == null ? "" : this.mUrlData.toString();
    }

    private static String encode(String input) {
        return Base64.encodeToString(input.getBytes(), 11);
    }

    private static String decode(String input) {
        return new String(Base64.decode(input, 8));
    }

    private static boolean getBooleanFromParams(String paramName, UrlData urlData, boolean defaultValue) {
        String strValue = (String) urlData.params.get(paramName);
        return TextUtils.isEmpty(strValue) ? defaultValue : Boolean.parseBoolean(strValue);
    }

    private static int getIntFromParams(String paramName, UrlData urlData, int defaultValue) {
        String strValue = (String) urlData.params.get(paramName);
        return TextUtils.isEmpty(strValue) ? defaultValue : Integer.parseInt(strValue);
    }
}
