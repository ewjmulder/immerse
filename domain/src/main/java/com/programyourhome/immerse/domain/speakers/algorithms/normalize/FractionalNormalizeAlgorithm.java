package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

public class FractionalNormalizeAlgorithm extends AbstractNormalizeAlgorithm {

    @Override
    public double calculateVolumeFraction(int speakerId) {
        double volumeRatio = this.getVolumeRatio(speakerId);
        return this.calculateFraction(this.getLowestRatio(), this.getHighestRatio(), volumeRatio);
    }

}
