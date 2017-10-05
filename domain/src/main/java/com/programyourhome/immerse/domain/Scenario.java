package com.programyourhome.immerse.domain;

import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.audio.settings.AudioStreamingSettings;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;

public class Scenario {

    private String name;
    private AudioResource audioResource;
    private DynamicLocation listenerLocation;
    private DynamicLocation sourceLocation;
    private AudioStreamingSettings settings;

    public Scenario(String name, AudioResource audioResource, DynamicLocation listenerLocation,
            DynamicLocation sourceLocation, AudioStreamingSettings settings) {
        this.name = name;
        this.audioResource = audioResource;
        this.listenerLocation = listenerLocation;
        this.sourceLocation = sourceLocation;
        this.settings = settings;
    }

    public String getName() {
        return this.name;
    }

    public AudioResource getAudioResource() {
        return this.audioResource;
    }

    public DynamicLocation getListenerLocation() {
        return this.listenerLocation;
    }

    public DynamicLocation getSourceLocation() {
        return this.sourceLocation;
    }

    public AudioStreamingSettings getSettings() {
        return this.settings;
    }

}
