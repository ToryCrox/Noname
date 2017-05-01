package com.tory.noname.bili.apis;

import com.tory.noname.bili.bean.HotVideoInfo;
import com.tory.noname.bili.bean.VideoItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * @Author: tory
 * Create: 2017/4/10
 * Update: ${UPDATE}
 */
public interface BangumiService {

    /**
     * http://bangumi.bilibili.com/jsonp/slideshow/34.ver
     */
    @GET("jsonp/slideshow/{cid}.ver")
    Call<List<VideoItem>> getBannerItems(@Path("cid") int cid);

    @GET("index/catalogy/{cid}-3day.json")
    Observable<HotVideoInfo> get3DayHotVideoInfos(@Path("cid") int cid);
}
