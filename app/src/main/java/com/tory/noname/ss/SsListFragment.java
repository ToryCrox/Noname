package com.tory.noname.ss;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;
import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;
import com.tory.library.utils.FileUtils;
import com.tory.noname.R;
import com.tory.noname.main.base.BasePageFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SsListFragment extends BasePageFragment {
    public static final String FRAGMENT_TAG = "SsListFragment";
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter mRecyclerAdpater;

    private List<Server> mServers = new ArrayList<>();
    Handler mHandler = new Handler();


    public SsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment SsListFragment.
     */
    public static SsListFragment newInstance() {
        SsListFragment fragment = new SsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void fetchData() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ss_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parseServers();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerAdpater = new ServerAdapater();
        mRecyclerView.setAdapter(mRecyclerAdpater);
        mRecyclerAdpater.addAll(mServers);

        Button pingStart = (Button) view.findViewById(R.id.ping_start);
        pingStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pingStart();
            }
        });

        
    }

    private void parseServers() {
        try {
            String json = FileUtils.readAssets(getContext(),"gui-config.json");
            String configs = JSONObject.parseObject(json).getString("configs");
            List<Server> servers = JSONObject.parseArray(configs,Server.class);
            mServers.addAll(servers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pingStart(){
        ExecutorService executors = Executors.newCachedThreadPool();
        for (final Server server : mServers) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    server.delay = PingUtil.ping(server.server);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerAdpater.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    public static class ServerAdapater extends BaseRecyclerAdapter<Server>{

        public ServerAdapater() {
            super(R.layout.item_ss_server);
        }

        @Override
        protected void convert(BaseViewHolder holder, Server item) {
            holder.setText(R.id.server_name,item.remarks)
                    .setText(R.id.server_ip,item.server)
                    .setText(R.id.server_delay,item.delay);

        }
    }
}
