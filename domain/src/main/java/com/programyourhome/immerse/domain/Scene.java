package com.programyourhome.immerse.domain;

import com.programyourhome.immerse.domain.exception.ValidationException;
import com.programyourhome.immerse.domain.location.Vector3D;

public class Scene {

    private Room room;
    private Vector3D listener;
    private Vector3D source;
    private ImmerseSettings settings;

    public Scene(Room room, Vector3D listener, Vector3D source, ImmerseSettings settings) {
        this.room = room;
        this.listener = listener;
        this.source = source;
        this.settings = settings;
    }

    public Room getRoom() {
        return this.room;
    }

    public Vector3D getListener() {
        return this.listener;
    }

    public Vector3D getSource() {
        return this.source;
    }

    public ImmerseSettings getSettings() {
        return this.settings;
    }

    public void validate() {
        if (this.getRoom().getSpeakers().isEmpty()) {
            throw new ValidationException("No speakers configured!");
        }
        // TODO: more validation, like:
        // room not null, validate speaker data
    }

}
