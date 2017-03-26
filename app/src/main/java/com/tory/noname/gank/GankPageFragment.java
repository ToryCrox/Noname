package com.tory.noname.gank;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;
import com.tory.library.recycler.EndlessRecyclerOnScrollListener;
import com.tory.library.utils.FileUtils;
import com.tory.library.utils.Md5Util;
import com.tory.noname.R;
import com.tory.noname.main.base.BasePageFragment;
import com.tory.noname.utils.Constance;
import com.tory.noname.utils.L;
import com.tory.noname.utils.Utilities;
import com.tory.noname.utils.http.XOkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class GankPageFragment extends BasePageFragment
        implements BaseRecyclerAdapter.OnRecyclerViewItemClickListener,
        BaseRecyclerAdapter.OnRecyclerViewItemLongClickListener {
    private static final String TAG = "GankPageFragment";

    private static final String ARG_TYPE = "arg_type";

    private String mTag;
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter<Gank> mRecyclerAdpater;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mPageCount = 15;
    private int mPageIndex = 1;

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
        List<Gank> list = null;//parseData(obtainOfflineData(getUrl()));
        mRecyclerAdpater = new GankRecyclerAdapter(list);
        mRecyclerAdpater.setOnRecyclerViewItemClickListener(this);
        mRecyclerAdpater.setOnRecyclerViewItemLongClickListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_common_page, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        mRecyclerView.clearOnScrollListeners();
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener((LinearLayoutManager) mRecyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int currentPage) {
                mPageIndex = currentPage;
                prepareFetchData(true);
            }
        });
    }

    @Override
    public void onResume() {
        L.d(TAG,"onResume:"+mTag);
        super.onResume();
    }

    public String getUrl() {
        String url = Constance.Gank.BASE_URL + "/" + mTag + "/" + mPageCount + "/" + mPageIndex;
        L.d(TAG, url);
        return url;
    }

    private List<Gank> parseData(String result) {
        List<Gank> list = null;
        if (!TextUtils.isEmpty(result)) {
            try {
                L.d("parseData result="+result);
                JSONObject jsonObj = JSONObject.parseObject(result);
                list = JSONObject.parseArray(jsonObj.getString("results"), Gank.class);
                L.d(list + "");
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        if(list == null){
            list = new ArrayList<Gank>();
        }

        return list;
    }

    @Override
    public void fetchData() {
        final String url = getUrl();
        XOkHttpUtils
                .get(url)
                .tag(this)
                .execute(new XOkHttpUtils.HttpCallBack() {
                    @Override
                    public void onLoadStart() {
                        //mSwipeRefreshLayout.setRefreshing(true);
                    }

                    @Override
                    public void onSuccess(String result) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (mPageIndex == 1) {
                            mRecyclerAdpater.clear();
                        }
                        mRecyclerAdpater.addAll(parseData(result));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "加载" + mTag + "失败", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });

    }

    @Override
    public void onDestroy() {
        XOkHttpUtils.getInstance().cancelTag(this);
        super.onDestroy();
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
            //result = FileUtils.readString(getOfflineDir(url));
            result = XOkHttpUtils.getInstance().getStringFromCatch(getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 保存离线数据
     *
     * @param url
     * @param result
     */
    private void saveOfficeLineData(String url, String result) {
        try {
            FileUtils.writeString(result, getOfflineDir(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取文件离线数据目录
     *
     * @param url
     * @return
     */
    private String getOfflineDir(String url) {
        String cacheDir = FileUtils.getCacheDir(getContext()) +
                File.separator + "offline_gank" + File.separator + Md5Util.digest(url);
        FileUtils.mkdir(new File(cacheDir).getParentFile());
        L.d(TAG, "getOfflineDir:" + cacheDir);
        return cacheDir;

    }

    @Override
    public void onItemClick(View v, int position) {
        Gank bean = mRecyclerAdpater.getItem(position);
        Utilities.startWeb(getContext(), bean.url);

    }

    @Override
    public boolean onLongClick(View v, int position) {
        Gank gank = mRecyclerAdpater.getItem(position);
        final String url = gank.url;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setItems(new String[]{"复制链接", "浏览器打开"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Utilities.copyToClipboar(getActivity(), url);
                        } else if (which == 1) {
                            Utilities.openInBrowser(getActivity(), url);
                        }
                    }
                });
        builder.show();
        return true;
    }


    public class GankRecyclerAdapter extends BaseRecyclerAdapter<Gank> {

        public GankRecyclerAdapter(List<Gank> data) {
            super(R.layout.item_gank, data);
            addFooterView();
        }

        @Override
        protected void convert(BaseViewHolder holder, Gank item) {
            holder.getView(R.id.tv_desc).setVisibility(View.GONE);
            holder.getView(R.id.iv_img).setVisibility(View.GONE);

            if (item.url.endsWith(".jpg")) {
                holder.getView(R.id.iv_img).setVisibility(View.VISIBLE);
                ImageView imageView = holder.getView(R.id.iv_img);
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
            holder.setText(R.id.tv_people, item.who+ matchGithub(item.url));
            holder.setText(R.id.tv_time, item.publishedAt.substring(0, 10));
            holder.setText(R.id.tv_tag, item.type);
        }
    }


    public static Pattern sGitHubPattern = Pattern.compile("github\\.com");
    private String matchGithub(String url){
        return sGitHubPattern.matcher(url).find() ? " GitHub" : "" ;
    }
}
