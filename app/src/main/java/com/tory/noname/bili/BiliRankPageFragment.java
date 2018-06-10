package com.tory.noname.bili;

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

import com.bumptech.glide.Glide;
import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;
import com.tory.library.utils.SystemConfigUtils;
import com.tory.noname.R;
import com.tory.noname.bili.apis.BiliApis;
import com.tory.noname.bili.bean.RankVideoInfo;
import com.tory.noname.bili.bean.RankVideoItem;
import com.tory.noname.main.base.BasePageFragment;
import com.tory.noname.utils.L;
import com.tory.noname.utils.Utilities;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class BiliRankPageFragment extends BasePageFragment implements
        BaseRecyclerAdapter.OnRecyclerViewItemClickListener,
        BaseRecyclerAdapter.OnRecyclerViewItemLongClickListener {

    private static final String TAG = "BiliRankPageFragment";

    private static final String RANK_TITLE= "rank_title";
    private static final String RANK_TYPE_NAME = "rank_menu_name";
    private static final String RANK_RANGE = "rank_range";
    private static final String RANK_CATALOGY_TAB_ID = "rank_catalogy_tab_id";

    private String mRankType;
    private int mRankRange = 30;
    private int mRankCatelogyId;


    private String mTitle;
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter<RankVideoItem> mRecyclerAdpater;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public BiliRankPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param rankCatelogyId Parameter 1.
     * @param rankMenuName Parameter 2.
     * @return A new instance of fragment BiliRankPageFragment.
     */
    public static BiliRankPageFragment newInstance(String title ,String rankMenuName, int rankRange,int rankCatelogyId) {
        BiliRankPageFragment fragment = new BiliRankPageFragment();
        Bundle args = new Bundle();
        args.putString(RANK_TITLE, title);
        args.putInt(RANK_RANGE, rankRange);
        args.putInt(RANK_CATALOGY_TAB_ID, rankCatelogyId);
        args.putString(RANK_TYPE_NAME, rankMenuName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
        if (bundle != null) {
            mTitle = bundle.getString(RANK_TITLE);
            mRankRange = bundle.getInt(RANK_RANGE);
            mRankCatelogyId = bundle.getInt(RANK_CATALOGY_TAB_ID);
            mRankType = bundle.getString(RANK_TYPE_NAME);
        }
        mRecyclerAdpater = new BiliRankRecyclerAdapter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(RANK_TITLE, mTitle);
        outState.putInt(RANK_RANGE, mRankRange);
        outState.putInt(RANK_CATALOGY_TAB_ID, mRankCatelogyId);
        outState.putString(RANK_TYPE_NAME, mRankType);
        super.onSaveInstanceState(outState);
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
        mRecyclerAdpater.setOnRecyclerViewItemClickListener(this);
        mRecyclerAdpater.setOnRecyclerViewItemLongClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                prepareFetchData(true);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void fetchData() {
        RetrofitHelper
                .createBiliRankService()
                .getRankItems(mRankType,mRankRange,mRankCatelogyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( rankVideoInfo -> refresData(rankVideoInfo.rank.list));

    }

    private void refresData(List<RankVideoItem> list) {
        mRecyclerAdpater.clear();
        mRecyclerAdpater.addAll(list);
    }

    @Override
    public void onItemClick(View v, int position) {
        RankVideoItem biliRank = mRecyclerAdpater.getItem(position);
        String url = BiliHelper.getUrlFromAid(biliRank.aid);
        Utilities.startWeb(getActivity(),url);
    }

    @Override
    public boolean onLongClick(View v, int position) {
        RankVideoItem biliRank = mRecyclerAdpater.getItem(position);
        final String url = BiliHelper.getUrlFromAid(biliRank.aid);
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


    public void setRankRange(int rankRange){
        if(rankRange != mRankRange){
            mRankRange = rankRange;
            isDataInitiated = false;
            if(isVisibleToUser && isViewInitiated){
                prepareFetchData(true);
            }
            putArgument(RANK_RANGE,mRankRange);
        }
    }

    protected void putArgument(String key,Object value){
        Bundle bundle = getArguments();
        if(bundle == null) bundle = new Bundle();
        if(value instanceof String){
            bundle.putString(key,(String)value);
        }else if(value instanceof Integer){
            bundle.putInt(key,(int)value);
        }
    }


    private class BiliRankRecyclerAdapter extends BaseRecyclerAdapter<RankVideoItem>{

        int colorAccent;
        int textColorPrimary;
        int textColorSecondary;

        public BiliRankRecyclerAdapter() {
            super(R.layout.item_bili_video);
            colorAccent = SystemConfigUtils.getThemeColor(getActivity(),R.attr.colorAccent);
            textColorPrimary = SystemConfigUtils.getThemeColor(getActivity(),android.R.attr.textColorPrimary);
            textColorSecondary = SystemConfigUtils.getThemeColor(getActivity(),android.R.attr.textColorSecondary);
        }

        @Override
        protected void convert(BaseViewHolder holder, RankVideoItem item) {
            int rankNum = holder.getLayoutPosition();
            holder.setText(R.id.tv_rank_num,String.valueOf(rankNum+1));
            if(rankNum < 3){
                holder.setTextColor(R.id.tv_rank_num,colorAccent);
                holder.setTextSize(R.id.tv_rank_num,18 + (3 - rankNum) * 2);
            }else{
                holder.setTextColor(R.id.tv_rank_num,textColorPrimary);
                holder.setTextSize(R.id.tv_rank_num,18);
            }
            Glide.with(BiliRankPageFragment.this)
                    .load(item.pic)
                    .placeholder(R.drawable.bili_default_image_tv)
                    .into((ImageView) holder.getView(R.id.iv_pic));
            holder.setText(R.id.tv_title,item.title)
                    .setText(R.id.tv_author,item.author)
                    .setText(R.id.tv_play,BiliHelper.formatNumber(item.play))
                    .setText(R.id.tv_danmakus,BiliHelper.formatNumber(item.video_review));
        }

    }

}
