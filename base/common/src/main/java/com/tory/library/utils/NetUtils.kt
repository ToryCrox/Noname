package com.tory.library.utils

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import java.net.NetworkInterface
import java.net.SocketException


object NetUtils {
    const val NETWORK_TYPE_WIFI = "wifi"
    const val NETWORK_TYPE_3G = "3g"
    const val NETWORK_TYPE_2G = "2g"
    const val NETWORK_TYPE_WAP = "wap"
    const val NETWORK_TYPE_UNKNOWN = "unknown"
    const val NETWORK_TYPE_DISCONNECT = "disconnect"
    /**
     * 返回移动终端类型
     * PHONE_TYPE_NONE :0 手机制式未知
     * PHONE_TYPE_GSM :1 手机制式为GSM，移动和联通
     * PHONE_TYPE_CDMA :2 手机制式为CDMA，电信
     * PHONE_TYPE_SIP:3
     *
     * @param context
     * @return
     */
    fun getPhoneType(context: Context): Int {
        val telephonyManager = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.phoneType
    }

    /**
     * 需要权限: android.permission.ACCESS_NETWORK_STATE
     * @param context
     * @return
     */
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        if (info != null && info.isConnected) {
            if (info.state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        try {
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                val info = connectivity.activeNetworkInfo
                return info?.isAvailable == true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun isWiFi(cxt: Context): Boolean {
        val cm = cxt.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // wifi的状态：ConnectivityManager.TYPE_WIFI
// 3G的状态：ConnectivityManager.TYPE_MOBILE
        return cm != null && cm.activeNetworkInfo != null && cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
    }

    /***
     * 获取MAC地址
     * 需要权限：android.permission.ACCESS_WIFI_STATE
     *
     * @return
     */
    fun getWifiMacAddr(ctx: Context): String? {
        var macAddr = ""
        try {
            val wifi = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
            macAddr = wifi.connectionInfo.macAddress
            if (macAddr == null) {
                macAddr = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return macAddr
    }

    fun getIP(ctx: Context): String {
        val wifiManager = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wifiManager.isWifiEnabled) getWifiIP(wifiManager) else gPRSIP!!
    }

    /***
     * 获取MAC地址
     * 需要权限：android.permission.ACCESS_WIFI_STATE
     *
     * @return
     */
    fun getWifiIP(wifiManager: WifiManager): String {
        val wifiInfo = wifiManager.connectionInfo
        val ip = intToIp(wifiInfo.ipAddress)
        return ip ?: ""
    }

    val gPRSIP: String?
        get() {
            var ip: String? = null
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val enumIpAddr = en.nextElement().inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            ip = inetAddress.hostAddress
                        }
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
                ip = null
            }
            return ip
        }

    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." + (i shr 8 and 0xFF) + "." + (i shr 16 and 0xFF) + "." + (i shr 24 and 0xFF)
    }

    fun getNetworkType(context: Context): Int {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = connectivityManager?.activeNetworkInfo
        return networkInfo?.type ?: -1
    }

    fun getNetworkTypeName(context: Context): String {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = manager.activeNetworkInfo
        var type = NETWORK_TYPE_DISCONNECT
        if (networkInfo == null) {
            return type
        }
        if (networkInfo.isConnected) {
            val typeName = networkInfo.typeName
            type = if ("WIFI".equals(typeName, ignoreCase = true)) {
                NETWORK_TYPE_WIFI
            } else if ("MOBILE".equals(typeName, ignoreCase = true)) { //String proxyHost = android.net.Proxy.getDefaultHost();//deprecated
                val proxyHost = System.getProperty("http.proxyHost")
                if (TextUtils.isEmpty(proxyHost)) if (isFastMobileNetwork(context)) NETWORK_TYPE_3G else NETWORK_TYPE_2G else NETWORK_TYPE_WAP
            } else {
                NETWORK_TYPE_UNKNOWN
            }
        }
        return type
    }

    //unchecked
    fun openNetSetting(act: Activity) {
        val intent = Intent()
        val cm = ComponentName("com.android.settings", "com.android.settings.WirelessSettings")
        intent.component = cm
        intent.action = "android.intent.action.VIEW"
        act.startActivityForResult(intent, 0)
    }

    /**
     * Whether is fast mobile network
     */
    private fun isFastMobileNetwork(context: Context): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                ?: return false
        return when (telephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_1xRTT -> false
            TelephonyManager.NETWORK_TYPE_CDMA -> false
            TelephonyManager.NETWORK_TYPE_EDGE -> false
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> true
            TelephonyManager.NETWORK_TYPE_EVDO_A -> true
            TelephonyManager.NETWORK_TYPE_GPRS -> false
            TelephonyManager.NETWORK_TYPE_HSDPA -> true
            TelephonyManager.NETWORK_TYPE_HSPA -> true
            TelephonyManager.NETWORK_TYPE_HSUPA -> true
            TelephonyManager.NETWORK_TYPE_UMTS -> true
            TelephonyManager.NETWORK_TYPE_EHRPD -> true
            TelephonyManager.NETWORK_TYPE_EVDO_B -> true
            TelephonyManager.NETWORK_TYPE_HSPAP -> true
            TelephonyManager.NETWORK_TYPE_IDEN -> false
            TelephonyManager.NETWORK_TYPE_LTE -> true
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
            else -> false
        }
    }

