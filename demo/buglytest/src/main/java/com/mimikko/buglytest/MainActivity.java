package com.mimikko.buglytest;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.bugly.beta.Beta;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "BUGLY_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ((TextView)findViewById(R.id.text)).setText("new pathed version="+BuildConfig.VERSION_CODE);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Beta.checkUpgrade();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Beta.downloadPatch();
            }
        });


        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "pathed "+BuildConfig.VERSION_CODE,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
