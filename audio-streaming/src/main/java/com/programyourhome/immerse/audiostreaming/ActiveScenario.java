package com.programyourhome.immerse.audiostreaming;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.playback.Playback;

public class ActiveScenario {

    private final Scenario scenario;
    private final Playback playback;
    private final AudioInputStream inputStream;
    private long startMillis;

    public ActiveScenario(Scenario scenario, Playback playback, AudioInputStream inputStream) {
        this.scenario = scenario;
        this.playback = playback;
        this.inputStream = inputStream;
        this.startMillis = -1;
    }

    public Scenario getScenario() {
        return this.scenario;
    }

    public Playback getPlayback() {
        return this.playback;
    }

    public AudioInputStream getInputStream() {
        return this.inputStream;
    }

    public long getStartMillis() {
        return this.startMillis;
    }

    public boolean isStarted() {
        return this.startMillis > -1;
    }

    public void startIfNotStarted() {
        if (!this.isStarted()) {
            this.startMillis = System.currentTimeMillis();
            this.playback.audioStarted();
        }
    }

}
