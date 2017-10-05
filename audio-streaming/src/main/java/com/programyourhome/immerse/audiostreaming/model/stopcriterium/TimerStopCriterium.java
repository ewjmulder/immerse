package com.programyourhome.immerse.audiostreaming.model.stopcriterium;

public class TimerStopCriterium implements StopCriterium {

    private long startMillis;
    private long durationInMillis;

    public TimerStopCriterium(long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }

    @Override
    public void audioStarted() {
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    public boolean shouldStop() {
        return System.currentTimeMillis() - this.startMillis >= this.durationInMillis;
    }

}
