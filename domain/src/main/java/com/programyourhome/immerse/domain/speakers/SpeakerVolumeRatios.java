package com.programyourhome.immerse.domain.speakers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.programyourhome.immerse.domain.Serialization;

/**
 * Convenience wrapper around a volume ratio map.
 * Represents the relative volumes for each speaker.
 * The values cannot be used directly as volumes, but need normalization first.
 */
public class SpeakerVolumeRatios implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private final Map<Integer, Double> volumeRatioMap;

    public SpeakerVolumeRatios(Map<Integer, Double> volumeRatioMap) {
        this.volumeRatioMap = volumeRatioMap;
    }

    public double getVolumeRatio(int speakerId) {
        return this.volumeRatioMap.get(speakerId);
    }

    public Map<Integer, Double> getVolumeRatioMap() {
        return new HashMap<>(this.volumeRatioMap);
    }

    @Override
    public String toString() {
        return this.volumeRatioMap.toString();
    }

}
