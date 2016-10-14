package com.tory.noname.bili;

import android.content.Context;

import com.tory.noname.MApplication;
import com.tory.noname.utils.SpHelper;

/**
 * @Author: Tory
 * Create: 2016/9/23
 * Update: 2016/9/23
 */
public class BiliSetting {
    public static final String SHARED_PATH = "bili_shared";

    private static final String SP_KEY_RANK_RANGE = "sp_key_rank_range";
    public static final int DEFAULT_RANK_RANGE = 3;

    private static BiliSetting sInstance;

    private SpHelper mSpHelper;


    private BiliSetting(Context context){
        mSpHelper = SpHelper.newInstance(context,SHARED_PATH);
    }

    public static BiliSetting getInstance(){
        if(sInstance == null){
            synchronized (SpHelper.class){
                sInstance = new BiliSetting(MApplication.getInstance());
            }
        }
        return sInstance;
    }

    public void setRankRage(int rankRage){
        mSpHelper.putInt(SP_KEY_RANK_RANGE,rankRage);
    }

    public int getRankRage(){
        int rankRange = mSpHelper.getInt(SP_KEY_RANK_RANGE);
        rankRange = rankRange == 0 ? DEFAULT_RANK_RANGE : rankRange;
        return rankRange;
    }

}
