package com.tory.noname.activity;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import com.tory.noname.R;
import com.tory.noname.activity.base.BaseActivity;
import com.tory.noname.fragment.BaseFragment;
import com.tory.noname.utils.L;
import com.tory.noname.utils.StatusBarHelper;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {

        initToolbar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        StatusBarHelper.setColorForDrawerLayout(this,mDrawerLayout, ContextCompat.getColor(this,R.color.colorPrimary));

        initDefalutFragment();
    }

    @Override
    public void doBusiness() {

    }



    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_ganhuo) {
            showGankListFragment(true);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        item.setChecked(true);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initDefalutFragment() {
        mNavigationView.setCheckedItem(R.id.nav_ganhuo);
        showGankListFragment(true);
    }

    public void showGankListFragment(boolean show){
        showFragment(BaseFragment.TAG_GANK_LIST_FRAGMENT,show,false);
    }

    public void showFragment(String tag, boolean show, boolean executeImmediately) {
        //Trace.beginSection("showFragment - " + tag);
        final FragmentManager fm = getSupportFragmentManager();

        if (fm == null) {
            L.w(TAG, "Fragment manager is null for : " + tag);
            return;
        }

        Fragment fragment = fm.findFragmentByTag(tag);
        if (!show && fragment == null) {
            // Nothing to show, so bail early.
            return;
        }

        final FragmentTransaction transaction = fm.beginTransaction();
        if (show) {
            if (fragment == null) {
                L.d(TAG, "showFragment: fragment need create: "+tag);
                fragment = BaseFragment.createNewFragmentForTag(tag);
                transaction.add(R.id.frame_content, fragment, tag);
            } else {
                L.d(TAG, "showFragment: fragment is all ready created "+tag);
                transaction.show(fragment);
            }
        } else {
            transaction.hide(fragment);
        }

        transaction.commitAllowingStateLoss();
        if (executeImmediately) {
            fm.executePendingTransactions();
        }
        //Trace.endSection();
    }
}
