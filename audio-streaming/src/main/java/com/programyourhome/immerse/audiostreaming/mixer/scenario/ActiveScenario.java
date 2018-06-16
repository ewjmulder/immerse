package com.programyourhome.immerse.audiostreaming.mixer.scenario;

import java.util.UUID;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * An active scenario is a scenario that is begin played by the mixer.
 * It contains the 'static' scenario data and all settings interface implementations that can contain state.
 * It also keeps track of the elapsed time since the playback has started.
 * An active scenario can be used for several playback loops.
 */
public class ActiveScenario {

    private final UUID id;
    private final Scenario scenario;
    private DynamicLocation sourceLocation;
    private DynamicLocation listenerLocation;
    private VolumeRatiosAlgorithm volumeRatiosAlgorithm;
    private NormalizeAlgorithm normalizeAlgorithm;
    private final Playback playback;
    private final Factory<AudioResource> audioResourceFactory;
    private AudioInputStream inputStream;
    private long startMillis;

    public ActiveScenario(Scenario scenario, Factory<AudioResource> audioResourceFactory) {
        this.id = UUID.randomUUID();
        this.scenario = scenario;
        this.playback = this.scenario.getSettings().getPlaybackFactory().create();
        this.audioResourceFactory = audioResourceFactory;
    }

    /**
     * Uniquely identifies this active scenario, since the scenario itself can be re-used multiple times.
     */
    public UUID getId() {
        return this.id;
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

    public Factory<AudioResource> getAudioResourceFactory() {
        return this.audioResourceFactory;
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

    /**
     * Start this active scenario, by recording the current time as start time.
     * For convenience, this method can be called several times and checks if action is actually required.
     */
    public void startIfNotStarted() {
        if (!this.isStarted()) {
            this.startMillis = System.currentTimeMillis();
            this.playback.audioStarted();
        }
    }

    /**
     * Reset this active scenario for the next start of playback (either initial start or restart).
     * Get a fresh input stream and fresh copies of all settings, except for the Playback object,
     * since that should persist over several restarts to be able to count loops.
     */
    public void resetForNextStart(AudioInputStream inputStream) {
        this.inputStream = inputStream;
        this.startMillis = -1;

        this.sourceLocation = this.scenario.getSettings().getSourceLocationFactory().create();
        this.listenerLocation = this.scenario.getSettings().getListenerLocationFactory().create();
        this.volumeRatiosAlgorithm = this.scenario.getSettings().getVolumeRatiosAlgorithmFactory().create();
        this.normalizeAlgorithm = this.scenario.getSettings().getNormalizeAlgorithmFactory().create();
    }

}
