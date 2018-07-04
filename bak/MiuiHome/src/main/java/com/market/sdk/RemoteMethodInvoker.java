package com.market.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.market.sdk.IMarketService.Stub;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class RemoteMethodInvoker<T> extends FutureTask<T> implements ServiceConnection {
    private final String MARKET_SERVICE_CLASS_NAME = "com.xiaomi.market.data.MarketService";
    private Context mContext = MarketManager.getContext();
    private T mResult = null;

    public abstract T innerInvoke(IMarketService iMarketService) throws RemoteException;

    public RemoteMethodInvoker() {
        super(new Callable<T>() {
            public T call() throws Exception {
                throw new IllegalStateException("this should never be called");
            }
        });
    }

    public void onServiceDisconnected(ComponentName name) {
    }

    public void onServiceConnected(ComponentName name, final IBinder service) {
        new Thread() {
            public void run() {
                IMarketService s = Stub.asInterface(service);
                RemoteMethodInvoker.this.mResult = null;
                try {
                    RemoteMethodInvoker.this.mResult = RemoteMethodInvoker.this.innerInvoke(s);
                } catch (RemoteException e) {
                    Log.e("MarketRemoteMethodInvoker", "error while invoking market service methods", e);
                } finally {
                    RemoteMethodInvoker.this.mContext.unbindService(RemoteMethodInvoker.this);
                }
                RemoteMethodInvoker.this.set(RemoteMethodInvoker.this.mResult);
            }
        }.start();
    }

    public T invoke() {
        T t = null;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(MarketManager.MARKET_PACKAGE_NAME, "com.xiaomi.market.data.MarketService"));
        if (this.mContext.bindService(intent, this, 1)) {
            try {
                t = get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e2) {
            }
        } else {
            Log.e("MarketRemoteMethodInvoker", "Can not find MarketService");
        }
        return t;
    }

    public void invokeAsync() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(MarketManager.MARKET_PACKAGE_NAME, "com.xiaomi.market.data.MarketService"));
        this.mContext.bindService(intent, this, 1);
    }

    public void invokeInNewThread() {
        new Thread() {
            public void run() {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(MarketManager.MARKET_PACKAGE_NAME, "com.xiaomi.market.data.MarketService"));
                RemoteMethodInvoker.this.mContext.bindService(intent, RemoteMethodInvoker.this, 1);
            }
        }.start();
    }
}
