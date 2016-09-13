package com.tory.noname.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.tory.noname.R;
import com.tory.noname.adapter.BaseRecyclerAdapter;
import com.tory.noname.adapter.BaseViewHolder;
import com.tory.noname.ben.Gank;
import com.tory.noname.utils.Constance;
import com.tory.noname.utils.FileUtils;
import com.tory.noname.utils.HttpUtil;
import com.tory.noname.utils.L;
import com.tory.noname.utils.Md5Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GankPageFragment extends BasePageFragment {
    private static final String TAG = "GankPageFragment";

    private static final String ARG_TYPE = "arg_type";

    private String mTag;
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter<Gank> mRecyclerAdpater;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public GankPageFragment() {
        // Required empty public constructor
    }

    public static GankPageFragment newInstance(String typeKey) {
        GankPageFragment fragment = new GankPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, typeKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTag = getArguments().getString(ARG_TYPE);
        }
        List<Gank> list = parseData(obtainOfflineData(getUrl()));
        mRecyclerAdpater = new GankRecyclerAdapter(list);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gank_page, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setAdapter(mRecyclerAdpater);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        /*
        * 设置手势下拉刷新的监听
        */
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        prepareFetchData(true);
                    }
                }
        );
    }

    public String getUrl() {
        String url = Constance.Gank.BASE_URL + "/" + mTag + "/10/" + 1;
        L.d(TAG, url);
        return url;
    }

    private List<Gank> parseData(String result){
        List<Gank> list;
        if(!TextUtils.isEmpty(result)){
            JSONObject jsonObj = JSONObject.parseObject(result);
            list= JSONObject.parseArray(jsonObj.getString("results"), Gank.class);
            L.d(list + "");
        }else{
            list = new ArrayList<Gank>();
        }

        return list;
    }

    @Override
    public void fetchData() {
        HttpUtil.getInstance().loadString(getUrl(), new HttpUtil.HttpCallBack() {
            @Override
            public void onLoading() {
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(String result) {
                mSwipeRefreshLayout.setRefreshing(false);
                mRecyclerAdpater.clear();
                mRecyclerAdpater.addAll(parseData(result));

            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),"加载"+mTag+"失败",Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    /**
     * 获取离线json数据
     *
     * @param url
     * @return
     */
    private String obtainOfflineData(String url) {
        String result = null;
        try {
            result = FileUtils.readString(getOfflineDir(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }



    /**
     * 获取文件离线数据目录
     *
     * @param url
     * @return
     */
    private String getOfflineDir(String url) {
        String cacheDir = FileUtils.getCacheDir(getContext()) +
                File.separator + "offline_gan_huo_cache" + File.separator + Md5Util.digest(url);
        L.d(TAG,"getOfflineDir:"+cacheDir);
        return cacheDir;

    }


    public class GankRecyclerAdapter extends BaseRecyclerAdapter<Gank> {

        public GankRecyclerAdapter(List<Gank> data) {
            super(R.layout.item_gank, data);
        }

        @Override
        protected void convert(BaseViewHolder holder, Gank item) {
            holder.getView(R.id.tv_desc).setVisibility(View.GONE);
            holder.getView(R.id.iv_img).setVisibility(View.GONE);

            if (item.url.endsWith(".jpg")) {
                holder.getView(R.id.iv_img).setVisibility(View.VISIBLE);
                ImageView imageView = holder.getView(R.id.iv_img);
                //HttpUtil.getInstance().loadImage(item.url, imageView, true);
                Glide.with(GankPageFragment.this)
                        .load(item.url)
                        .placeholder(R.drawable.ic_default)
                        .error(R.drawable.neterror)
                        .into(imageView);
            } else {
                holder.getView(R.id.tv_desc).setVisibility(View.VISIBLE);
                holder.setText(R.id.tv_desc, item.desc);
            }
            holder.setText(R.id.tv_source, item.source);
            holder.setText(R.id.tv_people, item.who);
            holder.setText(R.id.tv_time, item.publishedAt.substring(0, 10));
            holder.setText(R.id.tv_tag, item.type);
        }
    }

}
