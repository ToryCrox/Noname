package com.xiaomi.analytics;

public class TrackAction extends Action {
    public TrackAction setCategory(String str) {
        addContent("_category_", (Object) str);
        return this;
    }

    public TrackAction setAction(String str) {
        addContent("_action_", (Object) str);
        return this;
    }
}
