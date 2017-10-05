package com.programyourhome.immerse.domain.speakers;

import java.util.HashMap;
import java.util.Map;

public class SpeakerVolumes {

    private Map<Integer, Double> volumeMap;

    public SpeakerVolumes() {
        this(new HashMap<>());
    }

    public SpeakerVolumes(Map<Integer, Double> volumeMap) {
        this.volumeMap = volumeMap;
    }

    public void setVolume(int speakerId, double volume) {
        this.volumeMap.put(speakerId, volume);
    }

    public Double getVolume(int speakerId) {
        return this.volumeMap.get(speakerId);
    }

    @Override
    public String toString() {
        return this.volumeMap.toString();
    }

}
