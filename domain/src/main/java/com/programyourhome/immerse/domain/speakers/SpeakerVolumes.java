package com.programyourhome.immerse.domain.speakers;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.algorithms.ratiotovolume.SpeakerRatioToVolumeAlgorithm;

public class SpeakerVolumes {

    private SpeakerRatioToVolumeAlgorithm speakerRatioToVolumeAlgorithm;

    public SpeakerVolumes(Scene scene) {
        SpeakerVolumeRatios speakerVolumeRatios = scene.getSettings().getSpeakerVolumeRatiosAlgorithm().calculateVolumeRatios(scene);
        SpeakerRatioToVolumeAlgorithm speakerRatioToVolumeAlgorithm = scene.getSettings().getSpeakerRatioToVolumeAlgorithm();
        speakerRatioToVolumeAlgorithm.setSpeakerVolumeRatios(speakerVolumeRatios);
    }

    public double getVolume(int speakerId) {
        return this.speakerRatioToVolumeAlgorithm.calculateVolume(speakerId);
    }

}
