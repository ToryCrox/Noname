package com.xiaomi.analytics;

import android.text.TextUtils;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

public abstract class Action {
    private static Set<String> sKeywords = new HashSet();
    private JSONObject mContent = new JSONObject();
    private JSONObject mExtra = new JSONObject();

    static {
        sKeywords.add("_event_id_");
        sKeywords.add("_category_");
        sKeywords.add("_action_");
        sKeywords.add("_label_");
        sKeywords.add("_value_");
    }

    public Action addParam(String str, int i) {
        ensureKey(str);
        addContent(str, i);
        return this;
    }

    public Action addParam(String str, String str2) {
        ensureKey(str);
        addContent(str, (Object) str2);
        return this;
    }

    void addContent(String str, int i) {
        if (!TextUtils.isEmpty(str)) {
            try {
                this.mContent.put(str, i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void addContent(String str, Object obj) {
        if (!TextUtils.isEmpty(str)) {
            try {
                this.mContent.put(str, obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void addExtra(String str, String str2) {
        try {
            this.mExtra.put(str, str2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureKey(String str) {
        if (!TextUtils.isEmpty(str) && sKeywords.contains(str)) {
            throw new IllegalArgumentException("this key " + str + " is built-in, please pick another key.");
        }
    }

    final JSONObject getContent() {
        return this.mContent;
    }

    final JSONObject getExtra() {
        return this.mExtra;
    }
}
