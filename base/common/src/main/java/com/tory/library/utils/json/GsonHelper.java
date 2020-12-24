package com.tory.library.utils.json;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tory.library.log.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GsonHelper {

    static final String TAG = "GsonHelper";

    public static final TypeAdapterFactory BOOLEAN_FACTORY
            = TypeAdapters.newFactory(boolean.class, Boolean.class, GsonJsonAdapters.JSON_BOOLEAN);
    public static final TypeAdapterFactory INTEGER_FACTORY
            = TypeAdapters.newFactory(int.class, Integer.class, GsonJsonAdapters.JSON_INTEGER);
    public static final TypeAdapterFactory LONG_FACTORY
            = TypeAdapters.newFactory(long.class, Long.class, GsonJsonAdapters.JSON_LONG);
    public static final TypeAdapterFactory FLOAT_FACTORY
            = TypeAdapters.newFactory(float.class, Float.class, GsonJsonAdapters.JSON_FLOAT);
    public static final TypeAdapterFactory DOUBLE_FACTORY
            = TypeAdapters.newFactory(double.class, Double.class, GsonJsonAdapters.JSON_DOUBLE);

    private static volatile Gson sGson = null;

    /**
     * 获取单例Gson对象
     */
    @NonNull
    public static Gson getGson() {
        if (sGson == null) {
            synchronized (GsonHelper.class) {
                if (sGson == null) {
                    sGson = create();
                }
            }
        }
        return sGson;
    }

    @NonNull
    public static Gson create() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(String.class, GsonJsonAdapters.JSON_STRING)
                .registerTypeAdapter(JSONObject.class, GsonJsonAdapters.JSON_OBJECT)
                .registerTypeAdapter(JSONArray.class, GsonJsonAdapters.JSON_ARRAY)
                .registerTypeAdapterFactory(BOOLEAN_FACTORY)
                .registerTypeAdapterFactory(INTEGER_FACTORY)
                .registerTypeAdapterFactory(LONG_FACTORY)
                .registerTypeAdapterFactory(FLOAT_FACTORY)
                .registerTypeAdapterFactory(DOUBLE_FACTORY)
                .registerTypeAdapterFactory(new SafeJsonFactory(true))//放到后面，优先级会比较高
                .addDeserializationExclusionStrategy(GsonExclusionStrategy.DESERIALIZATION)
                .addSerializationExclusionStrategy(GsonExclusionStrategy.SERIALIZATION)
                .create();
    }

    /**
     * 直接转string, 如果为null，则返回空字符串，如果为float或者double，但是为整数，则会去掉小数据点
     *
     * @param obj
     * @return
     */
    @NonNull
    public static String valueOf(@Nullable Object obj) {
        if (obj instanceof Float || obj instanceof Double) {
            Number number = (Number) obj;
            if (number.doubleValue() == number.longValue()) {
                return String.valueOf(number.longValue());
            }
        }
        if (obj == null) {
            return "";
        } else {
            return String.valueOf(obj);
        }
    }

    /**
     * 转成json字符串，如果是String或者，基本类型，会直接String.valueOf，安全，不用try catch
     */
    @Nullable
    public static String toJson(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        if (obj instanceof CharSequence || PrimitiveHelper.isPrimitive(clazz)) {
            return valueOf(obj);
        }
        try {
            return getGson().toJson(obj);
        } catch (Exception e) {
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("class", clazz.toString());
            extraData.put("json", obj.toString());
            //BusinessHelper.uploadError(TAG + "_toJson", e, extraData);
            bug(e, "_toJson");
            return null;
        }
    }

    @NonNull
    public static String toJsonNonNull(@Nullable Object obj) {
        String json = toJson(obj);
        if (json == null) {
            return "";
        }
        return json;
    }

    /**
     * 反序列化为对象，如果目标为String，会直接返回
     * 该方法安全，不用try cache!!!!
     */
    @Nullable
    public static <T> T fromJson(@Nullable String json, @NonNull Class<T> clazz) {
        return fromJson(json, clazz, TAG + "_fromJson");
    }

    @Nullable
    public static <T> T fromJson(@Nullable String json, @NonNull Class<T> clazz,
                                 @NonNull String section) {
        if (json == null) {
            return null;
        }
        if (String.class.isAssignableFrom(clazz)) {
            return (T) json;
        }
        try {
            return getGson().fromJson(json, clazz);
        } catch (Exception e) {
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("class", clazz.toString());
            extraData.put("json", json);
            bug(e, "fromJson " + extraData);
            //BusinessHelper.uploadError(section, e, extraData);
            return null;
        }
    }

    /**
     * 可以用做 自定义数据类型
     * 如 : Map<>
     * <p>
     * GsonHelper.fromJson(popString, object : TypeToken<Map<String, PopLayerConfigModel>>() {}
     * .type)
     */
    @Nullable
    public static <T> T fromJson(@Nullable String json, @NonNull Type type) {
        return fromJson(json, type, TAG + "_fromJson");
    }

    @Nullable
    public static <T> T fromJson(@Nullable String json, @NonNull Type type,
                                 @NonNull String section) {
        if (json == null) {
            return null;
        }
        try {
            return getGson().fromJson(json, type);
        } catch (Exception e) {
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("class", type.toString());
            extraData.put("json", json);
            bug(e, "fromJson " + extraData);
            //BusinessHelper.uploadError(section, e, extraData);
            return null;
        }
    }

    /**
     * 反序列化为List
     * 该方法安全，不用try cache!!!!
     */
    @Nullable
    public static <T> List<T> fromJsonList(@Nullable String json, @NonNull Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return getGson().fromJson(json, getListType(clazz));
        } catch (Exception e) {
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("class", clazz.toString());
            extraData.put("json", json);
            bug(e, "fromJsonList " + extraData);
            return null;
        }
    }

    @NonNull
    public static <T> Type getListType(@NonNull Class<T> clazz) {
        return new ParameterizedTypeImpl(clazz);
    }

    public static <K, V> Type getMapType(@NonNull Class<K> kClazz, @NonNull Class<V> vClass) {
        return TypeToken.getParameterized(Map.class, kClazz, vClass).getType();
    }

    public static Type getParameterized(Type rawType, Type... typeArguments) {
        return TypeToken.getParameterized(rawType, typeArguments).getType();
    }


    static void waring(String msg) {
        LogUtils.w(TAG, " " + msg);
    }

    static void info(String msg) {
        LogUtils.d(TAG, " " + msg);
    }

    static void bug(Throwable e, String msg, Object... args) {
        LogUtils.e(TAG, String.format(msg, args));
    }

    static void bug(Throwable e, JsonReader reader, String msg, Object... args) {
        if (reader instanceof DuJsonReader) {
            DuJsonReader duJsonReader = (DuJsonReader) reader;
            byte[] bytes = duJsonReader.getBytes();
            String jsonData = bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);

            Map<String, Object> extraData = new HashMap<>();
            Annotation[] annotations = duJsonReader.getNetAnnotations();
            if (annotations != null && annotations.length > 0) {
                StringBuilder extraAnnotation = new StringBuilder();
                for (Annotation annotation : annotations) {
                    extraAnnotation.append(annotation.toString());
                }
                extraData.put("request_method_url", extraAnnotation);
            }
            extraData.put("data_json", jsonData);
            bug(e, "_error" + extraData);
        } else {
            LogUtils.e(TAG, msg, e);
        }
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        private Class<?> clazz;

        public ParameterizedTypeImpl(Class<?> clz) {
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
