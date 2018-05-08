package com.programyourhome.immerse.toolbox.audio.playback;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.playback.Playback;

/**
 * Will loop forever and never stops.
 */
public class ForeverPlayback implements Playback {

    private static final long serialVersionUID = Serialization.VERSION;

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

    public static Factory<Playback> forever() {
        return () -> new ForeverPlayback();
    }

}
