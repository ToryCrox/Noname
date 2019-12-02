package com.tory.iconpacklauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tory.library.recycler.BaseRecyclerAdapter;
import com.tory.library.recycler.BaseViewHolder;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements BaseRecyclerAdapter.OnRecyclerViewItemClickListener {

    private static final int PHOTO_REQUEST_CHOOSE = 100;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    AppInfoAdpater mAppInfoAdpater;

    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        setDisplayHomeAsUpEnabled(false);
        mAppInfoAdpater = new AppInfoAdpater();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
        mRecyclerView.setAdapter(mAppInfoAdpater);
        mAppInfoAdpater.setOnRecyclerViewItemClickListener(this);
    }

    @Override
    public void doBusiness() {
        UserManager mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);
        List<UserHandle> users = mUserManager.getUserProfiles();
        for (UserHandle user : users) {
            try{
                L.e(TAG, "user="+user+
                    ", isUserUnlocked="+mUserManager.isUserUnlocked(user)
                        +", isQuietModeEnabled="+mUserManager.isQuietModeEnabled(user));
            } catch (Exception e){
                L.e(TAG, "user="+user);
            }
        }

        //intent.addCategory("com.novalauncher.category.CUSTOM_ICON_PICKER");
        PackageManager pm = getPackageManager();
        ArrayList<AppInfo> appInfos = new ArrayList<>();
        loadAppInfo(pm, appInfos, new Intent("com.novalauncher.THEME"));
        loadAppInfo(pm, appInfos, new Intent("com.gau.go.launcherex.theme"));
        loadAppInfo(pm, appInfos, new Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME"));
        loadAppInfo(pm, appInfos, new Intent("org.adw.launcher.icons.ACTION_PICK_ICON"));


        appInfos.add(new AppInfo("无主题", "", 0,
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round)));
        Collections.sort(appInfos);
        mAppInfoAdpater.addAll(appInfos);
        mAppInfoAdpater.notifyDataSetChanged();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage("com.transsion.hilauncher");
        List<ResolveInfo> infos = pm.queryIntentActivities(mainIntent, PackageManager.GET_DISABLED_COMPONENTS);
        Log.d(TAG, "doBusiness: infos="+infos);

    }

    public ArrayList<AppInfo> loadAppInfo(@NonNull PackageManager pm,@NonNull ArrayList<AppInfo> appInfos,
                                          @NonNull Intent intent){
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkg = resolveInfo.activityInfo.packageName;
            if(!resolveInfo.activityInfo.exported || contains(appInfos, pkg)){
                continue;
            }
            String name = resolveInfo.activityInfo.loadLabel(pm).toString();
            Drawable icon = resolveInfo.activityInfo.loadIcon(pm);
            int versionCode = 0;
            try {
                versionCode = pm.getPackageInfo(pkg, 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appInfos.add(new AppInfo(name, pkg, versionCode, icon));
        }
        L.d("doBusiness apps="+appInfos);
        return appInfos;
    }

    public boolean contains(List<AppInfo> list, String pkg){
        for (AppInfo appInfo : list) {
            if(TextUtils.equals(appInfo.pkg, pkg)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onItemClick(View v, int position) {
        int type = mAppInfoAdpater.getItemViewType(position);
        if(type == AppInfoAdpater.ITYE_GET_MORE){
            if(position == mAppInfoAdpater.getItemCount() - 2){
                //choseIcon();
                startActivity(new Intent(this, LauncherAppActivity.class));
            }else{
                turnToAppMarket();
            }
        }else{
            AppInfo info = mAppInfoAdpater.getItem(position);
            Intent intent = new Intent(this, IconPacksActivity.class);
            intent.putExtra(IconPacksActivity.EXTRAL_PKG,info.pkg);
            startActivity(intent);
        }
    }

    private void choseIcon() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, PHOTO_REQUEST_CHOOSE);
    }

    public void turnToAppMarket(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://search?q=icon pack")); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "您的系统中没有安装应用市场", Toast.LENGTH_SHORT).show();
        }
    }

    private void parsePackge(final AppInfo item) {
        String pkg = item.pkg;


    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(PHOTO_REQUEST_CHOOSE == requestCode && data != null && data.getData() != null){

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            try {
                /*ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(data.getData(),"r");
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());*/

                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                Log.d(TAG, "onActivityResult: bitmap "+ Arrays.toString(new int[]{bitmap.getWidth(),bitmap.getHeight()}));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    class AppInfoAdpater extends BaseRecyclerAdapter<AppInfo>{

        protected static final int ITYE_GET_MORE = 1;

        public AppInfoAdpater() {
            super(R.layout.item_app_info);
        }

        @Override
        public int getItemCount() {
            return mData.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position >= getItemCount() - 2 ? ITYE_GET_MORE : TYPE_ITEM;
        }

        @Override
        protected int getLayoutId(int viewType) {
            return viewType == ITYE_GET_MORE ? R.layout.item_text : super.getLayoutId(viewType);
        }


        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            int viewType = holder.getItemViewType();
            if(viewType == ITYE_GET_MORE){
                holder.setText(R.id.title, "获取更多");
            }else{
                convert(holder, mData.get(position));
            }
        }

        @Override
        protected void convert(BaseViewHolder holder, AppInfo item) {
            holder.setText(R.id.title, item.name)
                    .setText(R.id.summary,item.pkg + "; vc="+item.versionCode)
                    .setImageDrawable(R.id.image, item.icon);
        }
    }


}
