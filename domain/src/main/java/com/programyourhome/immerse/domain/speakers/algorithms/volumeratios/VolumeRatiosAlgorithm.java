package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import java.io.Serializable;

import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

/**
 * Algorithm that can calculate the relative volumes for an active scenario.
 */
public interface VolumeRatiosAlgorithm extends Serializable {

    /**
     * Calculate the volume ratios.
     * The resulting volumes should be non-negative and in the right ratio to each other.
     * The actual numbers in the output do not matter, they should be normalized afterwards.
     */
    public SpeakerVolumeRatios calculateVolumeRatios(Room room, long millisSinceStart);

}
