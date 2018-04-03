package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;
import com.programyourhome.immerse.domain.util.MathUtil;

import one.util.streamex.EntryStream;

public class FractionalNormalizeAlgorithm extends AbstractNormalizeAlgorithm {

    @Override
    public SpeakerVolumes calculateVolumes(SpeakerVolumeRatios speakerVolumeRatios) {
        double lowestRatio = this.getLowestRatio(speakerVolumeRatios);
        double highestRatio = this.getHighestRatio(speakerVolumeRatios);
        return new SpeakerVolumes(EntryStream.of(speakerVolumeRatios.getVolumeRatioMap())
                .mapValues(volumeRatio -> MathUtil.calculateFraction(lowestRatio, highestRatio, volumeRatio))
                .toMap());
    }

}
