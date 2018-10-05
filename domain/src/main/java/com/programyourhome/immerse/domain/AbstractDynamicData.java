package com.programyourhome.immerse.domain;

public abstract class AbstractDynamicData<T> implements DynamicData<T> {

    private static final long serialVersionUID = Serialization.VERSION;

    private boolean ignoreReplay;
    private long startMillis;

    public AbstractDynamicData() {
        this(true);
    }

    public AbstractDynamicData(boolean ignoreReplay) {
        this.ignoreReplay = ignoreReplay;
        this.startMillis = -1;
    }

    @Override
    public void nextPlaybackStarted() {
        if (this.startMillis == -1 || !this.ignoreReplay) {
            this.startMillis = System.currentTimeMillis();
        }
    }

    public long getMillisSinceStart() {
        if (this.startMillis == -1) {
            // Not started yet means 1 millis since start.
            return 0;
        }
        return System.currentTimeMillis() - this.startMillis;
    }

}
