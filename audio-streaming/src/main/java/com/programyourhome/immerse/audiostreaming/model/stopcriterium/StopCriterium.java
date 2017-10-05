package com.programyourhome.immerse.audiostreaming.model.stopcriterium;

public interface StopCriterium {

    public void audioStarted();

    public boolean shouldStop();

}
