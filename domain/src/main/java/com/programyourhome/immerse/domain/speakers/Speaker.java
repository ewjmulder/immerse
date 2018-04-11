package com.programyourhome.immerse.domain.speakers;

import com.programyourhome.immerse.domain.location.Vector3D;

/**
 * Represents a physical speaker at a specific location.
 */
public class Speaker {

    private int id;
    private String name;
    private String description;
    private Vector3D position;

    private Speaker() {
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    @Override
    public String toString() {
        return this.getId() + "-" + this.getName() + " @ " + this.position;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Speaker speaker;

        public Builder() {
            this.speaker = new Speaker();
        }

        public Builder id(int id) {
            this.speaker.id = id;
            return this;
        }

        public Builder name(String name) {
            this.speaker.name = name;
            return this;
        }

        public Builder description(String description) {
            this.speaker.description = description;
            return this;
        }

        public Builder position(Vector3D vector) {
            this.speaker.position = vector;
            return this;
        }

        public Builder position(double x, double y, double z) {
            return this.position(new Vector3D(x, y, z));
        }

        public Speaker build() {
            return this.speaker;
        }
    }

}
