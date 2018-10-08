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
     * Signals that processing will start the next 'step' of the loop.
     * Any init logic that is needed once per step can be performed here.
     */
    public void nextStep();

    /**
     * Get the current value.
     */
    public T getCurrentValue();

}
