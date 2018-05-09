package com.tory.noname.bili.bgmlist;

import com.tory.noname.utils.L;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * @Author: tory
 * Create: 2017/8/26
 * Update: ${UPDATE}
 */
public class ArchiveResult {

    public Map<Integer, Map<Integer, Archive>> data;

    public Map<Integer, Map<Integer, Archive>> getData() {
        return data;
    }

    public void setData(Map<Integer, Map<Integer, Archive>> data) {
        this.data = data;
    }


    private static class KeyComparator implements Comparator<String>{

        @Override
        public int compare(String o1, String o2) {
            return Integer.parseInt(o1) - Integer.parseInt(o1);
        }
    }

    public Archive resoveLatestArchive(){
        L.d("resoveLatestArchive data="+data);
        Integer year = Collections.max(data.keySet());
        Map<Integer, Archive> quarterData = data.get(year);
        Integer quarter = Collections.max(quarterData.keySet());
        return quarterData.get(quarter);
    }

    @Override
    public String toString() {
        return "ArchiveResult{" +
                "data=" + data +
                '}';
    }

    /**
     *
     * {
     "data": {
     "2013": {
     "10": {
     "path": "https://bgmlist.com/tempapi/bangumi/2013/10/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     }
     },
     "2014": {
     "1": {
     "path": "https://bgmlist.com/tempapi/bangumi/2014/1/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "4": {
     "path": "https://bgmlist.com/tempapi/bangumi/2014/4/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "7": {
     "path": "https://bgmlist.com/tempapi/bangumi/2014/7/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "10": {
     "path": "https://bgmlist.com/tempapi/bangumi/2014/10/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     }
     },
     "2015": {
     "1": {
     "path": "https://bgmlist.com/tempapi/bangumi/2015/1/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "4": {
     "path": "https://bgmlist.com/tempapi/bangumi/2015/4/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "7": {
     "path": "https://bgmlist.com/tempapi/bangumi/2015/7/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "10": {
     "path": "https://bgmlist.com/tempapi/bangumi/2015/10/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     }
     },
     "2016": {
     "1": {
     "path": "https://bgmlist.com/tempapi/bangumi/2016/1/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "4": {
     "path": "https://bgmlist.com/tempapi/bangumi/2016/4/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "7": {
     "path": "https://bgmlist.com/tempapi/bangumi/2016/7/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "10": {
     "path": "https://bgmlist.com/tempapi/bangumi/2016/10/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     }
     },
     "2017": {
     "1": {
     "path": "https://bgmlist.com/tempapi/bangumi/2017/1/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "4": {
     "path": "https://bgmlist.com/tempapi/bangumi/2017/4/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     },
     "7": {
     "path": "https://bgmlist.com/tempapi/bangumi/2017/7/json",
     "version": "a0fb6d167753e28a9e6250090ecf8841"
     }
     }
     }
     }
     *
     *
     */
}
