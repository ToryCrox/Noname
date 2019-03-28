package com.tory.library.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class SerializationHelper {
    private static final String TAG = "SerializationHelper";

    private static Gson gson = new Gson();

    @Nullable
    public static <T> T fromJson(@NonNull Context context, @RawRes int resID, Class<T> tClass) {
        return fromJson(context, resID, tClass, false);
    }

    @Nullable
    public static <T> T fromJson(@NonNull Context context, @RawRes int resID, Class<T> tClass, boolean decrypt) {
        Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(resID);
        try {
            String json;
            if (decrypt) {
                json = AESUtils.decryptStringFrom(inputStream);
            } else {
                json = FileUtils.readString(inputStream);
            }
            return fromJson(json, tClass);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(inputStream);
        }
        return null;
    }

    @Nullable
    public static <T> List<T> fromFileJsonList(@NonNull Context context, @RawRes int resID, Class<T> tClass) {
        return fromFileJsonList(context, resID, tClass, false);
    }

    @Nullable
    public static <T> List<T> fromFileJsonList(@NonNull Context context, @RawRes int resID, Class<T> tClass, boolean decrypt) {
        Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(resID);
        try {
            String json;
            if (decrypt) {
                json = AESUtils.decryptStringFrom(inputStream);
            } else {
                json = FileUtils.readString(inputStream);
            }
            return fromJsonList(json, tClass);
        } catch (IOException e) {
            Log.e(TAG, "jsonToBean: resID="+resID + ",tClass="+tClass, e);
        } finally {
            IoUtils.closeSilently(inputStream);
        }
        return null;
    }

    @Nullable
    public static <T> T formFileJson(String path, Class<T> tClass, boolean decrypt) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
            String json;
            if (decrypt) {
                json = AESUtils.decryptStringFrom(inputStream);
            } else {
                json = FileUtils.readString(inputStream);
            }
            return fromJson(json, tClass);
        } catch (Exception e) {
            Log.e(TAG, "jsonToBean: path="+path, e);
        } finally {
            IoUtils.closeSilently(inputStream);
        }
        return null;
    }

    @Nullable
    public static <T> List<T> fromFileJsonList(String path, Class<T> tClass) {
        return fromFileJsonList(path, tClass, false);
    }

    @Nullable
    public static <T> List<T> fromFileJsonList(String path, Class<T> tClass, boolean decrypt) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
            String json;
            if (decrypt) {
                json = AESUtils.decryptStringFrom(inputStream);
            } else {
                json = FileUtils.readString(inputStream);
            }
            return fromJsonList(json, tClass);
        } catch (IOException e) {
            Log.e(TAG, "fromFileJsonList: ", e);
        } finally {
            IoUtils.closeSilently(inputStream);
        }

        return null;
    }

    public static <T> T fromJson(String json, Class<T> tClass){
        try {
            return gson.fromJson(json, tClass);
        } catch (Exception e) {
            Log.e(TAG, "fromJson: ", e);
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
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "fromJsonList: json="+json+", clazz="+clazz, e);
        }
        return null;
    }

    public static String toJson(@Nullable Object obj){
        if (obj == null){
            return null;
        }
        return gson.toJson(obj);
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