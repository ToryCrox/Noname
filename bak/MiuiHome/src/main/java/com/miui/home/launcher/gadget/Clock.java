package com.miui.home.launcher.gadget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import java.util.Calendar;

public class Clock {
    protected Calendar mCalendar;
    protected ClockStyle mClockStyle;
    private final Context mContext;
    private Handler mHandler;
    private Runnable mTicker;
    private boolean mTickerStopped = false;
    private TimeZoneChangedReceiver mTimeZoneChangedReceiver;

    public interface ClockStyle {
        int getUpdateInterval();

        void initConfig(String str);

        void updateAppearance(Calendar calendar);
    }

    private class TimeZoneChangedReceiver extends BroadcastReceiver {
        private TimeZoneChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                Clock.this.mCalendar = Calendar.getInstance();
            }
            if (!Clock.this.mTickerStopped && Clock.this.mClockStyle != null) {
                Clock.this.updateCurTime();
            }
        }
    }

    public Clock(Context context) {
        this.mContext = context;
    }

    public void init() {
        this.mTickerStopped = false;
        this.mHandler = new Handler();
        this.mTicker = new Runnable() {
            public void run() {
                if (!Clock.this.mTickerStopped && Clock.this.mClockStyle != null) {
                    Clock.this.updateCurTime();
                    int interval = Clock.this.mClockStyle.getUpdateInterval();
                    Clock.this.mHandler.postAtTime(Clock.this.mTicker, SystemClock.uptimeMillis() + (((long) interval) - (System.currentTimeMillis() % ((long) interval))));
                }
            }
        };
    }

    public void resume() {
        this.mCalendar = Calendar.getInstance();
        this.mHandler.removeCallbacks(this.mTicker);
        this.mTickerStopped = false;
        this.mTicker.run();
    }

    public void pause() {
        this.mTickerStopped = true;
        this.mHandler.removeCallbacks(this.mTicker);
    }

    public void onStart() {
        if (this.mTimeZoneChangedReceiver == null) {
            this.mTimeZoneChangedReceiver = new TimeZoneChangedReceiver();
            this.mContext.registerReceiver(this.mTimeZoneChangedReceiver, new IntentFilter("android.intent.action.TIMEZONE_CHANGED"));
        }
    }

    public void onStop() {
        if (this.mTimeZoneChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mTimeZoneChangedReceiver);
            this.mTimeZoneChangedReceiver = null;
        }
    }

    public void setClockStyle(ClockStyle clockStyle) {
        this.mClockStyle = clockStyle;
        updateCurTime();
    }

    private void updateCurTime() {
        if (this.mClockStyle != null && this.mCalendar != null) {
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            try {
                this.mClockStyle.updateAppearance(this.mCalendar);
            } catch (Exception e) {
                Log.e("com.miui.home.launcher.gadget.Clock", e.toString());
            }
        }
    }
}
