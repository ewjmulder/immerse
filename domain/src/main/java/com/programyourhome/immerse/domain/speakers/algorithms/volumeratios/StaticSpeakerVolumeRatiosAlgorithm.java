package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public class StaticSpeakerVolumeRatiosAlgorithm implements SpeakerVolumeRatiosAlgorithm {

    private SpeakerVolumeRatios speakerVolumes;

    public StaticSpeakerVolumeRatiosAlgorithm(SpeakerVolumeRatios speakerVolumes) {
        this.speakerVolumes = speakerVolumes;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Scene scene) {
        return this.speakerVolumes;
    }

}
