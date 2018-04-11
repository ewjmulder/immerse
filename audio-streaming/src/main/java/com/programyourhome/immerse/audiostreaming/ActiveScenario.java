package com.programyourhome.immerse.audiostreaming;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

public class ActiveScenario {

    private final Scenario scenario;
    private final DynamicLocation sourceLocation;
    private final DynamicLocation listenerLocation;
    private final VolumeRatiosAlgorithm volumeRatiosAlgorithm;
    private final NormalizeAlgorithm normalizeAlgorithm;
    private final Playback playback;
    private AudioInputStream inputStream;
    private long startMillis;

    public ActiveScenario(Scenario scenario, AudioInputStream inputStream) {
        this.scenario = scenario;
        this.sourceLocation = scenario.getSettings().getSourceLocationSupplier().get();
        this.listenerLocation = scenario.getSettings().getListenerLocationSupplier().get();
        this.volumeRatiosAlgorithm = scenario.getSettings().getVolumeRatiosAlgorithmSupplier().get();
        this.normalizeAlgorithm = scenario.getSettings().getNormalizeAlgorithmSupplier().get();
        this.playback = this.scenario.getSettings().getPlaybackSupplier().get();
        this.reset(inputStream);
    }

    public Scenario getScenario() {
        return this.scenario;
    }

    public DynamicLocation getSourceLocation() {
        return this.sourceLocation;
    }

    public DynamicLocation getListenerLocation() {
        return this.listenerLocation;
    }

    public VolumeRatiosAlgorithm getVolumeRatiosAlgorithm() {
        return this.volumeRatiosAlgorithm;
    }

    public NormalizeAlgorithm getNormalizeAlgorithm() {
        return this.normalizeAlgorithm;
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

    public void reset(AudioInputStream inputStream) {
        this.inputStream = inputStream;
        this.startMillis = -1;
    }

}
