package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.util.MathUtil;

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

    public static VolumeRatiosAlgorithm fixed(SpeakerVolumeRatios speakerVolumeRatios) {
        return new FixedVolumeRatiosAlgorithm(speakerVolumeRatios);
    }

    public static VolumeRatiosAlgorithm onlyClosest() {
        return new OnlyClosestVolumeRatiosAlgorithm();
    }

    public static VolumeRatiosAlgorithm fieldOfHearing() {
        return new FieldOfHearingVolumeRatiosAlgorithm();
    }

    public static VolumeRatiosAlgorithm fieldOfHearing(double maxAngle) {
        return new FieldOfHearingVolumeRatiosAlgorithm(maxAngle);
    }

    // TODO: unit test!
    public static void main(String[] args) {
        Vector listener = Vector.fromArray(new double[] { 5, 5, 5 });
        Vector source = Vector.fromArray(new double[] { 10, 5, 5 });
        Vector speaker1 = Vector.fromArray(new double[] { 10, 4, 4 });
        Vector speaker2 = Vector.fromArray(new double[] { 10, 7, 7 });
        Vector speaker3 = Vector.fromArray(new double[] { 10, 8, 2 });

        System.out.println(MathUtil.calculateAngleInDegrees(listener, source, speaker1));
        System.out.println(MathUtil.calculateAngleInDegrees(listener, source, speaker2));
        System.out.println(MathUtil.calculateAngleInDegrees(listener, source, speaker3));
    }

}
