package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

import one.util.streamex.EntryStream;

public class FieldOfHearingFractionalSpeakerVolumeRatiosAlgorithm implements SpeakerVolumeRatiosAlgorithm {

    private final double maxAngle;

    public FieldOfHearingFractionalSpeakerVolumeRatiosAlgorithm(double maxAngle) {
        this.maxAngle = maxAngle;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Scene scene) {
        return new SpeakerVolumeRatios(EntryStream.of(scene.getRoom().getSpeakers())
                .mapValues(speaker -> this.calculateAngleInDegrees(scene, speaker))
                // Only include all speakers inside the 'field of hearing'.
                .filterValues(angle -> angle <= this.maxAngle)
                // A small angle should be a high volume fraction and vice versa.
                .mapValues(this::reverseAngle)
                .toMap());
    }

}
