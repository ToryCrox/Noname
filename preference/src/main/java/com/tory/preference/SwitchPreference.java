package com.tory.preference;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;

/**
 * @Author: Tory
 * Create: 2016/10/1
 */
public class SwitchPreference extends TwoStatePreference {

    private final Listener mListener = new Listener();

    // Switch text for on and off states
    private CharSequence mSwitchOn = "";
    private CharSequence mSwitchOff = "";

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            SwitchPreference.this.setChecked(isChecked);
        }
    }

    public SwitchPreference(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);

    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int i) {
        /*TypedArray a = context.obtainStyledAttributes(attrs,
                android.R.styleable.SwitchPreference, defStyleAttr,0);
        setSummaryOn(a.getString(com.android.internal.R.styleable.SwitchPreference_summaryOn));
        setSummaryOff(a.getString(com.android.internal.R.styleable.SwitchPreference_summaryOff));
        setSwitchTextOn(a.getString(
                com.android.internal.R.styleable.SwitchPreference_switchTextOn));
        setSwitchTextOff(a.getString(
                com.android.internal.R.styleable.SwitchPreference_switchTextOff));
        setDisableDependentsState(a.getBoolean(
                com.android.internal.R.styleable.SwitchPreference_disableDependentsState, false));
        a.recycle();*/
        setWidgetLayoutResource(R.layout.preference_widget_switch);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View checkableView = view.findViewById(R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            if (checkableView instanceof CompoundButton) {
                final CompoundButton switchView = (CompoundButton) checkableView;
                switchView.setOnCheckedChangeListener(null);
            }

            ((Checkable) checkableView).setChecked(mChecked);

            if (checkableView instanceof SwitchCompat) {
                final SwitchCompat switchView = (SwitchCompat) checkableView;
                switchView.setTextOn(mSwitchOn);
                switchView.setTextOff(mSwitchOff);
                switchView.setOnCheckedChangeListener(mListener);
            }
        }

        syncSummaryView(view);
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param onText Text to display in the on state
     */
    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param offText Text to display in the off state
     */
    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }
}
