package com.tory.noname.bili.bean;

import java.util.List;

/**
 * @Author: tory
 * Create: 2017/4/9
 */
public class RankVideoInfo {


    /**
     *
     * "rank":{
     *     list :{
     *         rankvideoitems
     *     },
     *     note: "",
     *     num : 111,
     *     page : 1
     * }
     *
     */

    public RankItem rank;

    public static class RankItem {
        public List<RankVideoItem> list;
        public String note;
        public int num;
        public int page;
    }

}
