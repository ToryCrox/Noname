package com.tory.noname.ss;

import com.tory.noname.main.utils.L;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Author: tory
 * Create: 2017/3/26
 * Update: ${UPDATE}
 */
public class PingUtil {

    private static final String TAG = "PingUtil";

    public static String ping(String ip){
        try {
            String lost = new String();
            String delay = new String();
            Process p = Runtime.getRuntime().exec("ping -c 4 " + ip);
            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str = null;
            L.d(TAG,"start ping ip="+ip);
            while((str= buf.readLine())!=null){
                L.d(TAG,""+str);
                if(str.contains("packet loss")){
                    int i= str.indexOf("received");
                    int j= str.indexOf("%");
                    //System.out.println("丢包率:"+str.substring(i+10, j+1));
                    lost = "丢包率:"+str.substring(i+10, j+1);
                }
                if(str.contains("avg")){
                    int i=str.indexOf("/", 20);
                    int j=str.indexOf(".", i);
                    delay = "延迟"+str.substring(i+1, j);
                    delay = delay+"ms";
                }
            }
            return delay + "    "+ lost;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return "失败";
    }
}
