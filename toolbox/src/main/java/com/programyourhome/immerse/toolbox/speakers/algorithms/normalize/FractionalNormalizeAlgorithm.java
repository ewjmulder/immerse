package com.programyourhome.immerse.toolbox.speakers.algorithms.normalize;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.toolbox.util.MathUtil;

import one.util.streamex.EntryStream;

/**
 * Fractional normalize algorithm that takes the ratio between the input volumes
 * and tunes that down until all values are between 0 and 1. That means there will always
 * be at least one speaker with a volume of 1.
 *
 * The idea is that this will produce a maximum amount of sound, while keeping the ratio intact.
 */
public class FractionalNormalizeAlgorithm implements NormalizeAlgorithm {

    private static final long serialVersionUID = Serialization.VERSION;

    @Override
    public SpeakerVolumes calculateVolumes(SpeakerVolumeRatios speakerVolumeRatios) {
        double lowestRatio = this.getLowestRatio(speakerVolumeRatios);
        double highestRatio = this.getHighestRatio(speakerVolumeRatios);
        return new SpeakerVolumes(EntryStream.of(speakerVolumeRatios.getVolumeRatioMap())
                .mapValues(volumeRatio -> MathUtil.calculateFractionInRange(lowestRatio, highestRatio, volumeRatio))
                .toMap());
    }

    public static Factory<NormalizeAlgorithm> fractional() {
        return new Factory<NormalizeAlgorithm>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public NormalizeAlgorithm create() {
                return new FractionalNormalizeAlgorithm();
            }
        };
    }

}
