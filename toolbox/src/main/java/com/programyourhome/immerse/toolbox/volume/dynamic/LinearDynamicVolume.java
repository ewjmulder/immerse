package com.programyourhome.immerse.toolbox.volume.dynamic;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.volume.DynamicVolume;
import com.programyourhome.immerse.toolbox.util.ValueRangeUtil;

/**
 * A volume moving linear from a configured volume to a configured volume in a certain amount of time, after an optional delay.
 */
public class LinearDynamicVolume implements DynamicVolume {

    private static final long serialVersionUID = Serialization.VERSION;

    private final double fromVolume;
    private final double toVolume;
    private final long timespanInMillis;
    private final boolean ignoreReplay;
    private final long delayInMillis;
    private long startMillis;

    public LinearDynamicVolume(double fromVolume, double toVolume, long timespanInMillis, boolean ignoreRepay, long delayInMillis) {
        this.fromVolume = fromVolume;
        this.toVolume = toVolume;
        this.timespanInMillis = timespanInMillis;
        this.ignoreReplay = ignoreRepay;
        this.delayInMillis = delayInMillis;
        this.startMillis = -1;
    }

    @Override
    public void audioStarted() {
        if (this.startMillis == -1 || !this.ignoreReplay) {
            this.startMillis = System.currentTimeMillis();
        }
    }

    @Override
    public double getVolume() {
        long millisSinceStart = System.currentTimeMillis() - this.startMillis;
        double volume = this.fromVolume;
        if (millisSinceStart > this.delayInMillis) {
            if (millisSinceStart < this.delayInMillis + this.timespanInMillis) {
                volume = ValueRangeUtil.convertValueFromRangeToRange(millisSinceStart,
                        this.delayInMillis, this.delayInMillis + this.timespanInMillis,
                        this.fromVolume, this.toVolume);
            } else {
                volume = this.toVolume;
            }
        }
        return volume;
    }

    public static Factory<DynamicVolume> linear(double fromVolume, double toVolume, long timespanInMillis) {
        return linear(fromVolume, toVolume, timespanInMillis, false);
    }

    public static Factory<DynamicVolume> linear(double fromVolume, double toVolume, long timespanInMillis, boolean ignoreRepay) {
        return linearWithDelay(fromVolume, toVolume, timespanInMillis, ignoreRepay, 0);
    }

    public static Factory<DynamicVolume> linearWithDelay(double fromVolume, double toVolume, long timespanInMillis, boolean ignoreRepay,
            long delayInMillis) {
        return new Factory<DynamicVolume>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public DynamicVolume create() {
                return new LinearDynamicVolume(fromVolume, toVolume, timespanInMillis, ignoreRepay, delayInMillis);
            }
        };
    }

}
