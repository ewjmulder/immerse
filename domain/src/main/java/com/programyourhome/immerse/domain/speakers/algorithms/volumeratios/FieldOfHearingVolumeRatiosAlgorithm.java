package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.util.MathUtil;

import one.util.streamex.EntryStream;

/**
 * Volume ratios algorithm that takes a certain 'field of hearing': a cone shaped area
 * with the tip at the listener and in the direction of the source with a certain 'cutoff' angle.
 * A speaker within the cone can produce sound for the source, the closer to the center, the louder it will be.
 * Speakers outside the cone (so having a bigger angle than the 'cutoff' angle) will remain silent.
 * So it will use the angle of the speaker as measurement for relative volume.
 *
 * NB: The cutoff angle should not be smaller than twice the angle between speakers, otherwise
 * there can be 'silent spots' in the room, even though the scenario produces sound.
 */
public class FieldOfHearingVolumeRatiosAlgorithm implements VolumeRatiosAlgorithm {

    // The default 'cutoff' angle.
    public static final double DEFAULT_CUTOFF_ANGLE = 45;

    private final double cutoffAngle;

    public FieldOfHearingVolumeRatiosAlgorithm() {
        this(DEFAULT_CUTOFF_ANGLE);
    }

    public FieldOfHearingVolumeRatiosAlgorithm(double cutoffAngle) {
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Snapshot snapshot) {
        return new SpeakerVolumeRatios(EntryStream.of(snapshot.getScenario().getRoom().getSpeakers())
                .mapValues(speaker -> MathUtil.calculateAngleInDegrees(snapshot, speaker))
                // For speakers inside the 'field of hearing', a low angle should be a high volume ratio and vice versa.
                // For speakers not inside the 'field of hearing', it's just 0.
                .mapValues(angle -> angle <= this.cutoffAngle ? this.cutoffAngle - angle : 0.0)
                .toMap());
    }

}
