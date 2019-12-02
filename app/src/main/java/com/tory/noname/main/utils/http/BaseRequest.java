package com.tory.noname.main.utils.http;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;

public class BaseRequest {

    private String url;
    private Object tag;
    private Map<String, String> params;
    XOkHttpUtils httpUtil;

    public BaseRequest(String url) {
        httpUtil = XOkHttpUtils.getInstance();
        this.url = url;
    }

    public BaseRequest tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public void execute(XOkHttpUtils.HttpCallBack callBack) {
        Request request = generateRequest(null);
        httpUtil.enqueue(request, callBack);
    }

    protected Request generateRequest(RequestBody requestBody) {
        Request.Builder builder = new Request.Builder();
        builder.url(XOkHttpUtils.createUrlFromParams(url,params));
        if (tag != null) builder.tag(tag);
        return builder.build();
    }

    public BaseRequest params(String key,Object value){
        getParams().put(key,String.valueOf(value));
        return this;
    }

    public BaseRequest params(Map<String,String> data){
        getParams().putAll(data);
        return this;
    }

    public Map<String,String> getParams(){
        if(params == null){
            params = new HashMap<>();
        }
        return params;
    }




}