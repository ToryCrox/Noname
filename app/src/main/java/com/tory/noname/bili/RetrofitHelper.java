package com.tory.noname.bili;

import com.tory.noname.bili.apis.ApiConstants;
import com.tory.noname.bili.apis.BangumiService;
import com.tory.noname.bili.apis.BiliApiService;
import com.tory.noname.bili.apis.FastJsonConverterFactory;
import com.tory.noname.bili.apis.RankService;
import com.tory.noname.gank.apis.GankApiConstance;
import com.tory.noname.gank.apis.GankApiService;
import com.tory.noname.main.utils.http.XOkHttpUtils;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * @Author: tory
 * Create: 2017/4/9
 */
public class RetrofitHelper {


    public static RankService createBiliRankService(){
        return createApi(RankService.class, ApiConstants.RANK_BASE_URL);
    }

    public static BiliApiService createBiliApiService(){
        return createApi(BiliApiService.class, ApiConstants.API_BASE_URL);
    }


    public static BangumiService createBangumiService(){
        return createApi(BangumiService.class, ApiConstants.BANGUMI_BASE_URL);
    }

    public static GankApiService createGankApiService(){
        return createApi(GankApiService.class, GankApiConstance.BASE_URL);
    }

    /**
     * 根据传入的baseUrl，和api创建retrofit
     */
    private static <T> T createApi(Class<T> clazz, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(XOkHttpUtils.getInstance().getOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(FastJsonConverterFactory.create())
                .build();

        return retrofit.create(clazz);
    }
}
