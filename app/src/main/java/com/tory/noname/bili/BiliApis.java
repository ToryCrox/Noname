package com.tory.noname.bili;

/**
 * @Author: Tory
 * Create: 2016/9/17
 * Update: 2016/9/17
 */
public class BiliApis {

    public static final String CLIENT_PACKE_NAME = "tv.danmaku.bili";
    public static final String CLIENT_HANDLE_INTENT = "tv.danmaku.bili.ui.IntentHandlerActivity";

    public static String BASE_URL = "http://www.bilibili.com";

    public static String BASE_RANK_URL = BASE_URL + "/index/rank/";
    /**全站-全站*/
    public static final String RANK_ALL = "/index/rank/all-3-0.json";
    public static final String RANK_BANGUMI = "/index/rank/all-3-33.json";
    /**
     *
     *连载动画最新
     * http://api.bilibili.com/archive_rank/getarchiverankbypartion?callback=jQuery17206667210870638649_1474099779991&type=jsonp&tid=33&pn=1&_=1474099780284
     */
}
