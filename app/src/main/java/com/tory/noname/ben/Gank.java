package com.tory.noname.ben;

/**
 * Created by tao.xu2 on 2016/9/12.
 */
public class Gank {

    /**
     * _id : 5714449667765974f5e27da7
     * createdAt : 2016-04-18T10:21:10.634Z
     * desc : Android 实现图片圆角显示的几种方式
     * publishedAt : 2016-04-18T12:05:28.120Z
     * source : web
     * type : Android
     * url : http://gavinliu.cn/2016/04/12/Android-实现图片圆角显示的几种方式/
     * used : true
     * who : null
     */

    public String _id;
    public String createdAt;
    public String desc;
    public String publishedAt;
    public String source;
    public String type;
    public String url;
    public boolean used;
    public String who;

    @Override
    public String toString() {
        return "Gank{" +
                "_id='" + _id + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", desc='" + desc + '\'' +
                ", publishedAt='" + publishedAt + '\'' +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", used=" + used +
                ", who='" + who + '\'' +
                '}';
    }
}
