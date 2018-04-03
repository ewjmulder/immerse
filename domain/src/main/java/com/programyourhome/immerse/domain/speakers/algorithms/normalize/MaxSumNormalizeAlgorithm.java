package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

import one.util.streamex.EntryStream;

public class MaxSumNormalizeAlgorithm extends AbstractNormalizeAlgorithm {

    private final double maxSum;

    public MaxSumNormalizeAlgorithm(double maxSum) {
        this.maxSum = maxSum;
    }

    @Override
    public SpeakerVolumes calculateVolumes(SpeakerVolumeRatios speakerVolumeRatios) {
        double divisionFactor = this.getRatioSum(speakerVolumeRatios) / this.maxSum;
        return new SpeakerVolumes(EntryStream.of(speakerVolumeRatios.getVolumeRatioMap())
                // Cut off at max volume of 1, to not get distortions.
                .mapValues(volumeRatio -> Math.min(1, volumeRatio / divisionFactor))
                .toMap());
    }

}
