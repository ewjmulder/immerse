package com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * Fixed volume ratios, independent of the scene and locations.
 */
public class FixedVolumeRatiosAlgorithm implements VolumeRatiosAlgorithm {

    private final SpeakerVolumeRatios speakerVolumeRatios;

    public FixedVolumeRatiosAlgorithm(SpeakerVolumeRatios speakerVolumeRatios) {
        this.speakerVolumeRatios = speakerVolumeRatios;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Snapshot scene) {
        return this.speakerVolumeRatios;
    }

    public static Supplier<VolumeRatiosAlgorithm> fixed(SpeakerVolumeRatios speakerVolumeRatios) {
        return () -> new FixedVolumeRatiosAlgorithm(speakerVolumeRatios);
    }

}
