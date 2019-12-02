package com.tory.netexe;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author tory
 * @date 2019/3/28
 * @des:
 */
public class Utils {
    private static final String TAG = "Utils";
    public final static String CHARSET_NAME = "UTF-8";
    public static final String EMPTY = "";

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                Log.e(TAG, "closeSilently", e);
            }
        }
    }


    public static File getCacheDir(@NonNull Context context, String dir) {
        File cacheFile = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                ? context.getExternalCacheDir() : context.getCacheDir();
        if (TextUtils.isEmpty(dir)) {
            return cacheFile;
        } else {
            File newFile = new File(cacheFile, dir);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            return newFile;
        }
    }


    public static String toJson(Object obj){
        return new Gson().toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> tClass){
        try {
            return new Gson().fromJson(json, tClass);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static <T> List<T> fromJsonList(@Nullable String json, @NonNull Class<T> clazz){
        if (json == null){
            return null;
        }
        try {
            Type type = new ParameterizedTypeImpl(clazz);
            return new Gson().fromJson(json, type);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "fromJsonList: json="+json+", clazz="+clazz, e);
        }
        return null;
    }


    public static String readString(InputStream in) {
        String str = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writeStream(in, out);
            str = new String(out.toByteArray(), CHARSET_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void writeStream(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) {
            return;
        }
        // 定义缓冲区
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = in.read(buf)) != -1) { // 循环读取出入流中的内容，并写入输出流，直到输入流末尾
            out.write(buf, 0, len);
        }
        out.flush();
    }


    private static class ParameterizedTypeImpl implements ParameterizedType {
        Class clazz;

        public ParameterizedTypeImpl(Class clz) {
            clazz = clz;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{clazz};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
