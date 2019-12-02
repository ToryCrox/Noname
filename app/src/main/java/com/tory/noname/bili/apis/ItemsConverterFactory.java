package com.tory.noname.bili.apis;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.tory.library.reflect.ReflectionUtil;
import com.tory.noname.main.utils.L;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
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

    public ItemsConverterFactory(ItemsParser parser) {
        this.parser = parser;
    }

    public static ItemsConverterFactory create(ItemsParser parser){
        return new ItemsConverterFactory(parser);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new ItemsConverter(type);
    }

    public static interface ItemsParser{
        public String parse(String result);
    }

    /**
     * http://www.cnblogs.com/whitewolf/p/4355541.html
     */
    class ItemsConverter implements Converter<ResponseBody, List>{
        Type type;
        public ItemsConverter(Type type){
            this.type = type;
        }

        @Override
        public List convert(ResponseBody value) throws IOException {
            String result = value.string();
            List list = null;
            if (!TextUtils.isEmpty(result)) {
                try {
                    String dataStr = parser.parse(result);
                    if(ParameterizedType.class.isAssignableFrom(type.getClass())){
                        ParameterizedType superclassType = ((ParameterizedType)type);
                        Type parmeType = superclassType.getActualTypeArguments()[0];
                        list =  JSONObject.parseArray(dataStr, ReflectionUtil.getClass(parmeType));
                    }else{
                        L.e(TAG, "parseData error result type="+type);
                    }
                } catch (Exception e) {
                    L.e(TAG, "parseData error  result type="+type,e);

                }

            }
            L.d(TAG, "ItemsConverter convert list="+list);
            if (list == null) list = new ArrayList<>();
            return list;
        }
    }
}
