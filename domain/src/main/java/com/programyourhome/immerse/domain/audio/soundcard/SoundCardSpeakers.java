package com.programyourhome.immerse.domain.audio.soundcard;

public class SoundCardSpeakers {

    private int speakerIdLeft;
    private int speakerIdRight;

    public SoundCardSpeakers(int speakerIdLeft, int speakerIdRight) {
        this.speakerIdLeft = speakerIdLeft;
        this.speakerIdRight = speakerIdRight;
    }

    public int getSpeakerIdLeft() {
        return this.speakerIdLeft;
    }

    public int getSpeakerIdRight() {
        return this.speakerIdRight;
    }

}
