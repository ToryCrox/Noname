package com.tory.noname.bili.apis;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @Author: tory
 * Create: 2017/4/9
 */
public class FastJsonConverterFactory  extends Converter.Factory {

    public static FastJsonConverterFactory create(){
        return new FastJsonConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new FastJsonConverter<>(type);
    }

    private static class FastJsonConverter<T> implements Converter<ResponseBody, T> {

        private Type type;

        public FastJsonConverter(Type type) {
            this.type = type;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            String result = value.string();
            return JSONObject.parseObject(result,type);
        }
    }
}
