package com.programyourhome.immerse.toolbox.audio.playback;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.playback.Playback;

/**
 * Plays until the timer runs out.
 * Will loop if end of stream is reached before the timer runs out.
 */
public class TimerPlayback implements Playback {

    private static final long serialVersionUID = Serialization.VERSION;

    private long startMillis;
    private final long durationInMillis;

    public TimerPlayback(long durationInMillis) {
        if (durationInMillis < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
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
        // We should stop when the audio has started and is running longer than the configured duration.
        return this.startMillis > -1 && System.currentTimeMillis() - this.startMillis >= this.durationInMillis;
    }

    @Override
    public boolean endOfStream() {
        // Always continue with next loop, the time will decide when to stop.
        return true;
    }

    public static Factory<Playback> timer(long millis) {
        return new Factory<Playback>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public Playback create() {
                return new TimerPlayback(millis);
            }
        };
    }
}
