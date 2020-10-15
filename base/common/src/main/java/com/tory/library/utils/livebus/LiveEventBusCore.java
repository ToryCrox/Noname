package com.tory.library.utils.livebus;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.ViewModel;

import java.util.Map;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 * Description:
 */
class LiveEventBusCore extends ViewModel {

    private final static String PREFIX_TYPE_EVENT = "type_type#";
    private final static String PREFIX_TYPE_EMPTY = "type_empty#";

    private Map<String, BusObservable<? extends LiveBusEvent>> mBus = new ArrayMap<>();

    LiveEventBusCore() { }

    /**
     * 获取BusObservable
     * @param eventName
     * @param clazz
     * @param <T>
     * @return
     */
    @NonNull
    private  <T extends LiveBusEvent> BusObservable<T> of(@NonNull String eventName, @NonNull Class<T> clazz) {
        BusObservable observable = mBus.get(eventName);
        if (observable == null) {
            observable = new BusObservableWrapper(clazz);
            mBus.put(eventName, observable);
        } else {
            if (observable.getEventType() != clazz) {
                throw new IllegalArgumentException("LiveEventBusCore eventType not match, " +
                        "event:" + eventName +
                        "target is " + clazz + ", has been: " + observable.getEventType());
            }
        }
        return observable;
    }

    /**
     * 获取特定类型的 BusObservable
     * @param clazz
     * @param <T>
     * @return
     */
    @NonNull
    public <T extends LiveBusEvent> BusObservable<T> of(@NonNull Class<T> clazz) {
        String eventName = PREFIX_TYPE_EVENT + clazz.getName();
        return of(eventName, clazz);
    }

    /**
     * 获取空消息的BusObservable
     * @param eventName
     * @return
     */
    public BusObservable<LiveEmptyEvent> ofEmpty(@NonNull String eventName) {
        return of(PREFIX_TYPE_EMPTY + eventName, LiveEmptyEvent.class);
    }

    /**
     * 根据消息类型发送，是通过event的class类完全匹配
     * @param event
     * @param <T>
     */
    public <T extends LiveBusEvent> void post(@NonNull T event) {
        BusObservable observable = of(event.getClass());
        observable.post(event);
    }

    /**
     * 发送一个空消息，根据eventName来发送，监听使用ofEmpty(eventName).observe
     * @param eventName
     */
    public void postEmpty(@NonNull String eventName) {
        BusObservable<LiveEmptyEvent> observable = ofEmpty(eventName);
        observable.post(new LiveEmptyEvent(eventName));
    }
}
