package com.tory.noname.bili.apis;

import com.tory.noname.bili.bean.PartitionVideoInfo;
import com.tory.noname.bili.bean.RankVideoItem;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by tao.xu2 on 2016/10/13.
 */

public interface BiliApiService {

    @GET("archive_rank/getarchiverankbypartion")
    Observable<PartitionVideoInfo> getVideoByPartion(@QueryMap Map<String,String> params);


    @GET("index/rank/{type}-{rang}-{cid}.json")
    Call<List<RankVideoItem>> getRankItems(@Path("type") String type, @Path("rang") int rang, @Path("cid") int cid);

}
