package com.tory.noname.bili.bean;

import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/9/17
 * Update: 2016/9/17
 */
public class BiliCate {


    public int tid;
    public String typename;
    public String captionname;

    public BiliCate parent;
    public List<BiliCate> child;

    @Override
    public String toString() {
        return "BiliCate{" +
                "tid=" + tid +
                ", typename='" + typename + '\'' +
                ", captionname='" + captionname + '\'' +
                ", parent.tid=" + (parent != null ? parent.tid : -1)+
                ", child=" + child +
                '}';
    }
}
