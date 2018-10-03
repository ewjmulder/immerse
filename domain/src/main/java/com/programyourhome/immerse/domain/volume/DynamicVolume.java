package com.programyourhome.immerse.domain.volume;

import java.io.Serializable;

/**
 * A volume that changes over time.
 */
public interface DynamicVolume extends Serializable {

    /**
     * Signals that the audio playback has started.
     * NB: Will be called at the beginning of every playback loop.
     */
    public void audioStarted();

    /**
     * Get the current volume [0,1] (possibly based on the time elapsed since audio started).
     */
    public double getVolume();

}
