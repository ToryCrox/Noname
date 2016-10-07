package com.tory.noname.activity;

import android.os.Bundle;

import com.tory.noname.R;
import com.tory.noname.activity.base.BaseActivity;
import com.tory.noname.fragment.SettingsFragment;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            getFragmentManager().beginTransaction()
                    .add(R.id.frame_content,SettingsFragment.newInstance(),SettingsFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_content_main;
    }

    @Override
    public void initView() {


    }

    @Override
    public void doBusiness() {

    }
}
