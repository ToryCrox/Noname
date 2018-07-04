package com.miui.home.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.transitioneffects.TransitionEffectSwitcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransitionEffectThumbnailView extends ThumbnailView implements OnClickListener {
    private float mCameraDistanceCache;
    private Context mContext;
    private int mCurrentSelectedEffect;
    private LayoutInflater mInflater;
    private Launcher mLauncher;
    private Resources mResources;
    private ArrayList<String> mTransEffectList;
    private Map<String, Integer> mTransEffectMap;

    public TransitionEffectThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransitionEffectThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentSelectedEffect = -1;
        this.mTransEffectMap = new HashMap();
        this.mTransEffectList = new ArrayList();
        this.mCameraDistanceCache = 0.0f;
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mResources = context.getResources();
        initTransitionTypeThumbnailView();
        setOnClickListener(this);
    }

    private void initTransitionTypeThumbnailView() {
        setScrollWholeScreen(true);
        setScreenTransitionType(10);
        setScreenLayoutMode(5);
        setLayoutScreenSeamless(true);
        LayoutParams params = new LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.slide_bar_height));
        params.width = -1;
        params.gravity = 80;
        setSlideBarPosition(params, R.drawable.editing_mode_slidebar_fg, 0, true);
        if (this.mSlideBar != null) {
            this.mSlideBar.setOnTouchListener(null);
        }
        String[] transEffects = this.mResources.getStringArray(R.array.transformation_entries);
        String[] transEffectValues = this.mResources.getStringArray(R.array.transformation_values);
        for (int i = 0; i < transEffects.length; i++) {
            this.mTransEffectList.add(transEffects[i]);
            this.mTransEffectMap.put(transEffects[i], Integer.valueOf(transEffectValues[i]));
        }
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void reLoadThumbnails() {
        removeAllScreens();
        this.mCurrentSelectedEffect = -1;
        for (int i = 0; i < this.mTransEffectList.size(); i++) {
            View thumbnail = getView(i, null);
            if (thumbnail != null) {
                WallpaperUtils.onAddViewToGroup(this, thumbnail, true);
                addView(thumbnail);
                thumbnail.setOnClickListener(this);
            }
        }
        setCurrentScreen(0);
    }

    protected void adaptThumbnailItemStyle() {
        for (int i = 0; i < getScreenCount(); i++) {
            setIconDrawable(getScreen(i));
        }
    }

    public View getView(int position, View convertView) {
        AutoLayoutThumbnailItem resultView = (AutoLayoutThumbnailItem) this.mInflater.inflate(R.layout.thumbnail_item, null);
        if (position < 0 || position > this.mTransEffectMap.size()) {
            return null;
        }
        String transType = (String) this.mTransEffectList.get(position);
        int type = ((Integer) this.mTransEffectMap.get(transType)).intValue();
        resultView.setTag(Integer.valueOf(type));
        TextView title = (TextView) resultView.findViewById(R.id.title);
        title.setText(transType);
        if (type == this.mLauncher.getWorkspacePreviousTransitionType()) {
            setCurrentSelected(title);
            this.mLauncher.appendWorkspaceTransitionType(type);
            this.mCurrentSelectedEffect = type;
        }
        ViewGroup.LayoutParams lp = ((ImageView) resultView.findViewById(R.id.icon)).getLayoutParams();
        lp.width = -2;
        lp.height = -2;
        setIconDrawable(resultView);
        return resultView;
    }

    private void setIconDrawable(View thumbnail) {
        ImageView background = (ImageView) thumbnail.findViewById(R.id.background);
        if (background != null) {
            background.setImageDrawable(this.mContext.getResources().getDrawable(ThumbnailViewAdapter.THUMBNAIL_BACKGROUND[ThumbnailView.CURR_ICON_DRAWABLE_INDEX]));
            background.getDrawable().mutate();
        }
        ((ImageView) thumbnail.findViewById(R.id.icon)).setImageDrawable(this.mContext.getResources().getDrawable(TransitionEffectSwitcher.mEffectsDrawableIds[((Integer) thumbnail.getTag()).intValue()]));
    }

    public void onClick(View v) {
        if (isShown() && !this.mLauncher.isPrivacyModeEnabled()) {
            if (this.mLauncher.isFolderShowing()) {
                this.mLauncher.closeFolder();
            }
            int type = ((Integer) v.getTag()).intValue();
            if (this.mCurrentSelectedEffect != type) {
                this.mLauncher.removeWorkspaceTransitionType(this.mCurrentSelectedEffect);
                this.mLauncher.appendWorkspaceTransitionType(type);
                this.mCurrentSelectedEffect = type;
                TextView selected = (TextView) v.findViewById(R.id.title);
                selected.setVisibility(0);
                setCurrentSelected(selected);
            } else if (this.mLauncher.isShowingTransitionEffectDemo()) {
                return;
            }
            this.mLauncher.autoScrollWorkspace();
        }
    }

    public String getTransitionEffectName(int type) {
        for (String transEffect : this.mTransEffectMap.keySet()) {
            if (type == ((Integer) this.mTransEffectMap.get(transEffect)).intValue()) {
                return transEffect;
            }
        }
        return null;
    }

    public void setCameraDistance(float distance) {
        if (distance != this.mCameraDistanceCache) {
            this.mCameraDistanceCache = distance;
            super.setCameraDistance(this.mCameraDistanceCache);
        }
    }
}
