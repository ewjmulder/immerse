package com.programyourhome.immerse.domain.audio.playback;

import java.util.function.Supplier;

public interface Playback {

    /**
     * Signals that the audio playback has started.
     * NB: Will be called at the beginning of every playback loop.
     */
    public void audioStarted();

    /**
     * Can be called at any time to ask if the playback should be stopped or not.
     */
    public boolean shouldStop();

    /**
     * Signals that the audio reached the end of the stream.
     * Return value decides whether to continue playing from the start or not.
     */
    public boolean endOfStream();

    public static Supplier<Playback> once() {
        return () -> new LoopPlayback(1);
    }

    public static Supplier<Playback> times(int times) {
        return () -> new LoopPlayback(times);
    }

    public static Supplier<Playback> forever() {
        return () -> new ForeverPlayback();
    }

    public static Supplier<Playback> timer(long millis) {
        return () -> new TimerPlayback(millis);
    }

}
