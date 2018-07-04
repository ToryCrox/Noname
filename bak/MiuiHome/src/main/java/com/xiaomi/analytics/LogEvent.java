package com.xiaomi.analytics;

class LogEvent {

    public enum LogType {
        TYPE_EVENT(0),
        TYPE_AD(1);
        
        private int mValue;

        private LogType(int i) {
            this.mValue = 0;
            this.mValue = i;
        }

        public int value() {
            return this.mValue;
        }
    }
}
