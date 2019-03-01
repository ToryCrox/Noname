package com.aleaf.dbdemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author tory
 * @date 2019/2/22
 * @des:
 */
public class DbManager {


    private static DbManager sInstance;

    public static DbManager getInstance(Context context){
        if (sInstance == null){
            synchronized (DbManager.class){
                if (sInstance == null){
                    sInstance = new DbManager(context);
                }
            }
        }
        return sInstance;
    }

    private DaoSession mSesstion;
    public DbManager(Context context) {
        initGreenDao(context);
    }

    private void initGreenDao(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context.getApplicationContext(), "aserbao.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mSesstion = daoMaster.newSession();
    }

}
