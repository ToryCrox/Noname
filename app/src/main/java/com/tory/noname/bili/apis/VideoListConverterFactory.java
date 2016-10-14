package com.tory.noname.bili.apis;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.tory.noname.bili.bean.VideoItem;
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

public class VideoListConverterFactory extends Converter.Factory {

    private static final String TAG = "VideoListConverter";

    public static VideoListConverterFactory create(){
        return new VideoListConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new VideoListConverter();
    }

    class VideoListConverter implements Converter<ResponseBody, List<VideoItem>>{

        @Override
        public List<VideoItem> convert(ResponseBody value) throws IOException {
            List<VideoItem> list = null;
            String result = value.string();
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject jsonObj = JSONObject.parseObject(result);
                    int code = jsonObj.getIntValue("code");
                    if (code == 0) {
                        JSONObject dataObj = jsonObj.getJSONObject("data");
                        try {
                            list = JSONObject.parseArray(dataObj.getString("archives"), VideoItem.class);
                        } catch (Exception e) {
                            L.w(e.toString());
                        }
                    } else {
                        L.w(TAG, " return code error:" + code + "result:" + result);
                    }
                } catch (Exception e) {
                    L.d(TAG, "parseData error  result");
                    Log.e(TAG, "" + e.toString());

                }

            }
            L.d(TAG, "VideoListConverter convert");
            if (list == null) list = new ArrayList<>();
            return list;
        }
    }
}
