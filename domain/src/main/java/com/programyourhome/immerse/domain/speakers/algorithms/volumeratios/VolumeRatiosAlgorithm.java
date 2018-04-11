package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

/**
 * Algorithm that can calculate the relative volumes for a scenario snapshot.
 */
public interface VolumeRatiosAlgorithm {

    /**
     * Calculate the volume ratios.
     * The resulting volumes should be non-negative and in the right ratio to each other.
     * The actual numbers in the output do not matter, they should be normalized afterwards.
     */
    public SpeakerVolumeRatios calculateVolumeRatios(Snapshot snapshot);

    /**
     * A fixed ratio.
     */
    public static VolumeRatiosAlgorithm fixed(SpeakerVolumeRatios speakerVolumeRatios) {
        return new FixedVolumeRatiosAlgorithm(speakerVolumeRatios);
    }

    /**
     * Only the closest speaker has volume, the others are silent.
     */
    public static VolumeRatiosAlgorithm onlyClosest() {
        return new OnlyClosestVolumeRatiosAlgorithm();
    }

    /**
     * A 'field of hearing' algorithm with the default 'cutoff' angle.
     */
    public static VolumeRatiosAlgorithm fieldOfHearing() {
        return new FieldOfHearingVolumeRatiosAlgorithm();
    }

    /**
     * A 'field of hearing' algorithm with a certain 'cutoff' angle.
     */
    public static VolumeRatiosAlgorithm fieldOfHearing(double cutoffAngle) {
        return new FieldOfHearingVolumeRatiosAlgorithm(cutoffAngle);
    }

}
