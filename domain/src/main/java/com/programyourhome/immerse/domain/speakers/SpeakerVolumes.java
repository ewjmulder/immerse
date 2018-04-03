package com.programyourhome.immerse.domain.speakers;

import java.util.HashMap;
import java.util.Map;

public class SpeakerVolumes {

    private final Map<Integer, Double> volumeMap;

    public SpeakerVolumes(Map<Integer, Double> volumesMap) {
        this.volumeMap = volumesMap;
    }

    public double getVolumeFraction(int speakerId) {
        return this.volumeMap.get(speakerId);
    }

    public Map<Integer, Double> getVolumeMap() {
        return new HashMap<>(this.volumeMap);
    }

    @Override
    public String toString() {
        return this.volumeMap.toString();
    }

}
