package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

import one.util.streamex.DoubleStreamEx;

public abstract class AbstractNormalizeAlgorithm implements NormalizeAlgorithm {

    public DoubleStreamEx streamRatios(SpeakerVolumeRatios speakerVolumeRatios) {
        return DoubleStreamEx.of(speakerVolumeRatios.getVolumeRatioMap().values());
    }

    public double getLowestRatio(SpeakerVolumeRatios speakerVolumeRatios) {
        return this.streamRatios(speakerVolumeRatios).min().getAsDouble();
    }

    public double getHighestRatio(SpeakerVolumeRatios speakerVolumeRatios) {
        return this.streamRatios(speakerVolumeRatios).max().getAsDouble();
    }

    public double getRatioSum(SpeakerVolumeRatios speakerVolumeRatios) {
        return this.streamRatios(speakerVolumeRatios).sum();
    }

}
