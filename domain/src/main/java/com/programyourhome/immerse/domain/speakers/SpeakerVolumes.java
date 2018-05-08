package com.programyourhome.immerse.domain.speakers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.programyourhome.immerse.domain.Serialization;

/**
 * Convenience wrapper around a volume map.
 * Represents the absolute volumes for each speaker, as a value between 0 (mute) and 1 (max volume).
 * NB: These volumes are meant as a multiplier of the amplitudes of the audio data,
 * so 1 does not always mean very loud, this depends on the actual audio resources.
 */
public class SpeakerVolumes implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

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
