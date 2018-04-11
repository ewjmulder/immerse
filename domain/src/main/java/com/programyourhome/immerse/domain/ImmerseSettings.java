package com.programyourhome.immerse.domain;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * Settings class for storing the fields that define how a scenario should be played.
 * Since all these classes are interfaces of which the implementation could keep state,
 * they are all defined as Suppliers of the actual object, so this settings object can be re-used.
 */
public class ImmerseSettings {

    private Supplier<AudioResource> audioResourceSupplier;
    private Supplier<DynamicLocation> sourceLocationSupplier;
    private Supplier<DynamicLocation> listenerLocationSupplier;
    private Supplier<VolumeRatiosAlgorithm> volumeRatiosAlgorithmSupplier;
    private Supplier<NormalizeAlgorithm> normalizeAlgorithmSupplier;
    private Supplier<Playback> playbackSupplier;

    private ImmerseSettings() {
    }

    public Supplier<AudioResource> getAudioResourceSupplier() {
        return this.audioResourceSupplier;
    }

    public Supplier<DynamicLocation> getSourceLocationSupplier() {
        return this.sourceLocationSupplier;
    }

    public Supplier<DynamicLocation> getListenerLocationSupplier() {
        return this.listenerLocationSupplier;
    }

    public Supplier<VolumeRatiosAlgorithm> getVolumeRatiosAlgorithmSupplier() {
        return this.volumeRatiosAlgorithmSupplier;
    }

    public Supplier<NormalizeAlgorithm> getNormalizeAlgorithmSupplier() {
        return this.normalizeAlgorithmSupplier;
    }

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

        public Builder audioResource(Supplier<AudioResource> audioResourceSupplier) {
            this.settings.audioResourceSupplier = audioResourceSupplier;
            return this;
        }

        public Builder sourceLocation(Supplier<DynamicLocation> sourceLocationSupplier) {
            this.settings.sourceLocationSupplier = sourceLocationSupplier;
            return this;
        }

        public Builder listenerLocation(Supplier<DynamicLocation> listenerLocationSupplier) {
            this.settings.listenerLocationSupplier = listenerLocationSupplier;
            return this;
        }

        public Builder volumeRatiosAlgorithm(Supplier<VolumeRatiosAlgorithm> volumeRatiosAlgorithmSupplier) {
            this.settings.volumeRatiosAlgorithmSupplier = volumeRatiosAlgorithmSupplier;
            return this;
        }

        public Builder normalizeAlgorithm(Supplier<NormalizeAlgorithm> normalizeAlgorithmSupplier) {
            this.settings.normalizeAlgorithmSupplier = normalizeAlgorithmSupplier;
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
