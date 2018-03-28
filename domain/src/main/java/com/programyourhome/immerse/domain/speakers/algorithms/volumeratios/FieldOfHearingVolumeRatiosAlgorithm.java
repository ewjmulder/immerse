package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.util.MathUtil;

import one.util.streamex.EntryStream;

/**
 * Volume ratios algorithm that takes a certain 'field of hearing': a cone shaped area
 * around the source from within a speaker can produce sound for that source.
 * It will use the angle of the speaker as measurement for relative volume.
 */
public class FieldOfHearingVolumeRatiosAlgorithm implements VolumeRatiosAlgorithm {

    public static final double DEFAULT_MAX_ANGLE = 45;

    private final double maxAngle;

    public FieldOfHearingVolumeRatiosAlgorithm() {
        this(DEFAULT_MAX_ANGLE);
    }

    public FieldOfHearingVolumeRatiosAlgorithm(double maxAngle) {
        this.maxAngle = maxAngle;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Snapshot snapshot) {
        return new SpeakerVolumeRatios(EntryStream.of(snapshot.getScenario().getRoom().getSpeakers())
                .mapValues(speaker -> MathUtil.calculateAngleInDegrees(snapshot, speaker))
                // Only include all speakers inside the 'field of hearing'.
                .filterValues(angle -> angle <= this.maxAngle)
                // A small angle should be a high volume fraction and vice versa.
                .mapValues(MathUtil::reverseAngle)
                .toMap());
    }

}
