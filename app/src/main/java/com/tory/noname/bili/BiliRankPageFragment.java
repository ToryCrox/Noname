package com.tory.noname.bili;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.tory.noname.R;
import com.tory.noname.adapter.BaseRecyclerAdapter;
import com.tory.noname.adapter.BaseViewHolder;
import com.tory.noname.bili.bean.BiliRank;
import com.tory.noname.fragment.BasePageFragment;
import com.tory.noname.utils.L;
import com.tory.noname.utils.SystemConfigUtils;
import com.tory.noname.utils.Utilities;
import com.tory.noname.utils.http.XOkHttpUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private BaseRecyclerAdapter<BiliRank> mRecyclerAdpater;
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
        if (getArguments() != null) {
            mRankRange = getArguments().getInt(RANK_RANGE);
            mRankCatelogyId = getArguments().getInt(RANK_CATALOGY_TAB_ID);
            mRankType = getArguments().getString(RANK_TYPE_NAME);
            mTitle = getArguments().getString(RANK_TITLE);
        }
        mRecyclerAdpater = new BiliRankRecyclerAdapter(new ArrayList<BiliRank>());
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
        XOkHttpUtils.getInstance().cancelTag(this);
        super.onDestroy();
    }

    @Override
    public void fetchData() {
        final String url = getUrl();
        XOkHttpUtils.get(url)
                .tag(this)
                .execute(new XOkHttpUtils.HttpCallBack() {
                    @Override
                    public void onLoadStart() {
                        if(!isRecreated){
                            mSwipeRefreshLayout.setRefreshing(true);
                        }
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


    private String getUrl(){
        String url = BiliApis.BASE_RANK_URL +
                String.format("%s-%d-%d.json", mRankType, mRankRange,mRankCatelogyId);
        L.d(TAG,url);
        return url;
    }

    private void refresData(String result) {
        mRecyclerAdpater.clear();
        mRecyclerAdpater.addAll(parseData(result));
    }

    private List<BiliRank> parseData(String result) {
        List<BiliRank> list = null;
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObj = JSONObject.parseObject(result);
                JSONObject rankObj = jsonObj.getJSONObject("rank");
                int code = rankObj.getIntValue("code");
                if(code == 0){
                    list = JSONObject.parseArray(rankObj.getString("list"), BiliRank.class);
                    L.d(list + "");
                }else{
                    L.w(TAG," return code error:"+code);
                }
            } catch (Exception e) {
                L.d(TAG,"parseData error url:"+getUrl()+"\n"+result);
                e.printStackTrace();

            }

        } else {
            list = new ArrayList<BiliRank>();
        }
        return list;
    }

    @Override
    public void onItemClick(View v, int position) {
        BiliRank biliRank = mRecyclerAdpater.getItem(position);
        String url = BiliHelper.getUrlFromAid(biliRank.aid);
        Utilities.startWeb(getActivity(),url);
    }

    @Override
    public boolean onLongClick(View v, int position) {
        BiliRank biliRank = mRecyclerAdpater.getItem(position);
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
        //setArguments(bundle);
    }


    private class BiliRankRecyclerAdapter extends BaseRecyclerAdapter<BiliRank>{

        int colorAccent;
        int textColorPrimary;
        int textColorSecondary;

        public BiliRankRecyclerAdapter(List<BiliRank> data) {
            super(R.layout.item_bili_rank, data);
            colorAccent = SystemConfigUtils.getThemeColor(getActivity(),R.attr.colorAccent);
            textColorPrimary = SystemConfigUtils.getThemeColor(getActivity(),android.R.attr.textColorPrimary);
            textColorSecondary = SystemConfigUtils.getThemeColor(getActivity(),android.R.attr.textColorSecondary);
        }

        @Override
        protected void convert(BaseViewHolder holder, BiliRank item) {
            int rankNum = holder.getLayoutPosition();
            holder.setText(R.id.tv_rank_num,String.valueOf(rankNum+1));
            if(rankNum < 3){
                holder.setTextColor(R.id.tv_rank_num,colorAccent
                        );
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
                    .setText(R.id.tv_play,formatNumber(item.play))
                    .setText(R.id.tv_danmakus,formatNumber(item.video_review));
            tintTextDrawables(holder,textColorSecondary,R.id.iv_author,R.id.iv_play,R.id.iv_danmakus);
        }

        private void tintTextDrawables(BaseViewHolder holder,int color,int ...resIds){
            for(int resId : resIds){
                if(Boolean.TRUE.equals(holder.getView(resId).getTag(R.id.text_view_drawable_tint))){
                    L.d("drawablesdrawables: continue");
                    continue;
                }
                tintDrawable(holder.getView(resId),color);
            }
        }

        //http://chuansong.me/n/400689551333
        private void tintDrawable(View view, int color) {
            if(view instanceof TextView){//not function
                TextView tv = (TextView) view;
                Drawable[] drawables = tv.getCompoundDrawablesRelative();
                L.d("drawablesdrawables:"+ Arrays.toString(drawables));
                if(drawables == null ) return;
                for(int i = 0;i < drawables.length;i++){
                    Drawable drawable = drawables[i];
                    if(drawable != null){
                        Drawable d = DrawableCompat.wrap(drawable).mutate();
                        DrawableCompat.setTintList(d, ColorStateList.valueOf(color));
                        drawables[i] = d;
                    }
                }
                tv.setCompoundDrawablesRelative(drawables[0],drawables[1],drawables[2],drawables[3]);
                L.d("drawablesdrawables 11:"+ Arrays.toString(drawables));
                tv.setTag(R.id.text_view_drawable_tint,Boolean.TRUE);
            }else if(view instanceof ImageView){
                ImageView iv = (ImageView) view;
                Drawable d = DrawableCompat.wrap(iv.getDrawable());
                DrawableCompat.setTintList(d, ColorStateList.valueOf(color));
                iv.setImageDrawable(d);
            }
        }
    }

    public static String formatNumber(int num){
        if(num > 10000){
            return String.format("%.1f万",(num * 1.0f / 10000));
        }else{
            return String.valueOf(num);
        }

    }

}
