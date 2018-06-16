package com.programyourhome.immerse.toolbox.audio.playback;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.playback.Playback;

/**
 * Loops a certain amount of times.
 */
public class LoopPlayback implements Playback {

    private static final long serialVersionUID = Serialization.VERSION;

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
        // Continue while the loop count is less the the configured amount of loops.
        return this.loopCount < this.times;
    }

    public static Factory<Playback> once() {
        return times(1);
    }

    public static Factory<Playback> times(int times) {
        return new Factory<Playback>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public Playback create() {
                return new LoopPlayback(times);
            }
        };
    }

}
