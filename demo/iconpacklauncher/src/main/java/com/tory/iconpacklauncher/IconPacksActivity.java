package com.tory.iconpacklauncher;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.support.v4.util.LruCache;
import androidx.appcompat.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author tao.xu2
 * @date 2018/1/24
 */

public class IconPacksActivity extends BaseActivity implements BaseRecyclerAdapter.OnRecyclerViewItemClickListener {

    public static final String EXTRAL_PKG = "EXTRAL_PKG";

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    IconAdapter mAdapter;

    IconPackTheme mTheme;
    LruCache<String, Drawable> mBitmapCache;

    long mSpentParseTime;

    PackageManager mPm;

    @Override
    public int bindLayout() {
        return R.layout.activity_icon_packs;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

        setToolbarScrolled(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
        mAdapter = new IconAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnRecyclerViewItemClickListener(this);

        String pkg = getIntent().getStringExtra(EXTRAL_PKG);
        if(!TextUtils.isEmpty(pkg)){
            long t1 = SystemClock.elapsedRealtime();
            mTheme = IconPackTheme.getInstance();
            IconPackHelper.parseIconPack(this, pkg, mTheme);
            mSpentParseTime = SystemClock.elapsedRealtime() - t1;
        }

        int maxSize = (int) Runtime.getRuntime().maxMemory();// 返回byte
        int cacheSize = maxSize / 1024 / 8;
        // 创建LruCache时需要提供缓存的最大容量
        mBitmapCache = new LruCache<String, Drawable>(50) {
            @Override
            protected int sizeOf(String key, Drawable value) {
                return 1;
            }
            @Override
            protected void entryRemoved(boolean evicted, String key, Drawable oldValue, Drawable newValue) {
                // 当调用put或remove时触发此方法，可以在这里完成一些资源回收的操作
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    @Override
    public void doBusiness() {
        mPm = getPackageManager();
        showAllActiivties();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("所有图标");
        menu.add("所有应用");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("所有图标")){
            showAllIcons();
        }else{
            showAllActiivties();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAllIcons() {
        String summary = "versionCode="+mTheme.mVersionCode
                +", scale="+mTheme.scale
                +", iconSize="+mTheme.iconResNames.size()
                + ", spent="+mSpentParseTime;
        ArrayList<IconInfo> iconInfos = new ArrayList<>();
        Drawable defalut = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round);
        iconInfos.add(new IconInfo(mTheme.mPackageName,summary, defalut));
        iconInfos.add(new IconInfo("iconback", "", mTheme.iconbackResId));
        iconInfos.add(new IconInfo("iconmask", "", mTheme.iconmaskResId));
        iconInfos.add(new IconInfo("iconupon", "", mTheme.iconuponResId));

        for (Map.Entry<ComponentName, SparseIntArray> entry : mTheme.calendarPacks.entrySet()) {
            ComponentName cn = entry.getKey();
            SparseIntArray icons = entry.getValue();
            for (int i = 0; i < icons.size(); i++) {
                iconInfos.add(new IconInfo(cn, icons.valueAt(i)));
            }
        }

        ArrayList<IconInfo> iconPackInfos = new ArrayList<>();
        for (Map.Entry<ComponentName, Integer> entry : mTheme.iconPacks.entrySet()) {
            ComponentName cn = entry.getKey();
            int resId = entry.getValue();
            iconPackInfos.add(new IconInfo(cn, resId));
        }
        Collections.sort(iconPackInfos);
        iconInfos.addAll(iconPackInfos);

        mAdapter.clear();
        mAdapter.addAll(iconInfos);
        mAdapter.notifyDataSetChanged();
    }

    private void showAllActiivties(){
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = mPm;
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent,0);
        ArrayList<IconInfo> iconInfos = new ArrayList<>();
        for (ResolveInfo info : list) {
            iconInfos.add(new IconInfo(info.loadLabel(pm).toString(), info));
        }
        mAdapter.clear();
        mAdapter.addAll(iconInfos);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(View v, int position) {
        IconInfo iconInfo = mAdapter.getItem(position);
        String msg = "ComponetName="+ (iconInfo.cn != null ? iconInfo.cn.flattenToShortString() : iconInfo.title)+"\n"+
                "iconResId="+Integer.toHexString(iconInfo.iconId)+"\n"+
                "iconName="+mTheme.iconResNames.get(iconInfo.iconId)+"\n"+
                "iconSize="+iconInfo.iconSize;
        AlertDialog.Builder builder= new AlertDialog.Builder(this)
                .setTitle("信息")
                .setMessage(msg)
                .setCancelable(true);
        builder.show();
    }


    static class IconInfo implements Comparable{
        String title;
        String summary;
        int iconId;
        Drawable icon;
        ComponentName cn;
        Point iconSize = new Point();

        ResolveInfo resolveInfo;
        public IconInfo(String title, ResolveInfo info){
            this.title = title;
            this.summary = info.activityInfo.packageName;
            this.resolveInfo = info;
            this.cn = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        }

        public IconInfo( ComponentName cn, int iconId) {
            this.iconId = iconId;
            this.cn = cn;
            this.title = cn.getPackageName();
            this.summary = cn.getClassName();
        }

        public IconInfo(String title, String summary, int iconId) {
            this.title = title;
            this.summary = summary;
            this.iconId = iconId;
        }

        public IconInfo(String title, String summary, Drawable icon) {
            this.title = title;
            this.summary = summary;
            this.icon = icon;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            if(!(o instanceof IconInfo)) return 0;
            IconInfo other = (IconInfo) o;
            int a = title.compareTo(other.title);
            if(a != 0){
                return a;
            }
            return summary.compareTo(other.summary);
        }
    }

    class IconAdapter extends BaseRecyclerAdapter<IconInfo>{

        public IconAdapter() {
            super(R.layout.item_app_info);
        }

        @Override
        protected void convert(BaseViewHolder holder, IconInfo item, int position) {
            holder.setText(R.id.title, position +"." + item.title)
                    .setText(R.id.summary, item.summary);
            Drawable icon = item.icon;
            if(icon == null){

                if(mTheme == null){
                    icon = item.resolveInfo.loadIcon(mPm);
                }else  if(item.resolveInfo != null){
                    Bitmap bitmap;
                    bitmap = mTheme.getCalendarIcon(item.cn);
                    if(bitmap == null){
                        bitmap = mTheme.getIcon(item.cn);
                    }
                    if(bitmap == null){
                        bitmap = IconPackHelper.getThirdPartApp(item.resolveInfo.loadIcon(mPm), mTheme);
                    }
                    icon = new BitmapDrawable(bitmap);
                    item.icon = icon;
                }else if(item.iconId != 0){
                    String key = String.valueOf(item.iconId);
                    icon = mBitmapCache.get(key);
                    if(icon == null){
                        icon = new BitmapDrawable(mTheme.decodeBitmap(item.iconId));
                    }
                }else{
                    icon = ContextCompat.getDrawable(IconPacksActivity.this, R.mipmap.ic_launcher_round);
                }
            }
            item.iconSize.set(icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            holder.setImageDrawable(R.id.image, icon);
        }
    }
}
