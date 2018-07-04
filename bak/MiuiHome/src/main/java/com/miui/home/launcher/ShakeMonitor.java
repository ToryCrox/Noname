package com.miui.home.launcher;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeMonitor implements SensorEventListener {
    private long mLastShakeTime = -1;
    private float mLastShakeX = 0.0f;
    private ShakeConfirmListener mListener;
    private int mShakeCounter = -1;

    public interface ShakeConfirmListener {
        void onShake();
    }

    public void start(Context context, ShakeConfirmListener listener) {
        SensorManager sm = (SensorManager) context.getSystemService("sensor");
        if (sm != null) {
            sm.registerListener(this, sm.getDefaultSensor(1), 2);
            this.mListener = listener;
        }
    }

    public void stop(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService("sensor");
        if (sm != null) {
            sm.unregisterListener(this);
            this.mListener = null;
            this.mShakeCounter = -1;
        }
    }

    public void onSensorChanged(SensorEvent event) {
        float currX = event.values[0];
        if (this.mListener != null) {
            if (this.mShakeCounter == -1) {
                this.mLastShakeX = currX;
                this.mShakeCounter = 0;
                return;
            }
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - this.mLastShakeTime;
            if (Math.abs(currX - this.mLastShakeX) > 5.0f) {
                if (this.mLastShakeTime == -1) {
                    this.mShakeCounter++;
                    this.mLastShakeTime = currentTime;
                } else if (deltaTime > 300 && deltaTime < 600) {
                    this.mShakeCounter++;
                    this.mLastShakeTime = currentTime;
                    if (this.mShakeCounter == 3) {
                        this.mListener.onShake();
                        this.mShakeCounter = -1;
                        this.mLastShakeTime = -1;
                    }
                } else if (deltaTime > 900) {
                    this.mShakeCounter = -1;
                    this.mLastShakeTime = -1;
                }
            } else if (deltaTime > 600) {
                this.mShakeCounter = -1;
                this.mLastShakeTime = -1;
            }
            this.mLastShakeX = currX;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
