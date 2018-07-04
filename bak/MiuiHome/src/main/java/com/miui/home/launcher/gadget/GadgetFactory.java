package com.miui.home.launcher.gadget;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.miui.home.R;
import com.miui.home.launcher.DeviceConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import miui.util.MiuiFeatureUtils;

public class GadgetFactory {
    private static final Integer[] GADGET_ID_LIST = new Integer[]{Integer.valueOf(12), Integer.valueOf(6), Integer.valueOf(5)};
    private static HashMap<Integer, GadgetInfo> sMtzGadgetList = null;

    public static String getGadgetDir(Context context) {
        String name = "gadget";
        return context.getDir("gadget", DeviceConfig.TEMP_SHARE_MODE_FOR_WORLD_READABLE).getAbsolutePath();
    }

    private static void loadMtzGadgetList() {
        if (sMtzGadgetList == null) {
            sMtzGadgetList = new HashMap();
            File gadgets = new File("/system/media/theme/default/gadgets");
            if (gadgets.isDirectory()) {
                File[] arr$ = gadgets.listFiles();
                int len$ = arr$.length;
                int i$ = 0;
                int idCounter = 1000;
                while (i$ < len$) {
                    int idCounter2;
                    File file = arr$[i$];
                    if (file.getName().endsWith(".mtz")) {
                        idCounter2 = idCounter + 1;
                        GadgetInfo info = new GadgetInfo(idCounter);
                        if (info.loadMtzGadgetFromUri(Uri.fromFile(file))) {
                            sMtzGadgetList.put(Integer.valueOf(info.getGadgetId()), info);
                        }
                    } else {
                        idCounter2 = idCounter;
                    }
                    i$++;
                    idCounter = idCounter2;
                }
            }
        }
    }

    public static void resetMtzGadgetList() {
        sMtzGadgetList = null;
    }

    public static final int[] getGadgetIdList(Context context) {
        return getGadgetIdList(context, false);
    }

    public static final int[] getGadgetIdList(Context context, boolean iconStyleOnly) {
        if (iconStyleOnly) {
            return new int[]{12};
        }
        ArrayList<Integer> gadgetIds = new ArrayList();
        Collections.addAll(gadgetIds, GADGET_ID_LIST);
        loadMtzGadgetList();
        int[] ids = new int[(gadgetIds.size() + sMtzGadgetList.size())];
        int index = 0;
        Iterator i$ = gadgetIds.iterator();
        while (i$.hasNext()) {
            int index2 = index + 1;
            ids[index] = ((Integer) i$.next()).intValue();
            index = index2;
        }
        for (Integer i : sMtzGadgetList.keySet()) {
            index2 = index + 1;
            ids[index] = i.intValue();
            index = index2;
        }
        return ids;
    }

    public static GadgetInfo getInfo(int id) {
        switch (id) {
            case 4:
                return new GadgetInfo(id, 2, 1, R.string.gadget_clock_12_label, R.drawable.gadget_clock_12_icon, -1, 2);
            case 5:
                return new GadgetInfo(id, 2, 2, R.string.gadget_clock_22_label, R.drawable.gadget_clock_22_icon, -1, 2);
            case 6:
                return new GadgetInfo(id, 4, 2, R.string.gadget_clock_24_label, R.drawable.gadget_clock_24_icon, -1, 2);
            case 12:
                return new GadgetInfo(id, 1, 1, R.string.gadget_clear_button_label, R.drawable.gadget_clear_button_icon, -1, 0);
            case 13:
                return new GadgetInfo(id, 4, 1, R.string.gadget_google_search_label, R.drawable.gadget_google_search_icon, R.drawable.gadget_google_search_preview, 5);
            case 14:
                return new GadgetInfo(id, 1, 1, R.string.gadget_power_clear_button_label, R.drawable.power_clear_button_icon, -1, 0);
            default:
                if (id < 1000) {
                    return null;
                }
                if (sMtzGadgetList == null || !sMtzGadgetList.containsKey(Integer.valueOf(id))) {
                    return new GadgetInfo(id);
                }
                return ((GadgetInfo) sMtzGadgetList.get(Integer.valueOf(id))).clone();
        }
    }

    public static Gadget createGadget(Context activity, GadgetInfo info, int requestCode) {
        View gadget = null;
        switch (info.getGadgetId()) {
            case 4:
            case 5:
            case 6:
                gadget = new ClockGadgetDelegate(activity, requestCode);
                break;
            case 12:
                gadget = new ClearButton(activity);
                break;
            case 13:
                gadget = new GoogleSearch(activity);
                break;
            case 14:
                if (MiuiFeatureUtils.isLocalFeatureSupported(activity, "support_power_clean", true)) {
                    gadget = new PowerClearButton(activity);
                    break;
                }
                break;
            default:
                if (info.isMtzGadget()) {
                    gadget = new MtzGadget(activity, info);
                    break;
                }
                break;
        }
        if (gadget != null) {
            gadget.setTag(info);
        }
        return (Gadget) gadget;
    }

    public static long getGadgetItemId(Bundle bundle) {
        long id = bundle.getLong("callback_id", -1);
        if (id == -1) {
            try {
                id = Long.valueOf(bundle.getString("RESPONSE_TRACK_ID")).longValue();
            } catch (NumberFormatException e) {
            }
        }
        return id;
    }

    public static void updateGadgetBackup(Context context) {
        ClockGadgetDelegate.updateBackup(context);
    }
}
