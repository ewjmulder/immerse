package com.programyourhome.immerse.domain.speakers;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;

public class SpeakerVolumes {

    private final NormalizeAlgorithm normalizeAlgorithm;

    public SpeakerVolumes(Snapshot snapshot) {
        SpeakerVolumeRatios speakerVolumeRatios = snapshot.getScenario().getSettings().getVolumeRatiosAlgorithm().calculateVolumeRatios(snapshot);
        this.normalizeAlgorithm = snapshot.getScenario().getSettings().getNormalizeAlgorithm();
        this.normalizeAlgorithm.setSpeakerVolumeRatios(speakerVolumeRatios);
    }

    public double getVolumeFraction(int speakerId) {
        // TODO: caching?
        return this.normalizeAlgorithm.calculateVolumeFraction(speakerId);
    }

}
