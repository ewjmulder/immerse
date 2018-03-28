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
                // For speakers inside the 'field of hearing', a low angle should be a high volume ratio and vice versa.
                // For speakers not inside the 'field of hearing', it's just 0.
                .mapValues(angle -> angle <= this.maxAngle ? MathUtil.reverseAngle(angle) : 0.0)
                .toMap());
    }

}
