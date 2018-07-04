package com.market.sdk;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IImageCallback extends IInterface {

    public static abstract class Stub extends Binder implements IImageCallback {

        private static class Proxy implements IImageCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onImageLoadSuccess(String url, Uri iconUri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IImageCallback");
                    _data.writeString(url);
                    if (iconUri != null) {
                        _data.writeInt(1);
                        iconUri.writeToParcel(_data, 0);
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

            public void onImageLoadFailed(String url) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IImageCallback");
                    _data.writeString(url);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.market.sdk.IImageCallback");
        }

        public static IImageCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.market.sdk.IImageCallback");
            if (iin == null || !(iin instanceof IImageCallback)) {
                return new Proxy(obj);
            }
            return (IImageCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    Uri _arg1;
                    data.enforceInterface("com.market.sdk.IImageCallback");
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onImageLoadSuccess(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.market.sdk.IImageCallback");
                    onImageLoadFailed(data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.market.sdk.IImageCallback");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onImageLoadFailed(String str) throws RemoteException;

    void onImageLoadSuccess(String str, Uri uri) throws RemoteException;
}
