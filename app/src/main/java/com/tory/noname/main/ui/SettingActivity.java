package com.tory.noname.main.ui;

import android.os.Bundle;

import com.tory.noname.R;
import com.tory.noname.main.base.BaseActivity;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
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
