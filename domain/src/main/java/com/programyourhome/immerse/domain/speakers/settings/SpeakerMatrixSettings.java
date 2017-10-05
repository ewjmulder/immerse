package com.programyourhome.immerse.domain.speakers.settings;

public class SpeakerMatrixSettings {

    private SurroundMode surroundMode;

    public SpeakerMatrixSettings(SurroundMode surroundMode) {
        this.surroundMode = surroundMode;
    }

    public SurroundMode getSurroundMode() {
        return this.surroundMode;
    }

}
