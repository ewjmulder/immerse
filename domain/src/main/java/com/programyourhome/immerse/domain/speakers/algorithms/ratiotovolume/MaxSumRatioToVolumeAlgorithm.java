package com.programyourhome.immerse.domain.speakers.algorithms.ratiotovolume;

public class MaxSumRatioToVolumeAlgorithm extends AbstractSpeakerRatioToVolumeAlgorithm {

    private final double divisionFactor;

    public MaxSumRatioToVolumeAlgorithm(double maxSum) {
        double ratioSum = this.streamRatios().sum();
        this.divisionFactor = maxSum / ratioSum;
    }

    @Override
    public double calculateVolume(int speakerId) {
        double volume = this.getVolumeRatio(speakerId) / this.divisionFactor;
        // Cut off at max volume of 1, to not get distortions.
        return Math.min(volume, 1);
    }

}
