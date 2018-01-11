package com.programyourhome.immerse.domain;

import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;

//TODO: refactor the domain ideas:
//- Include room instance in a scenario: it makes no sense to have location specifiers without knowing the room dimensions
//- Rename 'scene' to something that is more 'static', like 'snapshot' or 'state'

public class Scenario {

    private final String name;
    private final AudioResource audioResource;
    private final DynamicLocation listenerLocation;
    private final DynamicLocation sourceLocation;
    private final ImmerseSettings settings;

    public Scenario(String name, AudioResource audioResource, DynamicLocation listenerLocation,
            DynamicLocation sourceLocation, ImmerseSettings settings) {
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

    public ImmerseSettings getSettings() {
        return this.settings;
    }

}
