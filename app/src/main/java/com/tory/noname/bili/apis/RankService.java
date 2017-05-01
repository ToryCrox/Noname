package com.tory.noname.bili.apis;

import com.tory.noname.bili.bean.RankVideoInfo;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface RankService {

  @GET("index/rank/{type}-{rang}-{cid}.json")
  Observable<RankVideoInfo> getRankItems(@Path("type") String type, @Path("rang") int rang, @Path("cid") int cid);


}