package com.tory.noname.bili.bgmlist;

import com.alibaba.fastjson.JSONObject;
import com.tory.library.utils.AppUtils;
import com.tory.library.utils.FileUtils;
import com.tory.library.utils.Md5Util;
import com.tory.library.utils.SpHelper;
import com.tory.noname.main.utils.L;
import com.tory.noname.main.utils.http.XOkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @Author: Tory
 * Create: 2016/10/1
 * Update: ${UPDATE}
 */
public class BgmPresenter {

    public static BgmPresenter sInstance;
    private List<BgmItem> mList = new ArrayList<>();

    public static final String SP_WEEKDAY_FITER = "sp_weekday_fiter";


    private BgmPresenter() {

    }

    public static BgmPresenter getInstance() {
        if (sInstance == null) {
            synchronized (BgmPresenter.class) {
                sInstance = new BgmPresenter();
            }
        }
        return sInstance;
    }

    private Set<OnBgmlistLoadCompeletListener> listeners = new HashSet<>();

    public void saveFilterState(boolean mWeekDayFiter) {
        SpHelper.getInstance(AppUtils.getContext()).put(SP_WEEKDAY_FITER, mWeekDayFiter);
    }

    public boolean getFilterState() {
        return SpHelper.getInstance(AppUtils.getContext()).getBoolean(SP_WEEKDAY_FITER);
    }

    public interface OnBgmlistLoadCompeletListener {
        void onLoadCompelte(List<BgmItem> list);
    }

    public void addLoadListener(OnBgmlistLoadCompeletListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        if (!mList.isEmpty()) {
            listener.onLoadCompelte(mList);
        }
    }

    public void removeListener(OnBgmlistLoadCompeletListener listener) {
        listeners.remove(listener);
    }

    public void loadData(boolean foreceLoad) {

        L.d("loadData start");
        BgmService.Apis.createArchivesObservalbe()
                .getArchives()
                .flatMap(archives -> {
                        String url = archives.resoveLatestArchive().path;
                        L.d("loadData url="+url);
                        return BgmService.Apis
                                .createArchivesObservalbe(url)
                                .getBgmItems(url);
                    }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list ->{
                    mList.clear();
                    mList.addAll(list);
                    onLoadComplete();
                });
    }

    public List<BgmItem> getBgmList() {
        return mList;
    }


    public void tearDown() {
        XOkHttpUtils.getInstance().cancelTag(this);
        mList.clear();
        mSitelist.clear();
    }

    private String obtainFromCache() {
        try {
            return FileUtils.readString(getCacheFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeToCache(String result) {
        try {
            FileUtils.writeString(result, getCacheFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCacheFile() {
        return FileUtils.getCacheDir(AppUtils.getContext())
                + File.separator + Md5Util.digest(getUrl());
    }

    private void onLoadComplete() {
        L.d("onLoadComplete:" + mList);
        for (OnBgmlistLoadCompeletListener listener : listeners) {
            listener.onLoadCompelte(mList);
        }
    }

    private List<BgmItem> parseResult(String result) {
        List<BgmItem> list = new ArrayList<>();
        try {
            JSONObject json = JSONObject.parseObject(result);
            Set<String> keySet = json.keySet();
            for (String key : keySet) {
                BgmItem item = json.getObject(key, BgmItem.class);
                item.siteNames = findSite(item.onAirSite);
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getUrl() {
        return "https://bgmlist.com/tempapi/bangumi/2017/1/json";
    }


    public class Site {

        public String regular;
        public String name;
        public Pattern pattern;
        public String url;

        public Site(String url){
        }

        public Site(String regular, String name) {
            this.regular = regular;
            this.name = name;
            pattern = Pattern.compile(regular);
        }

        public boolean match(String url) {
            return pattern != null && pattern.matcher(url).find();
        }
    }


    List<Site> mSitelist = new ArrayList<>();

    public void initSiteList() {
        if (!mSitelist.isEmpty()) return;
        List<Site> sitelist = new ArrayList<>();
        sitelist.add(new Site("acfun\\.(tv|tudou)", "A"));
        sitelist.add(new Site("bilibili\\.com", "B站"));
        sitelist.add(new Site("tucao\\.(tv|cc)", "C站"));
        sitelist.add(new Site("sohu\\.com", "搜狐"));
        sitelist.add(new Site("youku\\.com", "优酷"));
        sitelist.add(new Site("qq\\.com", "腾讯"));
        sitelist.add(new Site("iqiyi\\.com", "爱奇艺"));
        sitelist.add(new Site("(le|letv)\\.com", "乐视"));
        sitelist.add(new Site("pptv\\.com", "PPTV"));
        sitelist.add(new Site("tudou\\.com", "土豆"));
        sitelist.add(new Site("kankan\\.com", "迅雷"));
        sitelist.add(new Site("mgtv\\.com", "芒果"));
        mSitelist.addAll(sitelist);
    }

    public List<String> findSite(List<String> airSites) {
        if (airSites == null || airSites.isEmpty()) return  new ArrayList<>();
        initSiteList();
        List<String> siteNames = new ArrayList<>();
        for (String airSite : airSites) {
            for (Site site : mSitelist) {
                if (site.match(airSite)) {
                    siteNames.add(site.name);;
                    break;
                }
            }
        }
        return siteNames;

    }

}
