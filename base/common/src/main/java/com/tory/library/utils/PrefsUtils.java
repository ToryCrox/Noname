package com.tory.library.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tencent.mmkv.MMKV;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tory
 * @date 2019/5/20
 * @des:
 */
public class PrefsUtils {
    private static final String CRYPT_NULL_KEY = null;
    private static boolean sIsInit = false;
    private static SharedPreferences sDefault;

    private static void ensureInit(@NonNull Context context){
        if (!sIsInit){
            sIsInit = true;
            MMKV.initialize(context);
        }
        return;
    }

    public static SharedPreferences withMmkv(String mmapID, int mode){
        return MMKV.mmkvWithID(mmapID, mode);
    }


    public static SharedPreferences with(@NonNull Context context, String mmapID){
        ensureInit(context);
        return withMmkv(mmapID, MMKV.SINGLE_PROCESS_MODE);
    }

    public static SharedPreferences withMultiProcess(@NonNull Context context, String mmapID){
        ensureInit(context);
        return withMmkv(mmapID, MMKV.MULTI_PROCESS_MODE);
    }

    public static Map<String, String> getAll(@NonNull SharedPreferences prefs){
        Map<String, String> map = new HashMap<>();
        if (prefs instanceof MMKV){
            MMKV mmkv = (MMKV) prefs;
            String[] keys = mmkv.allKeys();
            for (String key : keys) {
                map.put(key, String.valueOf(getObjectValue(mmkv, key)));
            }
        } else {
            Map<String, ?> all = prefs.getAll();
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                map.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return map;
    }

    public static String getValue(SharedPreferences prefs, String key){
        if (prefs instanceof MMKV){
            return String.valueOf(getObjectValue((MMKV) prefs, key));
        } else {
            return String.valueOf(prefs.getAll().get(key));
        }
    }

    private static Object getObjectValue(MMKV mmkv, String key) {
        // 因为其他基础类型value会读成空字符串,所以不是空字符串即为string or string-set类型
        String value = mmkv.decodeString(key);
        if (!TextUtils.isEmpty(value)) {
            // 判断 string or string-set
            if (value.charAt(0) == 0x01) {
                return mmkv.decodeStringSet(key);
            } else {
                return value;
            }
        }
        // float double类型可通过string-set配合判断
        // 通过数据分析可以看到类型为float或double时string类型为空字符串且string-set类型读出空数组
        // 最后判断float为0或NAN的时候可以直接读成double类型,否则读float类型
        // 该判断方法对于非常小的double类型数据 (0d < value <= 1.0569021313E-314) 不生效
        Set<String> set = mmkv.decodeStringSet(key);
        if (set != null && set.size() == 0) {
            Float valueFloat = mmkv.decodeFloat(key);
            Double valueDouble = mmkv.decodeDouble(key);
            if (Float.compare(valueFloat, 0f) == 0 || Float.compare(valueFloat, Float.NaN) == 0) {
                return valueDouble;
            } else {
                return valueFloat;
            }
        }
        // int long bool 类型的处理放在一起, int类型1和0等价于bool类型true和false
        // 判断long或int类型时, 如果数据长度超出int的最大长度, 则long与int读出的数据不等, 可确定为long类型
        int valueInt = mmkv.decodeInt(key);
        long valueLong = mmkv.decodeLong(key);
        if (valueInt != valueLong) {
            return valueLong;
        } else {
            return valueInt;
        }
    }

}
