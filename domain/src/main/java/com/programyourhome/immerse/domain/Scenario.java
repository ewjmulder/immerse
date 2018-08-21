package com.programyourhome.immerse.domain;

import java.io.Serializable;

/**
 * A scenario represents an audio resource in a room with speakers, with a dynamic source and listener locations.
 * A scenario should be played according to these properties and the algorithm settings.
 * The scenario object and fields themselves will not contain state, so they can be re-used for repeated playback.
 */
public class Scenario implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private String name;
    private String description;
    private Room room;
    private ScenarioSettings settings;

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

    public ScenarioSettings getSettings() {
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

        public Builder settings(ScenarioSettings settings) {
            this.scenario.settings = settings;
            return this;
        }

        public Scenario build() {
            return this.scenario;
        }

    }

}
