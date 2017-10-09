package com.programyourhome.immerse.domain.audio.soundcard;

public class SoundCardSpeakers {

    private int speakerIdLeft;
    private int speakerIdRight;

    // TODO: support for no speaker (-1?)
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
