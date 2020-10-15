package com.tory.library.utils.livebus;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 * Description: 单例使用，页面间通信，
 * 注意!!: kotlin有坑，如果使用kotlin的Lambda，Observer内容和外部内无关会报错
 */
public class LiveEventBus extends LiveEventBusCore {

    public static LiveEventBus get() {
        return LiveEventBusHolder.instance;
    }

    private static class LiveEventBusHolder {
        static LiveEventBus instance = new LiveEventBus();
    }
}
