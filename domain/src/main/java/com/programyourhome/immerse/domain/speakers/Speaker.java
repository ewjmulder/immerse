package com.programyourhome.immerse.domain.speakers;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.location.Vector3D;

public class Speaker {

    private int id;
    private String name;
    private String description;
    private Vector3D vector3D;

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

    public Vector3D getVector3D() {
        return this.vector3D;
    }

    public Vector getVectorLa4j() {
        return this.vector3D.toLa4j();
    }

    @Override
    public String toString() {
        return this.getId() + "-" + this.getName() + " @ " + this.vector3D;
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

        public Builder vector(Vector3D vector) {
            this.speaker.vector3D = vector;
            return this;
        }

        public Builder vector(double x, double y, double z) {
            return this.vector(new Vector3D(x, y, z));
        }

        public Speaker build() {
            return this.speaker;
        }
    }

}
