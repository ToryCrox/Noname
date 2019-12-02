package com.tory.netexe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.tory.library.utils.SerializationHelper;
import com.tory.library.utils.TimeRecorder;

import java.util.List;

import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<BanInfo> list = SerializationHelper.fromFileJsonList(this, R.raw.bans, BanInfo.class);

        DiskCacheManager.getInstance(this)
                .put("bans", list);

        String ba = DiskCacheManager.getInstance(this).getString("bans");

        TimeRecorder.begin("loadCache");
        final Disposable subscribe = DiskCacheManager.getInstance(this)
                .getObservableList("bans", BanInfo.class)
                .subscribe(ll -> {
                            Log.d("MainActivity", "onCreate: " + ll);
                            TimeRecorder.end("loadCache");
                        }
                );
    }
}
