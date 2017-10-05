package com.programyourhome.immerse.domain.audio.stopcriterium;

public interface StopCriterium {

    public void audioStarted();

    public boolean shouldStop();

}
