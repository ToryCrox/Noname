package com.aleaf.viplist;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author tory
 * @date 2018/12/26
 * @des:
 */
public class VipProfileUtils {

    public static List<VipViewProfile> sVipViewProfiles;


    public static List<VipViewProfile> getVipViewProfiles(@NonNull Context context) {
        if (sVipViewProfiles == null) {
            ArrayList<VipViewProfile> list = new ArrayList<>();
            TypedArray array = context.getResources().obtainTypedArray(R.array.vip_view_profiles);
            int size = array.length();
            for (int i = 0; i < size; i += 8) {
                int level = array.getInteger(i, 0);
                int starColor = array.getColor(i + 1, 0);
                int endColor = array.getColor(i + 2, 0);
                int shadowColor = array.getColor(i + 3, 0);
                int drawableRes = array.getResourceId(i + 4, 0);
                int headerMaskColor = array.getColor(i + 5, 0);
                int iconColorTint = array.getColor(i + 6, 0);
                int iconTextTint = array.getColor(i + 7, 0);
                list.add(new VipViewProfile(level, starColor, endColor, shadowColor, drawableRes,
                        headerMaskColor, iconColorTint, iconTextTint));
            }
            array.recycle();
            sVipViewProfiles = Collections.unmodifiableList(list);
        }
        return sVipViewProfiles;
    }

    public static VipViewProfile getLevelVipViewProfile(@NonNull Context context, int level) {
        List<VipViewProfile> list = getVipViewProfiles(context);
        for (VipViewProfile profile : list) {
            if (profile.level == level) {
                return profile;
            }
        }
        return null;
    }


}
