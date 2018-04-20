package com.programyourhome.immerse.toolbox.audio.playback;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.audio.playback.Playback;

/**
 * Will loop forever and never stops.
 */
public class ForeverPlayback implements Playback {

    public ForeverPlayback() {
    }

    @Override
    public void audioStarted() {
    }

    @Override
    public boolean shouldStop() {
        return false;
    }

    @Override
    public boolean endOfStream() {
        return true;
    }

    public static Supplier<Playback> forever() {
        return () -> new ForeverPlayback();
    }

}
