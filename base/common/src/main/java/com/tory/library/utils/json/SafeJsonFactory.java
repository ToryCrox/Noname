package com.tory.library.utils.json;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

/**
 * Gson解析安全校验, 目前只根据类型进行事先判断, 如果是像String转Int这种软件错误，需要先读取值才行，需要的修改对应的TypeAdapter才行
 */
public class SafeJsonFactory implements TypeAdapterFactory {

    private boolean isSafeCheck = false;

    public SafeJsonFactory(boolean isSafeCheck) {
        this.isSafeCheck = isSafeCheck;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        final Class<? super T> rawType = type.getRawType();

        return new TypeAdapter<T>() {
            /** The delegate is lazily created because it may not be needed, and creating it may
             * fail. */
            private TypeAdapter<T> delegate;

            @Override
            public T read(JsonReader in) throws IOException {
                try {
                    final JsonToken token = in.peek();
                    if (isSafeCheck && !checkType(token, type, delegate())) {
                        //校验类型失败，过该值
                        if (token == JsonToken.NULL) {
                            in.nextNull();
                        } else {
                            in.skipValue();
                            waring("Warning!!! Expected an "
                                + rawType.getName()
                                + " but was "
                                + token);
                        }
                        return null;
                    } else {
                        if (token == JsonToken.STRING && PrimitiveHelper.isPrimitiveNumber(
                            rawType)) {
                            String value = in.nextString();
                            if (TextUtils.isEmpty(value)) {
                                waring("Warning!!! Expected an " + rawType.getName()
                                    + " but was empty String");
                                return null;
                            }
                            try {
                                T t = (T) parseNumber(value, rawType);
                                return t;
                            } catch (Exception e) {
                                waring("Warning can not parse number rawType:"
                                    + rawType
                                    + ", value:"
                                    + value);
                                return null;
                            }
                        }
                    }
                    return delegate().read(in);
                } catch (Exception e) {
                    if (e instanceof DuGsonException || isNetWorkError(e)) {
                        throw e;
                    } else {
                        throw new DuGsonException("SafeJsonFactory read " +
                            (rawType != null ? rawType.toString() : ""), e);
                    }
                }
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate().write(out, value);
            }

            private TypeAdapter<T> delegate() {
                TypeAdapter<T> d = delegate;
                return d != null ? d
                    : (delegate = gson.getDelegateAdapter(SafeJsonFactory.this, type));
            }
        };
    }

    private static void waring(String msg) {
        GsonHelper.waring(msg);
    }

    /**
     * 校验值是否合法，不合法，需要跳过，保正安全
     *
     * @return true 表示类型检查正确
     * @throws IOException
     */
    private <T> boolean checkType(JsonToken token,
        TypeToken<T> typeToken, TypeAdapter<T> delegate) {
        if (token == JsonToken.NULL) {
            return false;
        }

        if (delegate instanceof ReflectiveTypeAdapterFactory.Adapter) { //对象解析
            return token == JsonToken.BEGIN_OBJECT;
        }

        final Class<? super T> rawType = typeToken.getRawType();
        Type type = typeToken.getType();
        boolean isArray = type instanceof GenericArrayType
            || type instanceof Class && ((Class<?>) type).isArray();
        if (isArray || Collection.class.isAssignableFrom(rawType)) { //集合类型
            return token == JsonToken.BEGIN_ARRAY; //只有为Array时才合法
        }

        if (PrimitiveHelper.isPrimitive(rawType)) { //基本类型判断
            return token != JsonToken.BEGIN_OBJECT && token != JsonToken.BEGIN_ARRAY;
        }

        return true;
    }

    private static <T> T parseNumber(String value, Class<T> rawType) {
        if (int.class.isAssignableFrom(rawType)
            || Integer.class.isAssignableFrom(rawType)) {
            return (T) Integer.valueOf(value);
        } else if (float.class.isAssignableFrom(rawType)
            || Float.class.isAssignableFrom(rawType)) {
            return (T) Float.valueOf(value);
        } else if (double.class.isAssignableFrom(rawType)
            || Double.class.isAssignableFrom(rawType)) {
            return (T) Double.valueOf(value);
        } else if (long.class.isAssignableFrom(rawType)
            || Long.class.isAssignableFrom(rawType)) {
            return (T) Long.valueOf(value);
        } else if (byte.class.isAssignableFrom(rawType)
            || Byte.class.isAssignableFrom(rawType)) {
            return (T) Byte.valueOf(value);
        } else if (short.class.isAssignableFrom(rawType)
            || Short.class.isAssignableFrom(rawType)) {
            return (T) Short.valueOf(value);
        } else {
            throw new NumberFormatException(
                GsonHelper.TAG + "parseNumber rawType: " + rawType + ", value: " + value);
        }
    }

    public static boolean isNetWorkError(Throwable error) {
        if (error instanceof MalformedJsonException || error instanceof EOFException) {
            return false;
        } else if (error instanceof HttpException) {
            return true;
        } else if (error instanceof TimeoutException) {
            return true;
        } else
            return error instanceof IOException;
    }
}
