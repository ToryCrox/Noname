package com.xiaomi.settingsdk.backup.data;

import android.os.Parcel;
import android.os.Parcelable.Creator;

public class KeyStringSettingItem extends SettingItem<String> {
    public static final Creator<KeyStringSettingItem> CREATOR = new Creator<KeyStringSettingItem>() {
        public KeyStringSettingItem[] newArray(int size) {
            return new KeyStringSettingItem[size];
        }

        public KeyStringSettingItem createFromParcel(Parcel source) {
            KeyStringSettingItem obj = new KeyStringSettingItem();
            obj.fillFromParcel(source);
            return obj;
        }
    };

    protected String stringToValue(String rawValue) {
        return rawValue;
    }

    protected String valueToString(String actualValue) {
        return actualValue;
    }
}
