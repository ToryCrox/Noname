package com.tory.noname.bili.apis;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.tory.noname.utils.L;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by tao.xu2 on 2016/10/13.
 */

public class ItemsConverterFactory extends Converter.Factory {

    private static final String TAG = "ItemsConverterFactory";

    ItemsParser parser;
    Class clazz;

    public ItemsConverterFactory(ItemsParser parser, Class clazz) {
        this.parser = parser;
        this.clazz = clazz;
    }

    public static ItemsConverterFactory create(ItemsParser parser, Class clazz){
        return new ItemsConverterFactory(parser,clazz);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new ItemsConverter();
    }

    public static interface ItemsParser{
        public String parse(String result);
    }

    class ItemsConverter implements Converter<ResponseBody, List>{

        @Override
        public List convert(ResponseBody value) throws IOException {
            List list = null;
            String result = value.string();
            if (!TextUtils.isEmpty(result)) {
                try {
                    String dataStr = parser.parse(result);
                    try {
                        list = JSONObject.parseArray(dataStr, clazz);
                    } catch (Exception e) {
                        L.w(e.toString());
                    }
                } catch (Exception e) {
                    L.d(TAG, "parseData error  result");
                    Log.e(TAG, "" + e.toString());

                }

            }
            L.d(TAG, "ItemsConverter convert list="+list);
            if (list == null) list = new ArrayList<>();
            return list;
        }
    }
}
