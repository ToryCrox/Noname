package com.tory.library.extension

import android.os.Parcelable
import android.text.TextUtils
import com.tencent.mmkv.MMKV
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * - Author: tory
 * - Date: 2022/6/3
 * - Description:
 */
fun <T : Any> mmkv(key: String, def: T, fileName: String? = null) = object : ReadWriteProperty<Any, T> {
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        put(key, value, fileName)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return get(key, def, fileName) ?: def
    }
}

fun put(key: String, value: Any?, fileName: String?) {
    val mmkv: MMKV = getMMKV(fileName)
    when (value) {
        is String -> mmkv.putString(key, value)
        is Int -> mmkv.putInt(key, value)
        is Boolean -> mmkv.putBoolean(key, value)
        is Float -> mmkv.putFloat(key, value)
        is Long -> mmkv.putLong(key, value)
        is Double -> mmkv.encode(key, value)
        is Parcelable -> error("not support Parcelable")
        else -> {
            error("not support type")
        }
    }
}

fun <T> get(key: String?, defValue: T, fileName: String?): T? {
    val mmkv: MMKV = getMMKV(fileName)
    val value: Any? = when (defValue) {
        is String -> mmkv.getString(key, defValue as String)
        is Int -> mmkv.getInt(key, (defValue as Int))
        is Boolean -> mmkv.getBoolean(key, (defValue as Boolean))
        is Float -> mmkv.getFloat(key, (defValue as Float))
        is Long -> mmkv.getLong(key, (defValue as Long))
        is Double -> mmkv.decodeDouble(key, (defValue as Double))
        is Parcelable -> error("not support Parcelable")
        else -> {
            defValue
        }
    }
    return value as? T?
}

fun getMMKV(fileName: String?): MMKV {
    val mmkv: MMKV = if (TextUtils.isEmpty(fileName)) {
        MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
    } else {
        MMKV.mmkvWithID(fileName, MMKV.MULTI_PROCESS_MODE)
    }
    return mmkv
}