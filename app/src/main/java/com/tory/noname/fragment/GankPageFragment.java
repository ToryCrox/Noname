package com.tory.noname.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tory.noname.R;


public class GankPageFragment extends Fragment {

    private static final String ARG_TYPE = "arg_type";

    private String mType;

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
            mType = getArguments().getString(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gank_page, container, false);
    }
}
