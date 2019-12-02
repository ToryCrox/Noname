package com.aleaf.viplist;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.NonNull;
import android.support.design.animation.ArgbEvaluatorCompat;
import androidx.core.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.header_bg)
    View mHeaderBgView;

    @BindView(R.id.seek_bar)
    SeekBar mSeekBar;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    ColorDrawable mHeaderColorDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        mHeaderColorDrawable = new ColorDrawable(Color.parseColor("#ffdda852"));
        Drawable headerBgDrawable = getResources().getDrawable(R.drawable.bg_chara);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{mHeaderColorDrawable, headerBgDrawable});
        mHeaderBgView.setBackground(ld);

        mSeekBar.setMax(100);

        final int startColor = Color.parseColor("#ffdda852");
        final int endColor = Color.parseColor("#fff1898f");

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mHeaderColorDrawable.setColor(ArgbEvaluatorCompat.getInstance()
                        .evaluate(progress / 100f, startColor, endColor));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final List<VipViewProfile> profileList = VipProfileUtils.getVipViewProfiles(this);

        Log.d(TAG, "profileList ="+profileList);

        mViewPager.setAdapter(new VipCardAdapter(profileList));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.vip_card_view_pager_padding_h));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int startColor = profileList.get(position).headerMaskColor;
                int endColor = position >= profileList.size() - 1 ? startColor :
                        profileList.get(position + 1).headerMaskColor;
                mHeaderColorDrawable.setColor(ArgbEvaluatorCompat.getInstance()
                        .evaluate(positionOffset, startColor, endColor));
            }

            @Override
            public void onPageSelected(int i) {
                mViewPager.getCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mViewPager.setCurrentItem(1, false);

    }

    class VipCardAdapter extends PagerAdapter{

        List<VipViewProfile> profiles;
        SparseArray<VipCardContainerView> mViews = new SparseArray<>();
        public VipCardAdapter(List<VipViewProfile> list){
            profiles = list;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            VipCardContainerView view = mViews.get(position);
            if (view == null){
                view = createVipCardView(container, position);
                mViews.put(position, view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        private VipCardContainerView createVipCardView(ViewGroup container, int position){
            VipViewProfile profile = profiles.get(position);
            VipCardContainerView view = (VipCardContainerView) LayoutInflater.from(container.getContext())
                    .inflate(R.layout.layout_vip_info_card, container, false);
            view.setGradientColors(profile.cardStartColor, profile.cardEndColor);
            view.setShadowColor(profile.cardShadowColor);
            view.setCharaDrawable(ContextCompat.getDrawable(container.getContext(), profile.cardCharaDrawableRes));
            return view;
        }

        @Override
        public int getCount() {
            return profiles.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }



}
