package com.tory.debug;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TimeRecorder.begin("MainActivity#onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TimeRecorder.end("MainActivity#onCreate");
    }
}
