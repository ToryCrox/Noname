package com.tory.noname;

import com.tory.noname.bili.BiliApis;
import com.tory.noname.utils.L;
import com.tory.noname.utils.Md5Util;

import org.junit.Test;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void formatNumber() throws Exception {
        System.out.println(String.format("%.1f万",1.254f));
        System.out.println("ss:"+ URLEncoder.encode("徐涛", "UTF-8"));
        getParams();
    }

    @Test
    public void testMatcher() throws Exception{
        Pattern p=Pattern.compile("(le|letv)\\.com");
        Matcher matcher = p.matcher("http://www.le.com/comic/5938.html");
        System.out.println("matcher1:"+matcher.find());
    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        //params.put("access_key", BiliApis.ACCESS_KEY);
        //params.put("_device", "android");
        //params.put("_hwid", "c475c83f9dfc26b7");
        //params.put("_ulv", "10000");
        //params.put("build", "426003");
        //params.put("mobi_app", "android");
        params.put("order", "default");
        //params.put("platform", "android");
        params.put("tid", 33 + "");
        params.put("page", 1 + "");
        params.put("pagesize", 20 + "");
        //params.put("appkey", BiliApis.APPKEY);
        String sign = sign(params,BiliApis.ACCESS_KEY);
        System.out.println("sign: "+sign);

        params.put("sign", sign);
        //params.put("ts", ""+ System.currentTimeMillis()/1000);
        return params;
    }

    public static String sign(Map<String,String> data,String appkey){
        Set<String> keySet = data.keySet();
        String[] keyStrs = new String[keySet.size()];
        keySet.toArray(keyStrs);
        Arrays.sort(keyStrs);
        StringBuilder sb = new StringBuilder();
        System.out.println("key:"+Arrays.toString(keyStrs));
        try {
            for (String key : keyStrs){
                String val = data.get(key);
                sb.append("&").append(key).append("=").append(URLEncoder.encode(val, "UTF-8"));
            }
        } catch (Exception e){
            L.d(e.getMessage());
        }
        sb.deleteCharAt(0);
        System.out.println("sb: "+sb.toString());
        return Md5Util.digest(sb.append(appkey).toString());
    }
}