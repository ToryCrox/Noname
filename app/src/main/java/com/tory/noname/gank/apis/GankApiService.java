package com.tory.noname.gank.apis;

import com.tory.noname.gank.bean.GankApiResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * @author tory
 * @date 2018/3/21
 */

public interface GankApiService {

    @GET("{tag}/{pageCount}/{pageIndex}")
    Observable<GankApiResult> getGankApiResult(@Path("tag") String tag, @Path("pageCount") int pageCount,
                                               @Path("pageIndex") int pageIndex);
}
