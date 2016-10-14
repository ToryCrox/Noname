package com.tory.noname.bili;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.tory.noname.R;
import com.tory.noname.bili.apis.BiliApis;
import com.tory.noname.bili.apis.BiliService;
import com.tory.noname.bili.apis.VideoListConverterFactory;
import com.tory.noname.bili.bean.CategoryMeta;
import com.tory.noname.bili.bean.VideoItem;
import com.tory.noname.main.base.BasePageFragment;
import com.tory.noname.recycler.BaseRecyclerAdapter;
import com.tory.noname.recycler.BaseViewHolder;
import com.tory.noname.recycler.EndlessRecyclerOnScrollListener;
import com.tory.noname.utils.L;
import com.tory.noname.utils.SystemConfigUtils;
import com.tory.noname.utils.Utilities;
import com.tory.noname.utils.http.XOkHttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * 推荐
 * http://www.bilibili.com/index/ding/13.json?access_key=ba761f9faa500a6c8529a0e18fc794f0&appkey=1d8b6e7d45233436&build=426003&mobi_app=android&page=1&pagesize=100&platform=android&sign=5d0a12b922bda3975dd3c021c2e3d04d
 * 推荐下的所有视频
 * http://www.bilibili.com/index/ding/13.json?access_key=ba761f9faa500a6c8529a0e18fc794f0&appkey=1d8b6e7d45233436&build=426003&mobi_app=android&page=1&pagesize=100&platform=android&sign=5d0a12b922bda3975dd3c021c2e3d04d
 * <p/>
 * 最热视频
 * http://app.bilibili.com/x/region/show/two/old?access_key=ba761f9faa500a6c8529a0e18fc794f0&appkey=1d8b6e7d45233436&build=426003&mobi_app=android&platform=android&tid=33&ts=1474791371000&sign=7794378676f2d54bdc57961e3e81d73f
 * <p/>
 * 最新视频
 * http://api.bilibili.com/list?_device=android&_hwid=c475c83f9dfc26b7&_ulv=10000&access_key=ba761f9faa500a6c8529a0e18fc794f0&appkey=1d8b6e7d45233436&build=426003&mobi_app=android&order=default&page=1&pagesize=20&platform=android&tid=33&sign=b265db4c879044c034c4d2ca101838d0
 * /
 * <p/>
 * /**
 *
 * @Author: Tory
 * Create: 2016/9/25
 * Update: ${UPDATE}
 */
public class CategoryPageFragment extends BasePageFragment implements BaseRecyclerAdapter.OnRecyclerViewItemClickListener, BaseRecyclerAdapter.OnRecyclerViewItemLongClickListener {

    public static final String FRAGMENT_TAG = "BgmPageFragment";

    public static final String ARG_CATE = "arg_cate";

    private static final String TAG = "BgmPageFragment";

    CategoryMeta mCate;
    private String mTag;
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter<VideoItem> mRecyclerAdpater;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mPageIndex = 1;
    private int mPageSize = 20;

    public CategoryPageFragment() {

    }

