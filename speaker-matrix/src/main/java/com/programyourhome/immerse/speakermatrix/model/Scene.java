package com.programyourhome.immerse.speakermatrix.model;

public class Scene {

    private Room room;
    private Vector3D listener;
    private Vector3D source;
    private SpeakerMatrixSettings settings;

    public Scene(Room room, Vector3D listener, Vector3D source, SpeakerMatrixSettings settings) {
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

    public SpeakerMatrixSettings getSettings() {
        return this.settings;
    }

}
