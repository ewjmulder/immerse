package com.programyourhome.immerse.domain;

import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;

//TODO: refactor the domain ideas:
//- Include room instance in a scenario: it makes no sense to have location specifiers without knowing the room dimensions
//- Rename 'scene' to something that is more 'static', like 'snapshot' or 'state'
//- Should speaker know it's own coordinates?
//- SoundCardStream naar audio-streaming
//- Add option for dynamic volume, independent from location of sound source.
//- Keep type aliasses like SpeakerVolumeRatios?
//- Move all general math stuff to MathUtil class
//- All domain classes should be without state, esp. the whole scenario tree!

public class Scenario {

    private String name;
    private String description;
    private Room room;
    private AudioResource audioResource;
    private DynamicLocation sourceLocation;
    private DynamicLocation listenerLocation;
    private ImmerseSettings settings;

    private Scenario() {
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Room getRoom() {
        return this.room;
    }

    public AudioResource getAudioResource() {
        return this.audioResource;
    }

    public DynamicLocation getSourceLocation() {
        return this.sourceLocation;
    }

    public DynamicLocation getListenerLocation() {
        return this.listenerLocation;
    }

    public ImmerseSettings getSettings() {
        return this.settings;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Scenario scenario;

        public Builder() {
            this.scenario = new Scenario();
        }

        public Builder name(String name) {
            this.scenario.name = name;
            return this;
        }

        public Builder description(String description) {
            this.scenario.description = description;
            return this;
        }

        public Builder room(Room room) {
            this.scenario.room = room;
            return this;
        }

        public Builder audioResource(AudioResource audioResource) {
            this.scenario.audioResource = audioResource;
            return this;
        }

        public Builder sourceLocation(DynamicLocation sourceLocation) {
            this.scenario.sourceLocation = sourceLocation;
            return this;
        }

        public Builder listenerLocation(DynamicLocation listenerLocation) {
            this.scenario.listenerLocation = listenerLocation;
            return this;
        }

        public Builder settings(ImmerseSettings settings) {
            this.scenario.settings = settings;
            return this;
        }

        public Scenario build() {
            return this.scenario;
        }

    }

}
