package com.tory.noname.bili.bgmlist;

import com.alibaba.fastjson.JSONObject;
import com.tory.noname.utils.L;
import com.tory.noname.utils.http.XOkHttpUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * @Author: tory
 * Create: 2017/3/26
 * Update: 2017/3/26
 *
 * http://blog.csdn.net/evan_man/article/details/51320408
 */
public interface BgmService {


    @GET("archive.json")
    Observable<ArchiveResult> getArchives();


    @GET
    Observable<List<BgmItem>> getBgmItems(@Url String url);


    public static class Apis{

        static String BASE_URL = "https://bgmlist.com/tempapi/";

        public static  BgmService createArchivesObservalbe(){
            Retrofit retrofit =new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            return retrofit.create(BgmService.class);
        }

        public static BgmService createArchivesObservalbe(String url){
            Retrofit retrofit =new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(XOkHttpUtils.getInstance().getOkHttpClient())
                    .addConverterFactory(new BgmItemsFactory())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            return retrofit.create(BgmService.class);
        }
    }

    static class BgmItemsFactory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return new BgmItemsCover();
        }

        private class BgmItemsCover implements Converter<ResponseBody, List<BgmItem>> {
            @Override
            public List<BgmItem> convert(ResponseBody value) throws IOException {
                List<BgmItem> list = new ArrayList<>();
                try {
                    String result = value.string();
                    L.d("","BgmItemsCover convert result="+result);
                    JSONObject json = JSONObject.parseObject(result);
                    Set<String> keySet = json.keySet();
                    for (String key : keySet) {
                        BgmItem item = json.getObject(key, BgmItem.class);
                        item.siteNames = BgmPresenter.getInstance().findSite(item.onAirSite);
                        list.add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }
        }
    }
}
