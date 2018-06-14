package com.mimikko.buglytest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "BUGLY_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ((TextView)findViewById(R.id.text)).setText("pathed version="+BuildConfig.VERSION_CODE);

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "pathed "+BuildConfig.VERSION_CODE,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
