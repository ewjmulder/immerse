package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

import one.util.streamex.DoubleStreamEx;

/**
 * Algorithm that can turn a series of speaker volume ratios into values between 0 and 1 for each speaker.
 */
public interface NormalizeAlgorithm {

    /**
     * Calculate the normalized volume for all speakers.
     */
    public SpeakerVolumes calculateVolumes(SpeakerVolumeRatios speakerVolumeRatios);

    public default DoubleStreamEx streamRatios(SpeakerVolumeRatios speakerVolumeRatios) {
        return DoubleStreamEx.of(speakerVolumeRatios.getVolumeRatioMap().values());
    }

    public default double getLowestRatio(SpeakerVolumeRatios speakerVolumeRatios) {
        return this.streamRatios(speakerVolumeRatios).min().getAsDouble();
    }

    public default double getHighestRatio(SpeakerVolumeRatios speakerVolumeRatios) {
        return this.streamRatios(speakerVolumeRatios).max().getAsDouble();
    }

    public default double getRatioSum(SpeakerVolumeRatios speakerVolumeRatios) {
        return this.streamRatios(speakerVolumeRatios).sum();
    }

}
