package com.tory.demo.diskcache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.google.android.material.tabs.TabLayout;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/10
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/10 xutao 1.0
 * Why & What is modified:
 */
class CacheUtil {

    static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    static <T> T fromJson(@NonNull String value, @NonNull Class<T> clazz) {
        if (clazz == String.class){
            return (T) value;
        }
        return JSON.parseObject(value, clazz);
    }

    static String toJson(@NonNull Object obj) {
        if (obj instanceof String){
            return (String) obj;
        } else {
            return JSON.toJSONString(obj);
        }
    }

    private static final char HEXS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String MD5 = "MD5";

    public static String md5(@NonNull String str) {
        try {
            MessageDigest alga = MessageDigest.getInstance(MD5);
            alga.update(str.getBytes());

            byte[] digesta = alga.digest();
            String ls_str = byte2hex(digesta);

            return ls_str;
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("没有这个加密算法请检查JDK版本");
        }
        return str;
    }

    /**
     * 字节数组转换成16进制字符串
     *
     * @param bytes 输入字节数组
     * @return 16进制字符串（小写）
     */
    private static String byte2hex(byte[] bytes) {
        StringBuffer sBuffer = new StringBuffer();
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = (bytes[i] >>> 4) & 0x0F;
            sBuffer.append(HEXS[temp]);
            temp = bytes[i] & 0x0F;
            sBuffer.append(HEXS[temp]);
        }
        return sBuffer.toString();
    }
}
