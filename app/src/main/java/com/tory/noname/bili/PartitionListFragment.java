package com.tory.noname.bili;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tory.noname.R;
import com.tory.noname.adapter.BaseRecyclerAdapter;
import com.tory.noname.adapter.BaseViewHolder;
import com.tory.noname.bili.bean.CategoryMeta;
import com.tory.noname.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/9/25
 * Update: 2016/9/25
 */
public class PartitionListFragment extends Fragment implements BaseRecyclerAdapter.OnRecyclerViewItemClickListener {

    public static final String FRAGMENT_TAG = "fragment_tag";

    private static final String TAG = "PartitionListFragment";
    private RecyclerView mRecyclerView;


    private BaseRecyclerAdapter<CategoryMeta> mRecyclerAdpater;
    private List<CategoryMeta> mCateList;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public PartitionListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_common_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initData();
        mRecyclerAdpater = new CategoryAdpater(mCateList);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mRecyclerView.setAdapter(mRecyclerAdpater);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        if(mSwipeRefreshLayout!=null){
            mSwipeRefreshLayout.setEnabled(false);
        }

        mRecyclerAdpater.setOnRecyclerViewItemClickListener(this);
    }

    private void initData() {
        CategoryMeta root = BiliHelper.buildCate(getActivity());
        List<CategoryMeta> list = new ArrayList<>(20);
        list.addAll(root.child);
        CategoryMeta rank = new CategoryMeta(-1,"排行",R.drawable.ic_btn_rank_all,CategoryMeta.TYPE_RANK);
        list.add(rank);
        mCateList = list;
        L.d(TAG,"list:"+mCateList);
    }

    @Override
    public void onItemClick(View v, int position) {
        CategoryMeta cate = mCateList.get(position);
        startCateHomeActivity(cate);
    }

    private void startCateHomeActivity(CategoryMeta cate) {
        Intent intent = new Intent(getActivity(),CategoryHomeActivity.class);
        intent.putExtra(CategoryHomeActivity.ARG_CATE_ID,cate.tid);
        intent.putExtra(CategoryHomeActivity.ARG_CATE_TYPE,cate.type);
        intent.putExtra(CategoryHomeActivity.ARG_CATE,cate);
        startActivity(intent);
    }

    private class CategoryAdpater extends BaseRecyclerAdapter<CategoryMeta> {


        public CategoryAdpater(List<CategoryMeta> data) {
            super(R.layout.item_bili_partition, data);
        }

        @Override
        protected void convert(BaseViewHolder holder, CategoryMeta item) {
            holder.setImageResource(R.id.iv_cover,item.coverRes)
                    .setText(R.id.tv_typename,item.typename);
        }
    }
}
