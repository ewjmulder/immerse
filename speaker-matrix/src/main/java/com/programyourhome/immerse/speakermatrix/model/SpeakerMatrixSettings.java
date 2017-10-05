package com.programyourhome.immerse.speakermatrix.model;

public class SpeakerMatrixSettings {

    private SurroundMode surroundMode;

    public SpeakerMatrixSettings(SurroundMode surroundMode) {
        this.surroundMode = surroundMode;
    }

    public SurroundMode getSurroundMode() {
        return this.surroundMode;
    }

}
