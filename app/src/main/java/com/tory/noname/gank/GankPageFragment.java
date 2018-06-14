package com.tory.noname.gank;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;
import com.tory.library.recycler.EndlessRecyclerOnScrollListener;
import com.tory.library.utils.FileUtils;
import com.tory.library.utils.Md5Util;
import com.tory.noname.R;
import com.tory.noname.bili.RetrofitHelper;
import com.tory.noname.gank.bean.GankItem;
import com.tory.noname.main.base.BasePageFragment;
import com.tory.noname.utils.Constance;
import com.tory.noname.utils.L;
import com.tory.noname.utils.Utilities;
import com.tory.noname.utils.http.XOkHttpUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class GankPageFragment extends BasePageFragment
        implements BaseRecyclerAdapter.OnRecyclerViewItemClickListener,
        BaseRecyclerAdapter.OnRecyclerViewItemLongClickListener {
    private static final String TAG = "GankPageFragment";

    private static final String ARG_TYPE = "arg_type";

    private String mTag;
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter<GankItem> mRecyclerAdpater;
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
        mRecyclerAdpater = new GankRecyclerAdapter();
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

    @Override
    public void fetchData() {
        RetrofitHelper.createGankApiService()
                .getGankApiResult(mTag, mPageCount, mPageIndex)
                .subscribeOn(Schedulers.io())
                .map(gankApiResult -> {
                        return gankApiResult.getResults(); })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(gankItems -> {
                        if (mPageIndex == 1) {
                            mRecyclerAdpater.clear();
                        }
                        mRecyclerAdpater.addAll(gankItems);
                }, e -> e.printStackTrace(), () -> mSwipeRefreshLayout.setRefreshing(false));

    }

    @Override
    public void onDestroy() {
        XOkHttpUtils.getInstance().cancelTag(this);
        super.onDestroy();
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
        GankItem bean = mRecyclerAdpater.getItem(position);
        Utilities.startWeb(getContext(), bean.getUrl());

    }

    @Override
    public boolean onLongClick(View v, int position) {
        GankItem gank = mRecyclerAdpater.getItem(position);
        final String url = gank.getUrl();
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


    public class GankRecyclerAdapter extends BaseRecyclerAdapter<GankItem> {

        public GankRecyclerAdapter( ){
            super(R.layout.item_gank, null);
            addFooterView();
        }

        @Override
        protected void convert(BaseViewHolder holder, GankItem item) {
            holder.getView(R.id.tv_desc).setVisibility(View.GONE);
            holder.getView(R.id.iv_img).setVisibility(View.GONE);

            if (item.getUrl().endsWith(".jpg")) {
                holder.getView(R.id.iv_img).setVisibility(View.VISIBLE);
                ImageView imageView = holder.getView(R.id.iv_img);
                Glide.with(GankPageFragment.this)
                        .load(item.getUrl())
                        .placeholder(R.drawable.ic_default)
                        .error(R.drawable.neterror)
                        .into(imageView);
            } else {
                holder.getView(R.id.tv_desc).setVisibility(View.VISIBLE);
                holder.setText(R.id.tv_desc, item.getDesc());
            }
            holder.setText(R.id.tv_source, item.getSource());
            holder.setText(R.id.tv_people, item.getWho()+ matchGithub(item.getUrl()));
            holder.setText(R.id.tv_time, item.getPublishedAt().substring(0, 10));
            holder.setText(R.id.tv_tag, item.getType());
        }
    }


    public static Pattern sGitHubPattern = Pattern.compile("github\\.com");
    private String matchGithub(String url){
        return sGitHubPattern.matcher(url).find() ? " GitHub" : "" ;
    }
}
