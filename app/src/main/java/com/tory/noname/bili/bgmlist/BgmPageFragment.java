package com.tory.noname.bili.bgmlist;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;
import com.tory.noname.R;
import com.tory.noname.bili.BiliHelper;
import com.tory.noname.main.base.BasePageFragment;
import com.tory.noname.main.utils.L;
import com.tory.library.utils.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @Author: Tory
 * Create: 2016/9/25
 * Update:
 */
public class BgmPageFragment extends BasePageFragment implements BaseRecyclerAdapter.OnRecyclerViewItemClickListener, BaseRecyclerAdapter.OnRecyclerViewItemLongClickListener, BgmPresenter.OnBgmlistLoadCompeletListener {

    public static final String FRAGMENT_TAG = "BgmPageFragment";

    public static final String ARG_WEEK_DAY = "weekDay";
    public static final String ARG_WEEK_FITER = "weekDayFiter";

    private static final String TAG = "BgmPageFragment";

    private int mWeekDay;
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter<BgmItem> mRecyclerAdpater;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mWeekDayFiter = true;

    List<BgmItem> mList = new ArrayList<>();

    public BgmPageFragment() {

    }

    public static BgmPageFragment newInstance(int weekDay,boolean weekDayFiter) {
        BgmPageFragment fragment = new BgmPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_WEEK_DAY, weekDay);
        bundle.putBoolean(ARG_WEEK_FITER, weekDayFiter);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle savedBundle = savedInstanceState != null ? savedInstanceState : getArguments();
        if (savedBundle != null) {
            mWeekDay = savedBundle.getInt(ARG_WEEK_DAY);
            mWeekDayFiter = savedBundle.getBoolean(ARG_WEEK_FITER,true);
        }
        mRecyclerAdpater = new BgmPageRecyclerAdapter(null);
        mRecyclerAdpater.setOnRecyclerViewItemClickListener(this);
        mRecyclerAdpater.setOnRecyclerViewItemLongClickListener(this);
        BgmPresenter.getInstance().addLoadListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_WEEK_DAY, mWeekDay);
        outState.putBoolean(ARG_WEEK_FITER, mWeekDayFiter);
        super.onSaveInstanceState(outState);
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
        //mSwipeRefreshLayout.setEnabled(false);
        Utilities.initSwipeRefresh(mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BgmPresenter.getInstance().loadData(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        L.d(TAG,"onLoadComplete mWeekDay:"+mWeekDay+" list:"+mList.size());
    }

    @Override
    public void onDestroy() {
        BgmPresenter.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void fetchData() {
    }

    @Override
    public void onItemClick(View v, int position) {

    }

    @Override
    public boolean onLongClick(View v, int position) {
        final BgmItem item = mRecyclerAdpater.getItem(position);

        final List<String> airSite = item.onAirSite;
        String[] siteNames = item.siteNames.toArray(new String[item.siteNames.size()]);
        if(airSite == null || airSite.isEmpty()) return true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setItems(siteNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String site= airSite.get(which);
                        if(site.contains("bilibili")){
                            BiliHelper.openInBili(getActivity(),site);
                        }else{
                            Utilities.startWeb(getActivity(),site);
                        }
                    }
                });
        builder.show();
        
        return true;
    }


    @Override
    public void onLoadCompelte(List<BgmItem> list) {
        if(mSwipeRefreshLayout!=null&&mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mList.clear();
        for (BgmItem bgmItem : list) {
            int weekDay = mWeekDayFiter ? bgmItem.weekDayJP : bgmItem.weekDayCN;
            if(weekDay == mWeekDay){
                mList.add(bgmItem);
            }
        }
        Collections.sort(mList, new Comparator<BgmItem>() {
            @Override
            public int compare(BgmItem lhs, BgmItem rhs) {
                String time1 = mWeekDayFiter ? lhs.timeJP : lhs.timeCN;
                String time2 = mWeekDayFiter ? rhs.timeJP : rhs.timeCN;
                if(TextUtils.isEmpty(time1) || time1.length() < 4 || !TextUtils.isDigitsOnly(time1)){
                    time1 = "9999";
                }
                if(TextUtils.isEmpty(time2) ||  time2.length()< 4|| !TextUtils.isDigitsOnly(time2)){
                    time2 = "9999";
                }
                return time1.compareTo(time2);
            }
        });

        L.d(TAG,"onLoadComplete mWeekDay:"+mWeekDay+"  ;"+mList);
        if(mRecyclerAdpater != null){
            mRecyclerAdpater.clear();
            mRecyclerAdpater.addAll(mList);
        }
    }

    public void onSaveFilterChange(boolean weekDayFiter) {
        mWeekDayFiter = weekDayFiter;
        onLoadCompelte(BgmPresenter.getInstance().getBgmList());
    }



    private class BgmPageRecyclerAdapter extends BaseRecyclerAdapter<BgmItem> {

        public BgmPageRecyclerAdapter(List<BgmItem> data) {
            super(R.layout.item_bgmlist, data);
            L.d(TAG,"onLoadComplete BgmPageRecyclerAdapter:"+mWeekDay+"  ;"+data);
        }

        @Override
        protected void convert(BaseViewHolder holder, BgmItem item) {

            holder.setText(R.id.tv_title_cn, item.titleCN)
                    .setText(R.id.tv_time_jp, contactTime(item))
                    .setText(R.id.tv_air_sites, TextUtils.join(",", item.siteNames));
        }

        public String contactTime(BgmItem item){
            return "日本:"+parseTime(item.timeJP) + "\t"
                    +"大陆:"+parseTime(item.timeCN);
        }
    }

    public String parseTime(String time){
        if(TextUtils.isEmpty(time) || time.length() != 4) return time;
        return time.substring(0,2) + ":"+time.substring(2);
    }


}
