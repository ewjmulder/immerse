package com.programyourhome.immerse.domain;

import com.programyourhome.immerse.domain.audio.stopcriterium.StopCriterium;
import com.programyourhome.immerse.domain.speakers.algorithms.SpeakerVolumesAlgorithm;

public class ImmerseSettings {

    private SpeakerVolumesAlgorithm speakerVolumesAlgorithm;
    private StopCriterium stopCriterium;

    public ImmerseSettings(SpeakerVolumesAlgorithm speakerVolumesAlgorithm, StopCriterium stopCriterium) {
        this.speakerVolumesAlgorithm = speakerVolumesAlgorithm;
        this.stopCriterium = stopCriterium;
    }

    public SpeakerVolumesAlgorithm getSpeakerVolumesAlgorithm() {
        return this.speakerVolumesAlgorithm;
    }

    public StopCriterium getStopCriterium() {
        return this.stopCriterium;
    }

}
