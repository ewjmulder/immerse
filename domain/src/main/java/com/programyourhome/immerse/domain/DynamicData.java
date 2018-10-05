package com.programyourhome.immerse.domain;

import java.io.Serializable;

/**
 * Some piece of data that changes over time.
 */
public interface DynamicData<T> extends Serializable {

    /**
     * Signals that the 'next' audio playback has started.
     * This can be the first or any subsequent loop of the same resource.
     */
    public void nextPlaybackStarted();

    /**
     * Get the current value.
     */
    public T getCurrentValue();

}
