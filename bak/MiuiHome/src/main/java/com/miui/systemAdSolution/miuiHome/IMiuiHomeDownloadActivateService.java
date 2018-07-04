package com.miui.systemAdSolution.miuiHome;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMiuiHomeDownloadActivateService extends IInterface {

    public static abstract class Stub extends Binder implements IMiuiHomeDownloadActivateService {

        private static class Proxy implements IMiuiHomeDownloadActivateService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int getServiceVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.miui.systemAdSolution.miuiHome.IMiuiHomeDownloadActivateService");
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean showDownloadNotification() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.miui.systemAdSolution.miuiHome.IMiuiHomeDownloadActivateService");
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static IMiuiHomeDownloadActivateService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.miui.systemAdSolution.miuiHome.IMiuiHomeDownloadActivateService");
            if (iin == null || !(iin instanceof IMiuiHomeDownloadActivateService)) {
                return new Proxy(obj);
            }
            return (IMiuiHomeDownloadActivateService) iin;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.miui.systemAdSolution.miuiHome.IMiuiHomeDownloadActivateService");
                    int _result = getServiceVersion();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.miui.systemAdSolution.miuiHome.IMiuiHomeDownloadActivateService");
                    boolean _result2 = showDownloadNotification();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString("com.miui.systemAdSolution.miuiHome.IMiuiHomeDownloadActivateService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int getServiceVersion() throws RemoteException;

    boolean showDownloadNotification() throws RemoteException;
}
