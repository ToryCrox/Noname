package com.tory.iconpacklauncher;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;
import com.tory.library.utils.FileUtils;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class LauncherAppActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    LauncherAppAdapter mAdapter;

    public final List<String> FILTER_APPS = Arrays.asList(
            "电话", "短信", "浏览器", "相机"
    );

    @Override
    public int bindLayout() {
        return R.layout.activity_launcher_app;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

        setToolbarScrolled(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
        mAdapter = new LauncherAppAdapter();
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void doBusiness() {

        final PackageManager pm = getPackageManager();
        Disposable d = Observable.just(pm)
                .flatMapIterable((Function<PackageManager, Iterable<ResolveInfo>>) packageManager -> {
                    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> infos = packageManager.queryIntentActivities(mainIntent, 0);
                    return infos;
                }).map(resolveInfo -> new LauncherActivityInfo(getApplicationContext(), pm, resolveInfo))
                .filter(LauncherActivityInfo::isSystemApp)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mAdapter.addAll(list));

        /*final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(mainIntent, 0);
        List<LauncherActivityInfo> list =
                new ArrayList<LauncherActivityInfo>(infos.size());
        for (ResolveInfo info : infos) {
            LauncherActivityInfo appInfo = new LauncherActivityInfo(getApplicationContext(), pm, info);
            list.add(appInfo);
        }
        mAdapter.addAll(list);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("导出");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("导出")){

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                export();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1111);
            }

        }
        return super.onOptionsItemSelected(item);
    }


    private void export(){

        String format = "%-20s\t<folderapp launcher:componentName=\"%s\"/>";
        ArrayList<String> strs = new ArrayList<>();
        for (LauncherActivityInfo activityInfo : mAdapter.getAll()) {
            if(FILTER_APPS.contains(activityInfo.loadLable())){
                continue;
            }
            strs.add(String.format(format, activityInfo.loadLable(), activityInfo.loadCompnent()));
        }
        String target = TextUtils.join("\n", strs);
        File dest = new File(FileUtils.getSDPath(), "systemApp.txt");
        try {
            FileUtils.writeString(target, dest);
            FileUtils.notifyMediaScanFile(this, dest.getPath());
        } catch (IOException e) {
            Toast.makeText(this, "export error", Toast.LENGTH_SHORT);
        }
    }


    class LauncherAppAdapter extends BaseRecyclerAdapter<LauncherActivityInfo>{

        public LauncherAppAdapter() {
            super(R.layout.item_app_info);
        }

        @Override
        protected void convert(BaseViewHolder holder, LauncherActivityInfo item) {
            holder.setText(R.id.title, item.loadLable())
                    .setText(R.id.summary, item.loadCompnent())
                    .setImageDrawable(R.id.image, item.loadIcon());
        }
    }
}
