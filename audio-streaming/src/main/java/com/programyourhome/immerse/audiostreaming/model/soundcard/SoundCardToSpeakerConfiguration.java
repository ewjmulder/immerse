package com.programyourhome.immerse.audiostreaming.model.soundcard;

import java.util.Map;

public class SoundCardToSpeakerConfiguration {

    private String name;
    private String description;
    private Map<Integer, SoundCardSpeakers> mapping;

    public SoundCardToSpeakerConfiguration(String name, String description, Map<Integer, SoundCardSpeakers> mapping) {
        this.name = name;
        this.description = description;
        this.mapping = mapping;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public SoundCardSpeakers getSoundCardSpeakers(int soundCardId) {
        return this.mapping.get(soundCardId);
    }

    public int getSpeakerIdLeft(int soundCardId) {
        return this.mapping.get(soundCardId).getSpeakerIdLeft();
    }

    public int getSpeakerIdRight(int soundCardId) {
        return this.mapping.get(soundCardId).getSpeakerIdRight();
    }

}
