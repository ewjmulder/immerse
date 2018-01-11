package com.programyourhome.immerse.domain;

import com.programyourhome.immerse.domain.audio.stopcriterium.StopCriterium;
import com.programyourhome.immerse.domain.speakers.algorithms.ratiotovolume.SpeakerRatioToVolumeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.SpeakerVolumeRatiosAlgorithm;

public class ImmerseSettings {

    private final SpeakerVolumeRatiosAlgorithm speakerVolumeRatiosAlgorithm;
    private final SpeakerRatioToVolumeAlgorithm speakerRatioToVolumeAlgorithm;
    private final StopCriterium stopCriterium;

    public ImmerseSettings(SpeakerVolumeRatiosAlgorithm speakerVolumeRatiosAlgorithm,
            SpeakerRatioToVolumeAlgorithm speakerRatioToVolumeAlgorithm, StopCriterium stopCriterium) {
        this.speakerVolumeRatiosAlgorithm = speakerVolumeRatiosAlgorithm;
        this.speakerRatioToVolumeAlgorithm = speakerRatioToVolumeAlgorithm;
        this.stopCriterium = stopCriterium;
    }

    public SpeakerVolumeRatiosAlgorithm getSpeakerVolumeRatiosAlgorithm() {
        return this.speakerVolumeRatiosAlgorithm;
    }

    public SpeakerRatioToVolumeAlgorithm getSpeakerRatioToVolumeAlgorithm() {
        return this.speakerRatioToVolumeAlgorithm;
    }

    public StopCriterium getStopCriterium() {
        return this.stopCriterium;
    }

}
