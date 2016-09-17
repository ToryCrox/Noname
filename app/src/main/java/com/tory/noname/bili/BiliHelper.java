package com.tory.noname.bili;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.alibaba.fastjson.JSONObject;
import com.tory.noname.bili.bean.BiliCate;
import com.tory.noname.utils.FileUtils;

import java.io.InputStream;
import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/9/17
 * Update: 2016/9/17
 */
public class BiliHelper {



    public static String getUrlFromAid(int aid){
        return BiliApis.BASE_URL + "/video/av"+aid+"/";
    }

    public static ComponentName handleComponetName(){
        return new ComponentName(BiliApis.CLIENT_PACKE_NAME,BiliApis.CLIENT_HANDLE_INTENT);
    }

    public static void openInBili(Context context,String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(biliClientExists(context)){
            intent.setComponent(handleComponetName());
        }
        context.startActivity(intent);
    }

    public static boolean biliClientExists(Context context){
        Intent intent = new Intent();
        intent.setComponent(handleComponetName());
        return context.getPackageManager().resolveActivity(intent,0) != null;
    }

    /**
     * 重构cate
     * @return
     */
    public static BiliCate buildCate(Context context){
        try {
            InputStream inputStream = context.getAssets().open("bilicat.json");
            String str = FileUtils.readString(inputStream);
            JSONObject resultObj = JSONObject.parseObject(str).getJSONObject("result");
            List<BiliCate> rootList = JSONObject.parseArray(resultObj.getString("root"),BiliCate.class);
            JSONObject childObj = resultObj.getJSONObject("child");
            for(BiliCate biliCate : rootList){
                String tid = String.valueOf(biliCate.tid);
                if(!childObj.keySet().contains(tid)) continue;
                List<BiliCate> child = JSONObject.parseArray(childObj.getString(tid),BiliCate.class);
                fileParentCate(biliCate,child);
            }

            BiliCate rootCate = new BiliCate();
            rootCate.tid = 0;
            fileParentCate(rootCate,rootList);
            return rootCate;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void fileParentCate(BiliCate parent,List<BiliCate> child){
        parent.child = child;
        for (BiliCate cate : child){
            cate.parent = parent;
        }
    }

}