    public static CategoryPageFragment newInstance(CategoryMeta categoryMeta) {
        CategoryPageFragment fragment = new CategoryPageFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_CATE, categoryMeta);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCate = getArguments().getParcelable(ARG_CATE);
        }
        mRecyclerAdpater = new CatePageRecyclerAdapter(null);
        mRecyclerAdpater.setOnRecyclerViewItemClickListener(this);
        mRecyclerAdpater.setOnRecyclerViewItemLongClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bili_category_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setAdapter(mRecyclerAdpater);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        Utilities.initSwipeRefresh(mSwipeRefreshLayout);
        /*
        * 设置手势下拉刷新的监听
        */
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mPageIndex = 1;
                        prepareFetchData(true);
                    }
                }
        );

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener((LinearLayoutManager) mRecyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int currentPage) {
                mPageIndex = currentPage;
                prepareFetchData(true);
            }
        });
    }

    @Override
    public void onDestroy() {
        XOkHttpUtils.getInstance().cancelTag(this);
        super.onDestroy();
    }

    @Override
    public void fetchData() {
        Retrofit retrofit =new Retrofit.Builder()
                .baseUrl(BiliApis.BASE_URL_API)
                .addConverterFactory(VideoListConverterFactory.create())
                .build();
        BiliService biliService = retrofit.create(BiliService.class);
        Call<List<VideoItem>> call = biliService.getVideoByPartion(getParams());
        call.enqueue(new Callback<List<VideoItem>>() {
            @Override
            public void onResponse(Call<List<VideoItem>> call,
                                   Response<List<VideoItem>> response) {
                mSwipeRefreshLayout.setRefreshing(false);
                refresData(response.body());
            }

            @Override
            public void onFailure(Call<List<VideoItem>> call, Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });

        if(true) return;
        XOkHttpUtils.get(getBaseUrl())
                .params(getParams())
                .tag(this)
                .execute(new XOkHttpUtils.HttpCallBack() {
                    @Override
                    public void onLoadStart() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }

                    @Override
                    public void onSuccess(String result) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        refresData(result);
                    }

                    @Override
                    public void onError(Exception e) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });

    }
    public String getBaseUrl() {
        return "http://api.bilibili.com/archive_rank/getarchiverankbypartion";
    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("type", "json");
        params.put("pn", "" + mPageIndex);
        params.put("tid" ,""+ mCate.tid);
        params.put("_", "" + System.currentTimeMillis());
        return params;
    }

    private List<VideoItem> parseData(String result) {
        List<VideoItem> list = null;
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObj = JSONObject.parseObject(result);
                //JSONObject listObj = jsonObj.getJSONObject("list");
                int code = jsonObj.getIntValue("code");
                if (code == 0) {
                    //list = JSONObject.parseArray(jsonObj.getString("list"), VideoItem.class);
                    JSONObject dataObj = jsonObj.getJSONObject("data");
                    try {
                        list = JSONObject.parseArray(dataObj.getString("archives"), VideoItem.class);
                    } catch (Exception e) {
                        L.w(e.toString());
                    }
                } else {
                    L.w(TAG, " return code error:" + code + "result:" + result);
                }
            } catch (Exception e) {
                L.d(TAG, "parseData error url:" + getBaseUrl() + "\n" + result);
                Log.e(TAG, "" + e.toString());

            }

        }
        if (list == null) list = new ArrayList<>();
        return list;
    }

    private void refresData(List<VideoItem> list){
        if(mPageIndex == 1){
            mRecyclerAdpater.clear();
        }
        mRecyclerAdpater.addAll(list);
    }

    private void refresData(String result) {
        refresData(parseData(result));
    }

    @Override
    public void onItemClick(View v, int position) {
        VideoItem item = mRecyclerAdpater.getItem(position);
        String url = BiliHelper.getUrlFromAid(item.aid);
        Utilities.startWeb(getActivity(),url);
    }

    @Override
    public boolean onLongClick(View v, int position) {
        VideoItem item = mRecyclerAdpater.getItem(position);
        final String url = BiliHelper.getUrlFromAid(item.aid);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setItems(new String[]{"复制链接", "打开BiliBili"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Utilities.copyToClipboar(getActivity(),url);
                        }else if(which == 1){
                            BiliHelper.openInBili(getActivity(),url);
                        }
                    }
                });
        builder.show();
        return true;
    }

    private class CatePageRecyclerAdapter extends BaseRecyclerAdapter<VideoItem> {

        int textColorSecondary;
        public CatePageRecyclerAdapter(List<VideoItem> data) {
            super(R.layout.item_bili_video, data);
            addFooterView();
            textColorSecondary = SystemConfigUtils.getThemeColor(getActivity(),android.R.attr.textColorSecondary);
        }

        @Override
        protected void convert(BaseViewHolder holder, VideoItem item) {
            holder.setVisible(R.id.tv_rank_num, false)
                    .setText(R.id.tv_title, item.title)
                    .setText(R.id.tv_author, item.author)
                    .setText(R.id.tv_play, BiliHelper.formatNumber(item.play))
                    .setText(R.id.tv_danmakus, BiliHelper.formatNumber(item.video_review));
            Glide.with(CategoryPageFragment.this)
                    .load(item.pic)
                    .placeholder(R.drawable.bili_default_image_tv)
                    .into((ImageView) holder.getView(R.id.iv_pic));
            BiliViewHelper.tintTextDrawables(holder,textColorSecondary,
                    R.id.tv_author,R.id.tv_play,R.id.tv_danmakus);
        }

    }


}
