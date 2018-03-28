package com.programyourhome.immerse.domain.audio.playback;

public class LoopPlayback implements Playback {

    private final int times;
    private int loopCount;

    public LoopPlayback(int times) {
        if (times < 1) {
            throw new IllegalArgumentException("Number of loops must be 1 or larger.");
        }
        this.times = times;
        this.loopCount = 0;
    }

    @Override
    public void audioStarted() {
    }

    @Override
    public boolean shouldStop() {
        // We only signal stopping after a loop has been completed.
        return false;
    }

    @Override
    public boolean endOfStream() {
        this.loopCount++;
        return this.loopCount < this.times;
    }

}
