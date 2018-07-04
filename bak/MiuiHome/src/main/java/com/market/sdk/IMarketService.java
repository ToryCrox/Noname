package com.market.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMarketService extends IInterface {

    public static abstract class Stub extends Binder implements IMarketService {

        private static class Proxy implements IMarketService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public ApkVerifyInfo getVerifyInfo(String apkPath, String installedFrom, boolean isUpdate) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ApkVerifyInfo _result;
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeString(apkPath);
                    _data.writeString(installedFrom);
                    if (!isUpdate) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ApkVerifyInfo) ApkVerifyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ApkVerifyInfo getApkCheckInfo(String apkPath, String installedFrom, boolean isUpdate) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ApkVerifyInfo _result;
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeString(apkPath);
                    _data.writeString(installedFrom);
                    if (isUpdate) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ApkVerifyInfo) ApkVerifyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean allowConnectToNetwork() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public void recordStaticsCountEvent(String type, String event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeString(type);
                    _data.writeString(event);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void loadIcon(String appId, String mask, IImageCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeString(appId);
                    _data.writeString(mask);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void loadImage(String url, int width, int height, IImageCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeString(url);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void loadDesktopRecommendInfo(long folderId, String folderName, List<String> pkgNameList, IDesktopRecommendResponse response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeLong(folderId);
                    _data.writeString(folderName);
                    _data.writeStringList(pkgNameList);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringList(pkgNameList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInWhiteSetForApkCheck(String pkgName) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeString(pkgName);
                    this.mRemote.transact(8, _data, _reply, 0);
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

            public String getWhiteSet() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getEnableSettings() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCategory(String[] pkgList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.market.sdk.IMarketService");
                    _data.writeStringArray(pkgList);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static IMarketService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.market.sdk.IMarketService");
            if (iin == null || !(iin instanceof IMarketService)) {
                return new Proxy(obj);
            }
            return (IMarketService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            String _arg0;
            String _arg1;
            boolean _arg2;
            ApkVerifyInfo _result;
            boolean _result2;
            String _result3;
            switch (code) {
                case 1:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    } else {
                        _arg2 = false;
                    }
                    _result = getVerifyInfo(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 2:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    } else {
                        _arg2 = false;
                    }
                    _result = getApkCheckInfo(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 3:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    _result2 = allowConnectToNetwork();
                    reply.writeNoException();
                    if (_result2) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 4:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    recordStaticsCountEvent(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    loadIcon(data.readString(), data.readString(), com.market.sdk.IImageCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    loadImage(data.readString(), data.readInt(), data.readInt(), com.market.sdk.IImageCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    long _arg02 = data.readLong();
                    _arg1 = data.readString();
                    List<String> _arg22 = data.createStringArrayList();
                    loadDesktopRecommendInfo(_arg02, _arg1, _arg22, com.market.sdk.IDesktopRecommendResponse.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeStringList(_arg22);
                    return true;
                case 8:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    _result2 = isInWhiteSetForApkCheck(data.readString());
                    reply.writeNoException();
                    if (_result2) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 9:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    _result3 = getWhiteSet();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 10:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    _result3 = getEnableSettings();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 11:
                    data.enforceInterface("com.market.sdk.IMarketService");
                    int _result4 = getCategory(data.createStringArray());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 1598968902:
                    reply.writeString("com.market.sdk.IMarketService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean allowConnectToNetwork() throws RemoteException;

    ApkVerifyInfo getApkCheckInfo(String str, String str2, boolean z) throws RemoteException;

    int getCategory(String[] strArr) throws RemoteException;

    String getEnableSettings() throws RemoteException;

    ApkVerifyInfo getVerifyInfo(String str, String str2, boolean z) throws RemoteException;

    String getWhiteSet() throws RemoteException;

    boolean isInWhiteSetForApkCheck(String str) throws RemoteException;

    void loadDesktopRecommendInfo(long j, String str, List<String> list, IDesktopRecommendResponse iDesktopRecommendResponse) throws RemoteException;

    void loadIcon(String str, String str2, IImageCallback iImageCallback) throws RemoteException;

    void loadImage(String str, int i, int i2, IImageCallback iImageCallback) throws RemoteException;

    void recordStaticsCountEvent(String str, String str2) throws RemoteException;
}
