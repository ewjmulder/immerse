package com.programyourhome.immerse.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.Speaker;

/**
 * A room object represents an actual room in the real world where Immerse will be running.
 * You can provide a name and description and the dimensions of the room.
 * Furthermore, a room holds the speakers that are present in it.
 */
public class Room implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private UUID id;
    private String name;
    private String description;
    private Vector3D dimensions;
    private final Map<Integer, Speaker> speakers;

    private Room() {
        this(UUID.randomUUID());
    }

    private Room(UUID id) {
        this.id = id;
        this.speakers = new HashMap<>();
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Vector3D getDimensions() {
        return this.dimensions;
    }

    public Map<Integer, Speaker> getSpeakers() {
        return new HashMap<>(this.speakers);
    }

    public Speaker getSpeaker(int id) {
        return this.speakers.get(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Room room;

        public Builder() {
            this.room = new Room();
        }

        public Builder id(UUID id) {
            this.room.id = id;
            return this;
        }

        public Builder name(String name) {
            this.room.name = name;
            return this;
        }

        public Builder description(String description) {
            this.room.description = description;
            return this;
        }

        public Builder dimensions(Vector3D dimensions) {
            this.room.dimensions = dimensions;
            return this;
        }

        public Builder dimensions(double x, double y, double z) {
            return this.dimensions(new Vector3D(x, y, z));
        }

        public Builder addSpeaker(Speaker speaker) {
            this.room.speakers.put(speaker.getId(), speaker);
            return this;
        }

        public Builder addSpeakers(Speaker... speakers) {
            Arrays.stream(speakers).forEach(this::addSpeaker);
            return this;
        }

        public Room build() {
            return this.room;
        }

    }

}
