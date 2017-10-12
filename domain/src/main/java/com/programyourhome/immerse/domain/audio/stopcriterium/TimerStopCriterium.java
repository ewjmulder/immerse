package com.programyourhome.immerse.domain.audio.stopcriterium;

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
        System.out.println("current: " + System.currentTimeMillis());
        System.out.println("this.startMillis: " + this.startMillis);
        System.out.println("System.currentTimeMillis() - this.startMillis: " + (System.currentTimeMillis() - this.startMillis));
        return System.currentTimeMillis() - this.startMillis >= this.durationInMillis;
    }

}