    /**
     * 需要权限： android.permission.CHANGE_WIFI_STATE
     * @param context
     * @param enabled
     */
    fun setWifiEnabled(context: Context, enabled: Boolean) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled
    }

    /**
     * 需要权限： android.permission.CHANGE_WIFI_STATE
     * @param context
     */
    fun getWifiScanResults(context: Context): List<ScanResult>? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wifiManager.startScan()) wifiManager.scanResults else null
    }

    /**
     * 需要权限： android.permission.CHANGE_WIFI_STATE
     * @param context
     * @param bssid
     * @return
     */
    fun getScanResultsByBSSID(context: Context, bssid: String): ScanResult? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var scanResult: ScanResult? = null
        val f = wifiManager.startScan()
        if (!f) {
            getScanResultsByBSSID(context, bssid)
        }
        val list = wifiManager.scanResults
        if (list != null) {
            for (i in list.indices) {
                scanResult = list[i]
                if (scanResult.BSSID == bssid) {
                    break
                }
            }
        }
        return scanResult
    }

    fun getWifiConnectionInfo(context: Context): WifiInfo {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.connectionInfo
    }

    /**
     * 判断手机连接的网络类型(2G,3G,4G)
     * 联通的3G为UMTS或HSDPA，移动和联通的2G为GPRS或EGDE，电信的2G为CDMA，电信的3G为EVDO
     * @param context
     * @return
     */
    fun getNetWorkClass(context: Context): NetWorkType {
        val telephonyManager = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return when (telephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> NetWorkType.NETWORK_CLASS_2_G
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> NetWorkType.NETWORK_CLASS_3_G
            TelephonyManager.NETWORK_TYPE_LTE -> NetWorkType.NETWORK_CLASS_4_G
            else -> NetWorkType.NETWORK_CLASS_UNKNOWN
        }
    }

    /**
     * 判断当前手机的网络类型(WIFI还是2,3,4G)
     * 需要权限android.permission.ACCESS_NETWORK_STATE
     * @param context
     * @return
     */
    fun getNetWorkStatus(context: Context): NetWorkType {
        var netWorkType = NetWorkType.NETWORK_CLASS_UNKNOWN
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            val type = networkInfo.type
            if (type == ConnectivityManager.TYPE_WIFI) {
                netWorkType = NetWorkType.NETWORK_WIFI
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                netWorkType = getNetWorkClass(context)
            }
        }
        return netWorkType
    }

    enum class NetWorkType {
        NETWORK_CLASS_UNKNOWN, NETWORK_WIFI, NETWORK_CLASS_2_G, NETWORK_CLASS_3_G, NETWORK_CLASS_4_G
    }

    /**
     * 网络是否已连接
     *
     * @return true:已连接 false:未连接
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun isNetConnected(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (manager != null) {
            val networkInfo = manager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
        return false
    }

    /**
     * Wifi是否已连接
     *
     * @return true:已连接 false:未连接
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun isWifiConnected(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.getNetworkCapabilities(manager.activeNetwork)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            val networkInfo = manager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    /**
     * 是否为流量
     */
    fun isMobileData(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.getNetworkCapabilities(manager.activeNetwork)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        } else {
            val networkInfo = manager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_MOBILE
        }
    }


}