package com.programyourhome.immerse.domain.speakers;

import java.util.HashMap;
import java.util.Map;

public class SpeakerVolumeRatios {

    private final Map<Integer, Double> volumeRatioMap;

    public SpeakerVolumeRatios() {
        this(new HashMap<>());
    }

    public SpeakerVolumeRatios(Map<Integer, Double> volumeRatioMap) {
        this.volumeRatioMap = volumeRatioMap;
    }

    public void setVolumeRatio(int speakerId, double value) {
        this.volumeRatioMap.put(speakerId, value);
    }

    public double getVolumeRatio(int speakerId) {
        return this.volumeRatioMap.get(speakerId);
    }

    public Map<Integer, Double> getVolumeRatioMap() {
        return this.volumeRatioMap;
    }

    @Override
    public String toString() {
        return this.volumeRatioMap.toString();
    }

}
