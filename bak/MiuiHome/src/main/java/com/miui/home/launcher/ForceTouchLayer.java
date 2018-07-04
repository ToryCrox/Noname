package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.common.Ease.Cubic;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.structures.ForceTouchItem;
import java.util.ArrayList;

public class ForceTouchLayer extends FrameLayout {
    private static final float MENU_PRESSURE = DeviceConfig.FORCE_TOUCH_MENU_PRESSURE;
    private static final float TRIGGER_PRESSURE = DeviceConfig.FORCE_TOUCH_TRIGGER_PRESSURE;
    private float[] mCoords;
    private boolean mDoneCheckingForceTouchInfo;
    private ImageView mForceTouchIcon;
    private ArrayList<ForceTouchItem> mForceTouchItemArrayList;
    private LinearLayout mForceTouchMenu;
    private ForceTouchPressureCircle mForceTouchPressureCircle;
    private boolean mGPUBoosted;
    private Rect mHotSeatRect;
    private float mIconPadding;
    private boolean mInterceptedWhenPressureIsBigEnough;
    private boolean mIsIconShown;
    private boolean mIsMenuShowing;
    private boolean mIsMoved;
    private boolean mIsShakingAnimationShowing;
    private Launcher mLauncher;
    private ForceTouchTriggeredListener mListener;
    private float mMenuItemHeight;
    private float mMenuMarginIcon;
    private int mMenuVisibleItemNum;
    private float mMenuWidth;
    private float mOriginX;
    private float mOriginY;
    private Animation mShakeAnimation;
    private AnimationListener mShakeAnimationListener;
    AnimatorListenerAdapter mZoomOutAnimatorListenerAdatper;

    interface ForceTouchTriggeredListener {
        void onForceTouchFinish();

        void onForceTouchTriggered();
    }

    public ForceTouchLayer(Context context) {
        this(context, null);
    }

