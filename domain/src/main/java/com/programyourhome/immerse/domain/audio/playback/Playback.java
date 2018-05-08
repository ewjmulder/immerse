package com.programyourhome.immerse.domain.audio.playback;

import java.io.Serializable;

/**
 * A Playback object decides when playback should be stopped.
 * Since Playback implementations have mutable state and the domain module is designed to be immutable,
 * no actual implementation are provided in this module.
 */
public interface Playback extends Serializable {

    /**
     * Signals that the audio playback has started.
     * NB: Will be called at the beginning of every playback loop.
     */
    public void audioStarted();

    /**
     * Can be called at any time to ask if the playback should be stopped or not.
     * For implementations just triggering on loop events, this can always return false.
     */
    public boolean shouldStop();

    /**
     * Signals that the audio reached the end of the stream.
     * Return value decides whether to continue playing from the start or not.
     * Will be called after every completed loop.
     * For implementations just triggering on non-loop related events, this can always return true.
     */
    public boolean endOfStream();

}
