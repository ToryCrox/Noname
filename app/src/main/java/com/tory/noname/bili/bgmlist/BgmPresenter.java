package com.tory.noname.bili.bgmlist;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.tory.noname.MApplication;
import com.tory.noname.utils.FileUtils;
import com.tory.noname.utils.L;
import com.tory.noname.utils.Md5Util;
import com.tory.noname.utils.SpHelper;
import com.tory.noname.utils.http.XOkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Author: Tory
 * Create: 2016/10/1
 * Update: ${UPDATE}
 */
public class BgmPresenter {

    public static BgmPresenter sInstance;
    private List<BgmItem> mList = new ArrayList<>();

    public static final String SP_WEEKDAY_FITER = "sp_weekday_fiter";


    private BgmPresenter(){

    }

    public static BgmPresenter getInstance(){
        if(sInstance == null){
            synchronized (BgmPresenter.class){
                sInstance = new BgmPresenter();
            }
        }
        return sInstance;
    }

    private Set<OnBgmlistLoadCompeletListener> listeners = new HashSet<>();

    public void saveFilterState(boolean mWeekDayFiter) {
        SpHelper.getInstance(MApplication.getInstance()).put(SP_WEEKDAY_FITER,mWeekDayFiter);
    }
    public boolean getFilterState() {
        return SpHelper.getInstance(MApplication.getInstance()).getBoolean(SP_WEEKDAY_FITER);
    }

    public interface OnBgmlistLoadCompeletListener{
        void onLoadCompelte(List<BgmItem> list);
    }

    public void addLoadListener(OnBgmlistLoadCompeletListener listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
        if(!mList.isEmpty()){
            listener.onLoadCompelte(mList);
        }
    }

    public void removeListener(OnBgmlistLoadCompeletListener listener){
        listeners.remove(listener);
    }

    public void loadData(boolean foreceLoad){
        if(!foreceLoad ){
            if(mList == null){
                L.d("onLoadComplete: hasload!");
                onLoadComplete();
                return;
            }
            String result = obtainFromCache();//XOkHttpUtils.getInstance().getStringFromCatch(getUrl());
            if(!TextUtils.isEmpty(result)){
                L.d("onLoadComplete: load from catach!");
                mList.addAll(parseResult(result));
                onLoadComplete();
                return;
            }

        }

        XOkHttpUtils.get(getUrl())
                .tag(this)
                .execute(new XOkHttpUtils.HttpCallBack() {
                    @Override
                    public void onLoadStart() {

                    }

                    @Override
                    public void onSuccess(String result) {
                        mList.clear();
                        writeToCache(result);
                        mList.addAll(parseResult(result));
                        onLoadComplete();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    public List<BgmItem> getBgmList(){
        return mList;
    }


    public void tearDown(){
        XOkHttpUtils.getInstance().cancelTag(this);
        mList.clear();
        mSitelist.clear();
        mSitelist = null;
    }

    private String obtainFromCache(){
        try {
            return FileUtils.readString(getCacheFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeToCache(String result) {
        try {
            FileUtils.writeString(result,getCacheFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCacheFile() {
        return FileUtils.getCacheDir(MApplication.getInstance())
                +File.separator + Md5Util.digest(getUrl());
    }

    private void onLoadComplete() {
        L.d("onLoadComplete:"+mList);
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
                BgmItem item = json.getObject(key,BgmItem.class);
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getUrl() {
        return "http://bgmlist.com/json/bangumi-1610.json";
    }


    public class Site{

        public String regular;
        public String name;
        public Pattern pattern;
        public String url;

        public Site(String regular, String name) {
            this.regular = regular;
            this.name = name;
            pattern = Pattern.compile(regular);
        }

        public boolean match(String url){
            return pattern != null &&pattern.matcher(url).find();
        }
    }


    List<Site> mSitelist ;
    public void initSiteList() {
        if(mSitelist != null) return;
        List<Site> sitelist = new ArrayList<>();
        sitelist.add(new Site("acfun\\.(tv|tudou)","A"));
        sitelist.add(new Site("bilibili\\.com","B站"));
        sitelist.add(new Site("tucao\\.(tv|cc)","C站"));
        sitelist.add(new Site("sohu\\.com","搜狐"));
        sitelist.add(new Site("youku\\.com","优酷"));
        sitelist.add(new Site("qq\\.com","腾讯"));
        sitelist.add(new Site("iqiyi\\.com","爱奇艺"));
        sitelist.add(new Site("(le|letv)\\.com","乐视"));
        sitelist.add(new Site("pptv\\.com","PPTV"));
        sitelist.add(new Site("tudou\\.com","土豆"));
        sitelist.add(new Site("kankan\\.com","迅雷"));
        sitelist.add(new Site("mgtv\\.com","芒果"));
        mSitelist = sitelist;
    }

    public String[] findSite(List<String> airSites){
        if(airSites == null || airSites.isEmpty()) return new String [0];
        initSiteList();
        List<String> siteNames = new ArrayList<>();
        for (String airSite : airSites) {
            for (Site site : mSitelist) {
                if(site.match(airSite)){
                    siteNames.add(site.name);
                    break;
                }
            }
        }
        return siteNames.toArray(new String[siteNames.size()]);

    }

}
