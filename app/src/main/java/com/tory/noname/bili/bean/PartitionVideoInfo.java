package com.tory.noname.bili.bean;

import java.util.List;

/**
 * @Author: tory
 * Create: 2017/4/9
 *
 */
public class PartitionVideoInfo {


    /**
     * {
     *     "code":0,
     *     "messge":""
     *     "data":{
     *         "archives":{
     *
     *         }
     *         "page"{
     *
     *         }
     *     }
     * }
     */

    public int code;
    public String message;
    public DataItem data;



    public static class DataItem{
        public List<VideoItem> archives;
        public PageItem page;
    }

    public static class PageItem{
        public int count;
        public int num;
        public int size;
    }
}
