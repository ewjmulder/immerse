package com.programyourhome.immerse.speakermatrix.model;

import java.util.HashMap;
import java.util.Map;

public class Room {

    private String name;
    private String description;
    // TODO: add dimensions of the room, so scneario's can take this into account.
    private Map<Integer, Speaker> speakers;

    public Room(String name, String description) {
        this.name = name;
        this.description = description;
        this.speakers = new HashMap<>();
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Map<Integer, Speaker> getSpeakers() {
        return this.speakers;
    }

    public void addSpeaker(Speaker speaker) {
        this.speakers.put(speaker.getId(), speaker);
    }

    public void removeSpeaker(int id) {
        this.speakers.remove(id);
    }

}
