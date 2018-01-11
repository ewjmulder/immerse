package com.programyourhome.immerse.domain.speakers.algorithms.ratiotovolume;

public class NormalizeRatioToVolumeAlgorithm extends AbstractSpeakerRatioToVolumeAlgorithm {

    @Override
    public double calculateVolume(int speakerId) {
        double volumeRatio = this.getVolumeRatio(speakerId);
        return this.calculateFraction(this.getLowestRatio(), this.getHighestRatio(), volumeRatio);
    }

}
