package com.market.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDesktopRecommendResponse extends IInterface {

    public static abstract class Stub extends Binder implements IDesktopRecommendResponse {

        private static class Proxy implements IDesktopRecommendResponse {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onLoadSuccess(DesktopRecommendInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IDesktopRecommendResponse");
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onLoadFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IDesktopRecommendResponse");
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.market.sdk.IDesktopRecommendResponse");
        }

        public static IDesktopRecommendResponse asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.market.sdk.IDesktopRecommendResponse");
            if (iin == null || !(iin instanceof IDesktopRecommendResponse)) {
                return new Proxy(obj);
            }
            return (IDesktopRecommendResponse) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    DesktopRecommendInfo _arg0;
                    data.enforceInterface("com.market.sdk.IDesktopRecommendResponse");
                    if (data.readInt() != 0) {
                        _arg0 = (DesktopRecommendInfo) DesktopRecommendInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onLoadSuccess(_arg0);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.market.sdk.IDesktopRecommendResponse");
                    onLoadFailed();
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.market.sdk.IDesktopRecommendResponse");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onLoadFailed() throws RemoteException;

    void onLoadSuccess(DesktopRecommendInfo desktopRecommendInfo) throws RemoteException;
}
