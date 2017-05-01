package com.tory.noname.bili.bean;

import java.util.List;

/**
 * @Author: tory
 * Create: 2017/4/10
 * Update: ${UPDATE}
 */
public class HotVideoInfo {

    /**
     *
     */

    public HotItem hot;

    public static class HotItem{
        public int code;
        public String note;
        public int num;
        public int pages;
        public List<VideoItem> list;

        @Override
        public String toString() {
            return "HotItem{" +
                    "code=" + code +
                    ", note='" + note + '\'' +
                    ", num=" + num +
                    ", pages=" + pages +
                    ", list=" + list +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "HotVideoInfo{" +
                "hot=" + hot +
                '}';
    }
}
