package com.programyourhome.immerse.domain.audio.playback;

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

}
