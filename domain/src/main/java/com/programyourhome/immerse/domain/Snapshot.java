package com.programyourhome.immerse.domain;

import com.programyourhome.immerse.domain.location.Vector3D;

public class Snapshot {

    private Scenario scenario;
    private Vector3D source;
    private Vector3D listener;

    private Snapshot() {
    }

    public Scenario getScenario() {
        return this.scenario;
    }

    public Vector3D getSource() {
        return this.source;
    }

    public Vector3D getListener() {
        return this.listener;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Snapshot snapshot;

        public Builder() {
            this.snapshot = new Snapshot();
        }

        public Builder scenario(Scenario scenario) {
            this.snapshot.scenario = scenario;
            return this;
        }

        public Builder source(Vector3D source) {
            this.snapshot.source = source;
            return this;
        }

        public Builder source(double x, double y, double z) {
            return this.source(new Vector3D(x, y, z));
        }

        public Builder listener(Vector3D listener) {
            this.snapshot.listener = listener;
            return this;
        }

        public Builder listener(double x, double y, double z) {
            return this.listener(new Vector3D(x, y, z));
        }

        public Snapshot build() {
            return this.snapshot;
        }
    }

}
