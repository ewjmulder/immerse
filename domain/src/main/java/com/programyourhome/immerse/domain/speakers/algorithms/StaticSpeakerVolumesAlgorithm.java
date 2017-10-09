package com.programyourhome.immerse.domain.speakers.algorithms;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

public class StaticSpeakerVolumesAlgorithm implements SpeakerVolumesAlgorithm {

    private SpeakerVolumes speakerVolumes;

    public StaticSpeakerVolumesAlgorithm(SpeakerVolumes speakerVolumes) {
        this.speakerVolumes = speakerVolumes;
    }

    @Override
    public SpeakerVolumes calculateSpeakerVolumes(Scene scene) {
        return this.speakerVolumes;
    }

}
