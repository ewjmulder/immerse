package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

import one.util.streamex.EntryStream;

/**
 * Max sum normalize algorithm that takes the ratio between the input volumes
 * and tunes that down until all values summed are equal to the max sum.
 *
 * The idea is that this will produce a constant 'amount' of sound, independent of the speaker distribution.
 * You can provide a total max value higher than 1, but individual speaker volumes will always be cut off at 1.
 */
public class MaxSumNormalizeAlgorithm implements NormalizeAlgorithm {

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

    public static Supplier<NormalizeAlgorithm> maxSum(double maxSum) {
        return () -> new MaxSumNormalizeAlgorithm(maxSum);
    }

}
