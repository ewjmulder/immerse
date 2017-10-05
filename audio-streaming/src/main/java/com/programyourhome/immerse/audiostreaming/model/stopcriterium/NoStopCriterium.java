package com.programyourhome.immerse.audiostreaming.model.stopcriterium;

public class NoStopCriterium implements StopCriterium {

    @Override
    public void audioStarted() {
    }

    @Override
    public boolean shouldStop() {
        return false;
    }

}
