package com.aleaf.launcherimport;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    List<HomeAppSupportInfo> mAppSupportInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAppSupportInfos = HomeImportUtils.loadHomeAppSupportInfos(this);
        Log.d(TAG, "onCreate: mAppSupportInfos="+mAppSupportInfos);
        for (HomeAppSupportInfo info : mAppSupportInfos) {
            Log.d(TAG, "onCreate: title="+info.title);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                get();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getAthority(){
        return "com.miui.home.launcher.settings";
    }

    private Uri getContentUri(){
        return Uri.parse("content://" +
                getAthority() + "/" + "favorites");
    }


    private void get(){
        if (mAppSupportInfos.size() > 0){
            Log.d(TAG, "get: "+ HomeImportUtils.checkPermission(this, mAppSupportInfos.get(0)));
        }

        Uri uri = getContentUri();
        final ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                uri, null, null, null, null);

        if (cursor == null){
            Log.d(TAG, "cursor is null");
        } else {
            int titleIndex = cursor.getColumnIndexOrThrow("title");
            int containerIndex = cursor.getColumnIndexOrThrow("container");
            int itemTypeIndex = cursor.getColumnIndexOrThrow("itemType");
            int screenIndex = cursor.getColumnIndexOrThrow("screen");
            int cellXIndex = cursor.getColumnIndexOrThrow("cellX");
            int cellYIndex = cursor.getColumnIndexOrThrow("cellY");
            int intentIndex = cursor.getColumnIndexOrThrow("intent");

            while(cursor.moveToNext()){
                String title = cursor.getString(titleIndex);
                String intent = cursor.getString(intentIndex);
                int cellX = cursor.getInt(cellXIndex);
                int cellY = cursor.getInt(cellYIndex);
                int container = cursor.getInt(containerIndex);
                int itemType = cursor.getInt(itemTypeIndex);
                int screen = cursor.getInt(screenIndex);
                Log.d(TAG, "title="+title+", cellX="+cellX+", cellY="+cellY+", container="+container
                        +", itemType="+itemType+", screen="+screen);
            }
        }
    }
}
