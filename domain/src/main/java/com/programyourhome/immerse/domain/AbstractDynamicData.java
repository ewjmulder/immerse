package com.programyourhome.immerse.domain;

public abstract class AbstractDynamicData<T> implements DynamicData<T> {

    private static final long serialVersionUID = Serialization.VERSION;

    private boolean ignoreReplay;
    private long startMillis;
    private long currentMillis;

    public AbstractDynamicData() {
        this(true);
    }

    public AbstractDynamicData(boolean ignoreReplay) {
        this.ignoreReplay = ignoreReplay;
        this.startMillis = -1;
        this.currentMillis = -1;
    }

    @Override
    public void nextPlaybackStarted() {
        if (this.startMillis == -1 || !this.ignoreReplay) {
            this.startMillis = System.currentTimeMillis();
            this.currentMillis = -1;
        }
    }

    /**
     * Update the current millis at the start of a next step, so getMillisSinceStart will not call
     * System.currentTimeMillis every time (performance improvement).
     */
    @Override
    public void nextStep() {
        this.currentMillis = System.currentTimeMillis();
    }

    public long getMillisSinceStart() {
        if (this.startMillis == -1) {
            // Not started yet means 1 millis since start.
            return 0;
        }
        return this.currentMillis - this.startMillis;
    }

}
