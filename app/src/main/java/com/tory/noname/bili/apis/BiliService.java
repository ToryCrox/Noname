package com.tory.noname.bili.apis;

import com.tory.noname.bili.bean.VideoItem;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by tao.xu2 on 2016/10/13.
 */

public interface BiliService {

    @GET("archive_rank/getarchiverankbypartion")
    Call<List<VideoItem>> getVideoByPartion(@QueryMap Map<String,String> params);
}
