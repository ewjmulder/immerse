package com.programyourhome.immerse.domain.audio.stopcriterium;

public class NoStopCriterium implements StopCriterium {

    @Override
    public void audioStarted() {
    }

    @Override
    public boolean shouldStop() {
        return false;
    }

}
