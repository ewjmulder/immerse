package com.programyourhome.immerse.domain.audio.playback;

public class TimerPlayback implements Playback {

    private long startMillis;
    private final long durationInMillis;

    public TimerPlayback(long durationInMillis) {
        this.startMillis = -1;
        this.durationInMillis = durationInMillis;
    }

    @Override
    public void audioStarted() {
        // Only set this on the first call, not on next loops.
        if (this.startMillis == -1) {
            this.startMillis = System.currentTimeMillis();
        }
    }

    @Override
    public boolean shouldStop() {
        return this.startMillis > -1 && System.currentTimeMillis() - this.startMillis >= this.durationInMillis;
    }

    @Override
    public boolean endOfStream() {
        // Always continue with next loop, the time will decide when to stop.
        return true;
    }

}
