package com.tory.noname.bili;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.alibaba.fastjson.JSONObject;
import com.tory.noname.MApplication;
import com.tory.noname.R;
import com.tory.noname.bili.apis.BiliApis;
import com.tory.noname.bili.bean.CategoryMeta;
import com.tory.library.utils.FileUtils;
import com.tory.noname.utils.L;
import com.tory.library.utils.Md5Util;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Tory
 * Create: 2016/9/17
 * Update: 2016/9/17
 */
public class BiliHelper {


    public static CategoryMeta sRootCateMeta;
    public static SparseIntArray sCoverResources;

    public static String getUrlFromAid(int aid){
        return BiliApis.BASE_URL + "/video/av"+aid+"/";
    }

    public static ComponentName handleComponetName(){
        return new ComponentName(BiliApis.CLIENT_PACKE_NAME,BiliApis.CLIENT_HANDLE_INTENT);
    }

    /**
     * 打开bilibili
     * @param context
     * @param url
     */
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
    public static CategoryMeta buildCate(Context context){
        if(sRootCateMeta != null){
            return sRootCateMeta;
        }

        try {
            InputStream inputStream = context.getAssets().open("bilicat.json");
            String str = FileUtils.readString(inputStream);
            JSONObject resultObj = JSONObject.parseObject(str).getJSONObject("result");
            List<CategoryMeta> rootList = JSONObject.parseArray(resultObj.getString("root"),CategoryMeta.class);
            JSONObject childObj = resultObj.getJSONObject("child");
            for(CategoryMeta biliCate : rootList){
                String tid = String.valueOf(biliCate.tid);
                if(!childObj.keySet().contains(tid)) continue;
                List<CategoryMeta> child = JSONObject.parseArray(childObj.getString(tid),CategoryMeta.class);
                fileParentCate(biliCate,child);
            }

            CategoryMeta rootCate = new CategoryMeta();
            rootCate.tid = 0;
            fileParentCate(rootCate,rootList);
            sRootCateMeta = rootCate;
            return rootCate;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void fileParentCate(CategoryMeta parent, List<CategoryMeta> child){
        parent.child = child;
        SparseIntArray coverRes = coverResources();
        for (CategoryMeta cate : child){
            cate.parent = parent;
            cate.coverRes = coverRes.get(cate.tid);
        }
    }

    public static SparseIntArray coverResources(){
        if(sCoverResources != null) return sCoverResources;
        SparseIntArray array = new SparseIntArray(20);
        array.put(65537, R.drawable.ic_category_live);
        array.put(1,R.drawable.ic_category_t1);
        array.put(3,R.drawable.ic_category_t3);
        array.put(4,R.drawable.ic_category_t4);
        array.put(5,R.drawable.ic_category_t5);
        array.put(11,R.drawable.ic_category_t11);
        array.put(13,R.drawable.ic_category_t13);
        array.put(23,R.drawable.ic_category_t23);
        array.put(33,R.drawable.ic_category_t33);
        array.put(36,R.drawable.ic_category_t36);
        array.put(119,R.drawable.ic_category_t119);
        array.put(129,R.drawable.ic_category_t129);
        array.put(155,R.drawable.ic_category_t155);
        sCoverResources = array;
        return array;
    }


    public static CategoryMeta getCategoryInfo(int tid){
        CategoryMeta root = buildCate(MApplication.getInstance());
        List<CategoryMeta> cates = root.child;
        for(CategoryMeta cate: cates){
            if(cate.tid == tid){
                return cate;
            }
        }
        throw new RuntimeException("no CategoryMeta matched tid:"+tid);
    }

    public static String sign(Map<String,String> data,String appkey){
        Set<String> keySet = data.keySet();
        String[] keyStrs = new String[keySet.size()];
        keySet.toArray(keyStrs);
        Arrays.sort(keyStrs);
        StringBuilder sb = new StringBuilder();

        try {
            for (String key : keyStrs){
                String val = data.get(key);
                sb.append("&").append(key).append("=").append(URLEncoder.encode(val, "UTF-8"));
            }
        } catch (Exception e){
            L.d(e.getMessage());
        }
        sb.deleteCharAt(0);
        return Md5Util.digest(sb.append(appkey).toString());
    }


    public static String formatNumber(String num) {
        if(TextUtils.isDigitsOnly(num)){
            return formatNumber(Integer.parseInt(num));
        }
        return num;
    }

    public static String formatNumber(int num) {
        if (num > 10000) {
            return String.format("%.1f万", (num * 1.0f / 10000));
        } else {
            return String.valueOf(num);
        }
    }

}
