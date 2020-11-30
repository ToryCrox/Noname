package com.tory.library.utils.json;

import android.os.Parcelable;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.io.Serializable;

class GsonExclusionStrategy {

    static boolean isIgnore(FieldAttributes f) {
        return f.getAnnotation(GsonIgnore.class) != null;
    }

    /**
     * 忽略反序列化字段
     */
    static ExclusionStrategy DESERIALIZATION = new ExclusionStrategy() {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            if (isIgnore(f)) {
                GsonHelper.info("shouldSkipField deserialization fieldName:" + f.getName());
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            if (clazz == Serializable.class || clazz == Parcelable.class) {
                GsonHelper.info("shouldSkipClass " + clazz);
                return true;
            }
            return false;
        }
    };

    /**
     * 忽略序列化字段
     */
    static ExclusionStrategy SERIALIZATION = new ExclusionStrategy() {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            if (isIgnore(f)) {
                GsonHelper.info("shouldSkipField serialization fieldName:" + f.getName());
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };
}