    public ForceTouchLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForceTouchLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mMenuVisibleItemNum = 0;
        this.mIsIconShown = false;
        this.mIsMenuShowing = false;
        this.mIsShakingAnimationShowing = false;
        this.mOriginX = -1.0f;
        this.mOriginY = -1.0f;
        this.mIsMoved = false;
        this.mHotSeatRect = new Rect();
        this.mCoords = new float[2];
        this.mInterceptedWhenPressureIsBigEnough = false;
        this.mListener = null;
        this.mShakeAnimation = null;
        this.mDoneCheckingForceTouchInfo = false;
        this.mGPUBoosted = false;
        this.mShakeAnimationListener = new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (!ForceTouchLayer.this.mIsMenuShowing) {
                    ForceTouchLayer.this.closeForceTouch();
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        };
        this.mZoomOutAnimatorListenerAdatper = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ForceTouchLayer.this.mForceTouchIcon.startAnimation(ForceTouchLayer.this.mShakeAnimation);
                ForceTouchLayer.this.mForceTouchPressureCircle.clearZoomOutAnimatorListener();
                ForceTouchLayer.this.mForceTouchPressureCircle.changeCircleSizeWhenShaking(false);
                ForceTouchLayer.this.performHapticFeedback(0, 1);
            }
        };
        this.mMenuMarginIcon = context.getResources().getDimension(R.dimen.force_touch_menu_margin_icon);
        this.mMenuItemHeight = context.getResources().getDimension(R.dimen.force_touch_menu_item_height);
        this.mMenuWidth = context.getResources().getDimension(R.dimen.force_touch_menu_item_width);
        this.mIconPadding = context.getResources().getDimension(R.dimen.force_touch_icon_padding);
        this.mLauncher = Application.getLauncherApplication(context).getLauncher();
        this.mShakeAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        this.mShakeAnimation.setStartOffset(60);
        this.mShakeAnimation.setAnimationListener(this.mShakeAnimationListener);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mForceTouchIcon = (ImageView) findViewById(R.id.force_touch_icon);
        this.mForceTouchMenu = (LinearLayout) findViewById(R.id.force_touch_menu);
        this.mForceTouchPressureCircle = (ForceTouchPressureCircle) findViewById(R.id.force_touch_pressure_circle);
        this.mForceTouchPressureCircle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ForceTouchLayer.this.mIsMenuShowing) {
                    ForceTouchLayer.this.closeForceTouch();
                }
            }
        });
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 2 && !this.mGPUBoosted) {
            Utilities.boostGPU();
            this.mGPUBoosted = true;
        }
        if (ev.getAction() == 1 && this.mGPUBoosted) {
            this.mGPUBoosted = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void startForceTouch(ItemIcon itemIcon) {
        if (itemIcon != null && (itemIcon.getTag() instanceof ShortcutInfo)) {
            this.mForceTouchIcon.setVisibility(0);
            itemIcon.setIsHideTitle(true);
            itemIcon.setIsHideTouchMask(true);
            this.mForceTouchIcon.setImageBitmap(DragController.createViewBitmap(itemIcon, 1.0f));
            itemIcon.setIsHideTitle(false);
            itemIcon.setIsHideTouchMask(false);
            this.mForceTouchIcon.setTranslationX(this.mCoords[0]);
            this.mForceTouchIcon.setTranslationY(this.mCoords[1]);
            this.mForceTouchPressureCircle.setCenterXY(this.mCoords[0] + ((float) (itemIcon.getWidth() / 2)), this.mCoords[1] + ((float) ((itemIcon.getHeight() - itemIcon.getTitle().getHeight()) / 2)));
            ShortcutInfo itemInfo = (ShortcutInfo) itemIcon.getTag();
            Context context = getContext().getApplicationContext();
            String packageName = itemInfo.getPackageName();
            String title = itemInfo.getTitle(context) != null ? itemInfo.getTitle(context).toString() : "";
            this.mForceTouchItemArrayList = Utilities.parseForceTouchStatic(packageName, title, context);
            if (this.mForceTouchItemArrayList == null || this.mForceTouchItemArrayList.size() < this.mForceTouchMenu.getChildCount()) {
                ArrayList<ForceTouchItem> dynamicItems = Utilities.parseForceTouchDynamic(packageName, title, context);
                if (this.mForceTouchItemArrayList == null) {
                    this.mForceTouchItemArrayList = dynamicItems;
                } else if (dynamicItems != null) {
                    int num = Math.min(this.mForceTouchMenu.getChildCount() - this.mForceTouchItemArrayList.size(), dynamicItems.size());
                    for (int i = 0; i < num; i++) {
                        this.mForceTouchItemArrayList.add(dynamicItems.get(i));
                    }
                }
            }
            if (!(this.mForceTouchItemArrayList == null || this.mForceTouchItemArrayList.isEmpty())) {
                AnalyticalDataCollector.trackForceTouchAdaptedApp(packageName);
            }
            this.mForceTouchPressureCircle.setIsInFolder(this.mLauncher.getFolderCling().isOpened());
            this.mForceTouchPressureCircle.setVisibility(0);
            this.mIsIconShown = true;
            if (this.mListener != null) {
                this.mListener.onForceTouchTriggered();
            }
        }
    }

    private void showForceTouchMenu(ItemIcon itemIcon) {
        if (this.mForceTouchItemArrayList != null && itemIcon != null) {
            float menuTransX;
            float menuTransY;
            this.mForceTouchMenu.setVisibility(0);
            fillInfo(this.mForceTouchItemArrayList);
            if (this.mCoords[0] > ((float) (getWidth() / 2))) {
                menuTransX = (((this.mCoords[0] + ((float) (itemIcon.getWidth() / 2))) + ((float) (itemIcon.getIcon().getWidth() / 2))) - this.mMenuWidth) - this.mIconPadding;
                this.mForceTouchMenu.setPivotX(this.mMenuWidth);
            } else {
                menuTransX = ((this.mCoords[0] + ((float) (itemIcon.getWidth() / 2))) - ((float) (itemIcon.getIcon().getWidth() / 2))) + this.mIconPadding;
                this.mForceTouchMenu.setPivotX(0.0f);
            }
            if (((this.mCoords[1] + ((float) itemIcon.getHeight())) + this.mMenuMarginIcon) + (this.mMenuItemHeight * ((float) this.mForceTouchMenu.getChildCount())) > ((float) getHeight())) {
                menuTransY = (this.mCoords[1] - this.mMenuMarginIcon) - (((float) this.mMenuVisibleItemNum) * this.mMenuItemHeight);
                this.mForceTouchMenu.setPivotY(((float) this.mMenuVisibleItemNum) * this.mMenuItemHeight);
            } else {
                menuTransY = ((this.mCoords[1] + ((float) itemIcon.getHeight())) + this.mMenuMarginIcon) - ((float) itemIcon.getTitle().getHeight());
                this.mForceTouchMenu.setPivotY(0.0f);
            }
            this.mForceTouchMenu.setTranslationX(menuTransX);
            this.mForceTouchMenu.setTranslationY(menuTransY);
            this.mForceTouchMenu.setScaleX(0.0f);
            this.mForceTouchMenu.setScaleY(0.0f);
            this.mForceTouchMenu.animate().setInterpolator(Cubic.easeOut).scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            this.mForceTouchPressureCircle.setClickable(true);
            this.mForceTouchPressureCircle.startSpreadAnimate();
            this.mIsMenuShowing = true;
        }
    }

    public void closeForceTouch() {
        this.mForceTouchIcon.setVisibility(8);
        this.mForceTouchMenu.setVisibility(8);
        this.mForceTouchPressureCircle.setVisibility(8);
        this.mForceTouchPressureCircle.setClickable(false);
        resetAll();
        if (this.mListener != null) {
            this.mListener.onForceTouchFinish();
            this.mListener = null;
        }
        for (int i = 0; i < this.mForceTouchMenu.getChildCount(); i++) {
            ((ImageView) this.mForceTouchMenu.getChildAt(i).findViewById(R.id.force_touch_item_icon)).setImageResource(0);
        }
    }

    private void resetAll() {
        this.mIsMenuShowing = false;
        this.mIsIconShown = false;
        this.mIsMoved = false;
        this.mForceTouchItemArrayList = null;
        this.mOriginX = -1.0f;
        this.mOriginY = -1.0f;
        this.mCoords[0] = -1.0f;
        this.mCoords[1] = -1.0f;
        this.mInterceptedWhenPressureIsBigEnough = false;
        this.mIsShakingAnimationShowing = false;
        this.mDoneCheckingForceTouchInfo = false;
    }

    private void fillInfo(ArrayList<ForceTouchItem> forceTouchItemArrayList) {
        int i;
        for (i = 0; i < this.mForceTouchMenu.getChildCount(); i++) {
            View child = this.mForceTouchMenu.getChildAt(i);
            this.mMenuVisibleItemNum = 0;
            child.setVisibility(8);
        }
        if (forceTouchItemArrayList != null && !forceTouchItemArrayList.isEmpty()) {
            for (i = 0; i < this.mForceTouchMenu.getChildCount(); i++) {
                child = this.mForceTouchMenu.getChildAt(i);
                if (i < forceTouchItemArrayList.size()) {
                    final ForceTouchItem forceTouchItem = (ForceTouchItem) forceTouchItemArrayList.get(i);
                    TextView title = (TextView) child.findViewById(R.id.force_touch_item_title);
                    TextView desc = (TextView) child.findViewById(R.id.force_touch_item_desc);
                    ((ImageView) child.findViewById(R.id.force_touch_item_icon)).setImageDrawable(forceTouchItem.getDrawableIcon());
                    title.setText(forceTouchItem.getTitle());
                    if (TextUtils.isEmpty(forceTouchItem.getDesc())) {
                        desc.setVisibility(8);
                    } else {
                        desc.setText(forceTouchItem.getDesc());
                    }
                    child.setVisibility(0);
                    child.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (forceTouchItem.getIntent() == null) {
                                return;
                            }
                            if (TextUtils.equals("activity", forceTouchItem.getType())) {
                                ForceTouchLayer.this.getContext().startActivity(forceTouchItem.getIntent());
                            } else if (TextUtils.equals("broadcast", forceTouchItem.getType())) {
                                ForceTouchLayer.this.getContext().sendBroadcast(forceTouchItem.getIntent());
                            } else if (TextUtils.equals("service", forceTouchItem.getType())) {
                                ForceTouchLayer.this.getContext().startService(forceTouchItem.getIntent());
                            }
                        }
                    });
                    if (i == 0) {
                        if (i == forceTouchItemArrayList.size() - 1) {
                            child.setBackground(getContext().getResources().getDrawable(R.drawable.force_touch_menu_only_one));
                        } else {
                            child.setBackground(getContext().getResources().getDrawable(R.drawable.force_touch_menu_top));
                        }
                    } else if (i == forceTouchItemArrayList.size() - 1) {
                        child.setBackground(getContext().getResources().getDrawable(R.drawable.force_touch_menu_bottom));
                    } else {
                        child.setBackground(getContext().getResources().getDrawable(R.drawable.force_touch_menu_center));
                    }
                    this.mMenuVisibleItemNum++;
                }
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!DeviceConfig.isSupportForceTouch(getContext()) || this.mLauncher.isInNormalEditing()) {
            return super.onInterceptTouchEvent(ev);
        }
        if (this.mIsShakingAnimationShowing) {
            this.mInterceptedWhenPressureIsBigEnough = false;
            return true;
        }
        if (ev.getAction() == 0 && !this.mIsIconShown) {
            resetAll();
            this.mOriginX = ev.getRawX();
            this.mOriginY = ev.getRawY();
        }
        if (ev.getAction() == 2) {
            calculateIsMoved(ev);
            if (!(ev.getPressure() <= TRIGGER_PRESSURE || this.mIsMoved || getTouchedItemIconAndSetListener() == null)) {
                this.mInterceptedWhenPressureIsBigEnough = true;
                return true;
            }
        }
        if (ev.getAction() == 1 && !this.mIsMenuShowing) {
            closeForceTouch();
        }
        return false;
    }

    private void calculateIsMoved(MotionEvent event) {
        boolean z = this.mIsMoved || Math.abs(event.getRawX() - this.mOriginX) > 20.0f || Math.abs(event.getRawY() - this.mOriginY) > 20.0f;
        this.mIsMoved = z;
    }

    private void shakeIcon() {
        this.mForceTouchPressureCircle.setZoomOutAnimatorListenerAdapter(this.mZoomOutAnimatorListenerAdatper);
        this.mForceTouchPressureCircle.changeCircleSizeWhenShaking(true);
        this.mIsShakingAnimationShowing = true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (!this.mIsShakingAnimationShowing) {
            if (event.getAction() == 2 && this.mInterceptedWhenPressureIsBigEnough) {
                if (event.getPressure() >= TRIGGER_PRESSURE) {
                    if (!(this.mIsIconShown || this.mDoneCheckingForceTouchInfo)) {
                        ItemIcon itemIcon = getTouchedItemIconAndSetListener();
                        if (itemIcon != null) {
                            Utilities.getDescendantCoordRelativeToAncestor(itemIcon, this, this.mCoords, true, false);
                            startForceTouch(itemIcon);
                        }
                        if (this.mForceTouchItemArrayList == null && this.mIsIconShown) {
                            shakeIcon();
                        }
                        this.mDoneCheckingForceTouchInfo = true;
                    }
                    if (event.getPressure() < MENU_PRESSURE && this.mIsIconShown && this.mForceTouchItemArrayList != null && !this.mIsMenuShowing) {
                        this.mForceTouchPressureCircle.setPressure(event.getPressure(), TRIGGER_PRESSURE, MENU_PRESSURE);
                        return true;
                    }
                }
                if (!(event.getPressure() < MENU_PRESSURE || this.mForceTouchItemArrayList == null || this.mIsMenuShowing)) {
                    showForceTouchMenu(getTouchedItemIconAndSetListener());
                    return true;
                }
            }
            if (event.getAction() == 1 && this.mInterceptedWhenPressureIsBigEnough && !this.mIsMenuShowing) {
                closeForceTouch();
            }
            return false;
        } else if (event.getAction() != 1) {
            return true;
        } else {
            this.mForceTouchPressureCircle.cancelZoomOutAnimation();
            return true;
        }
    }

    public boolean isInterceptedByForceTouchLayer() {
        return this.mInterceptedWhenPressureIsBigEnough;
    }

    private ItemIcon getTouchedItemIconAndSetListener() {
        View view = null;
        HotSeats hotSeats = this.mLauncher.getHotSeats();
        hotSeats.getHitRect(this.mHotSeatRect);
        FolderCling folderCling = this.mLauncher.getFolderCling();
        this.mListener = null;
        if (folderCling.isOpened()) {
            view = folderCling.getForceTouchSelectedView();
            this.mListener = folderCling;
        } else if (this.mHotSeatRect.contains((int) this.mOriginX, (int) this.mOriginY)) {
            view = hotSeats.getForceTouchSelectedView();
            this.mListener = hotSeats;
        } else {
            CellLayout cellLayout = this.mLauncher.getWorkspace().getCurrentCellLayout();
            if (cellLayout != null) {
                view = ((CellInfo) cellLayout.getTag()).cell;
                this.mListener = cellLayout;
            }
        }
        if (view == null || !(view instanceof ItemIcon)) {
            return null;
        }
        return (ItemIcon) view;
    }

    public boolean isShowing() {
        return this.mIsMenuShowing || this.mIsIconShown;
    }
}
