package com.programyourhome.immerse.domain;

import java.io.Serializable;

import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * Settings class for storing the fields that define how a scenario should be played.
 * Since all these classes are interfaces of which the implementation could keep state,
 * they are all defined as Factory's of the actual object, so this settings object can be re-used.
 */
public class Settings implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private Factory<AudioResource> audioResourceFactory;
    private Factory<DynamicLocation> sourceLocationFactory;
    private Factory<DynamicLocation> listenerLocationFactory;
    private Factory<VolumeRatiosAlgorithm> volumeRatiosAlgorithmFactory;
    private Factory<NormalizeAlgorithm> normalizeAlgorithmFactory;
    private Factory<Playback> playbackFactory;

    private Settings() {
    }

    public Factory<AudioResource> getAudioResourceFactory() {
        return this.audioResourceFactory;
    }

    public Factory<DynamicLocation> getSourceLocationFactory() {
        return this.sourceLocationFactory;
    }

    public Factory<DynamicLocation> getListenerLocationFactory() {
        return this.listenerLocationFactory;
    }

    public Factory<VolumeRatiosAlgorithm> getVolumeRatiosAlgorithmFactory() {
        return this.volumeRatiosAlgorithmFactory;
    }

    public Factory<NormalizeAlgorithm> getNormalizeAlgorithmFactory() {
        return this.normalizeAlgorithmFactory;
    }

    public Factory<Playback> getPlaybackFactory() {
        return this.playbackFactory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Settings settings;

        public Builder() {
            this.settings = new Settings();
        }

        public Builder audioResource(Factory<AudioResource> audioResourceFactory) {
            this.settings.audioResourceFactory = audioResourceFactory;
            return this;
        }

        public Builder sourceLocation(Factory<DynamicLocation> sourceLocationFactory) {
            this.settings.sourceLocationFactory = sourceLocationFactory;
            return this;
        }

        public Builder listenerLocation(Factory<DynamicLocation> listenerLocationFactory) {
            this.settings.listenerLocationFactory = listenerLocationFactory;
            return this;
        }

        public Builder volumeRatiosAlgorithm(Factory<VolumeRatiosAlgorithm> volumeRatiosAlgorithmFactory) {
            this.settings.volumeRatiosAlgorithmFactory = volumeRatiosAlgorithmFactory;
            return this;
        }

        public Builder normalizeAlgorithm(Factory<NormalizeAlgorithm> normalizeAlgorithmFactory) {
            this.settings.normalizeAlgorithmFactory = normalizeAlgorithmFactory;
            return this;
        }

        public Builder playback(Factory<Playback> playbackFactory) {
            this.settings.playbackFactory = playbackFactory;
            return this;
        }

        public Settings build() {
            return this.settings;
        }

    }

}
