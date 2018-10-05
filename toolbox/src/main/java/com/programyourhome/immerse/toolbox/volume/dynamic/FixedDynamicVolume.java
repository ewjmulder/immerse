package com.programyourhome.immerse.toolbox.volume.dynamic;

import com.programyourhome.immerse.domain.AbstractDynamicData;
import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.volume.DynamicVolume;

/**
 * A fixed volume, static at all times.
 */
public class FixedDynamicVolume extends AbstractDynamicData<Double> implements DynamicVolume {

    private static final long serialVersionUID = Serialization.VERSION;

    private final double volume;

    public FixedDynamicVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public void nextPlaybackStarted() {
        // Ignore, has no effect.
    }

    @Override
    public Double getCurrentValue() {
        return this.volume;
    }

    public static Factory<DynamicVolume> full() {
        return fixed(1);
    }

    public static Factory<DynamicVolume> mute() {
        return fixed(0);
    }

    public static Factory<DynamicVolume> fixed(double volume) {
        return new Factory<DynamicVolume>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public DynamicVolume create() {
                return new FixedDynamicVolume(volume);
            }
        };
    }

}
