package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public class FixedVolumeRatiosAlgorithm implements VolumeRatiosAlgorithm {

    private SpeakerVolumeRatios speakerVolumeRatios;

    public FixedVolumeRatiosAlgorithm(SpeakerVolumeRatios speakerVolumeRatios) {
        this.speakerVolumeRatios = speakerVolumeRatios;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Snapshot scene) {
        return this.speakerVolumeRatios;
    }

}
