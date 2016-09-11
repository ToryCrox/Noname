package com.tory.noname.utils;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: ${UPDATE}
 */
public class Constance {

    public interface Apis {
        //以下api来自http://gank.io/api

        //分类数据:  http://gank.io/api/data/数据类型/请求个数/第几页
        //数据类型： 福利 | Android | iOS | 休息视频 | 拓展资源 | 前端 | all
        //example:  http://gank.io/api/data/Android/10/1

        public static String GanHuo = "http://gank.io/api/data";

        // 图片
        public static final String IMAGES_URL = "http://api.laifudao.com/open/tupian.json";

        // 天气预报url
        public static final String WEATHER = "http://wthrcdn.etouch.cn/weather_mini?city=";

        //百度定位
        public static final String INTERFACE_LOCATION = "http://api.map.baidu.com/geocoder";
    }
}
