package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

import one.util.streamex.DoubleStreamEx;

public abstract class AbstractNormalizeAlgorithm implements NormalizeAlgorithm {

    private SpeakerVolumeRatios speakerVolumeRatios;

    @Override
    public void setSpeakerVolumeRatios(SpeakerVolumeRatios speakerVolumeRatios) {
        this.speakerVolumeRatios = speakerVolumeRatios;
    }

    public double getVolumeRatio(int speakerId) {
        return this.speakerVolumeRatios.getVolumeRatio(speakerId);
    }

    public DoubleStreamEx streamRatios() {
        return DoubleStreamEx.of(this.speakerVolumeRatios.getVolumeRatioMap().values());
    }

    // TODO: cache lowest and highest
    public double getLowestRatio() {
        return this.streamRatios().min().getAsDouble();
    }

    public double getHighestRatio() {
        return this.streamRatios().max().getAsDouble();
    }

    // TODO: move to Math util class?
    public double calculateFraction(double min, double max, double value) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("Value " + value + " must be between " + min + " and " + max);
        }
        double diff = max - min;
        double fraction;
        if (diff == 0) {
            // Special case: no diff, so default to 1.
            fraction = 1;
        } else {
            fraction = (value - min) / diff;
        }
        return fraction;
    }

}
