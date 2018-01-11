package com.programyourhome.immerse.domain.speakers.algorithms.ratiotovolume;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public interface SpeakerRatioToVolumeAlgorithm {

    public void setSpeakerVolumeRatios(SpeakerVolumeRatios speakerVolumeRatios);

    public double calculateVolume(int speakerId);

}
