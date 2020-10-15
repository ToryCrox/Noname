package com.tory.library.utils.livebus;

import java.util.Objects;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 */
public class LiveEmptyEvent implements LiveBusEvent {

    private String eventName;

    public LiveEmptyEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LiveEmptyEvent{");
        sb.append("eventName='").append(eventName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiveEmptyEvent that = (LiveEmptyEvent) o;
        return Objects.equals(eventName, that.eventName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName);
    }
}
