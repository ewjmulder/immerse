package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

public class MaxSumNormalizeAlgorithm extends AbstractNormalizeAlgorithm {

    private final double divisionFactor;

    public MaxSumNormalizeAlgorithm(double maxSum) {
        double ratioSum = this.streamRatios().sum();
        this.divisionFactor = maxSum / ratioSum;
    }

    @Override
    public double calculateVolumeFraction(int speakerId) {
        double volume = this.getVolumeRatio(speakerId) / this.divisionFactor;
        // Cut off at max volume of 1, to not get distortions.
        return Math.min(volume, 1);
    }

}
