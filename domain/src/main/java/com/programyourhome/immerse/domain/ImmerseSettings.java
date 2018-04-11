package com.programyourhome.immerse.domain;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * Settings class for storing the fields that define how a scenario should be played.
 */
public class ImmerseSettings {

    private VolumeRatiosAlgorithm volumeRatiosAlgorithm;
    private NormalizeAlgorithm normalizeAlgorithm;
    private Supplier<Playback> playbackSupplier;

    private ImmerseSettings() {
    }

    public VolumeRatiosAlgorithm getVolumeRatiosAlgorithm() {
        return this.volumeRatiosAlgorithm;
    }

    public NormalizeAlgorithm getNormalizeAlgorithm() {
        return this.normalizeAlgorithm;
    }

    /**
     * For the Playback we use a supplier, because the domain should not keep state
     * and a settings object should be reusable.
     */
    public Supplier<Playback> getPlaybackSupplier() {
        return this.playbackSupplier;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmerseSettings settings;

        public Builder() {
            this.settings = new ImmerseSettings();
        }

        public Builder volumeRatiosAlgorithm(VolumeRatiosAlgorithm volumeRatiosAlgorithm) {
            this.settings.volumeRatiosAlgorithm = volumeRatiosAlgorithm;
            return this;
        }

        public Builder normalizeAlgorithm(NormalizeAlgorithm normalizeAlgorithm) {
            this.settings.normalizeAlgorithm = normalizeAlgorithm;
            return this;
        }

        public Builder playback(Supplier<Playback> playbackSupplier) {
            this.settings.playbackSupplier = playbackSupplier;
            return this;
        }

        public ImmerseSettings build() {
            return this.settings;
        }

    }

}
