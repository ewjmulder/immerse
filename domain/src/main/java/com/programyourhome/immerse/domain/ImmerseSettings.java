package com.programyourhome.immerse.domain;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.FractionalNormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.FieldOfHearingVolumeRatiosAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

public class ImmerseSettings {

    private VolumeRatiosAlgorithm volumeRatiosAlgorithm;
    private NormalizeAlgorithm normalizeAlgorithm;
    private Supplier<Playback> playbackSupplier;

    // Use sensible defaults.
    private ImmerseSettings() {
    }

    public VolumeRatiosAlgorithm getVolumeRatiosAlgorithm() {
        return this.volumeRatiosAlgorithm;
    }

    public NormalizeAlgorithm getNormalizeAlgorithm() {
        return this.normalizeAlgorithm;
    }

    // Supplier to avoid state in the Scenario
    public Supplier<Playback> getPlaybackSupplier() {
        return this.playbackSupplier;
    }

    public static ImmerseSettings defaults() {
        return builder().defaults().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmerseSettings settings;

        public Builder() {
            this.settings = new ImmerseSettings();
        }

        public Builder defaults() {
            this.settings.volumeRatiosAlgorithm = new FieldOfHearingVolumeRatiosAlgorithm();
            this.settings.normalizeAlgorithm = new FractionalNormalizeAlgorithm();
            this.settings.playbackSupplier = Playback.once();
            return this;
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
