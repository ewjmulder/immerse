package com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios;

import java.util.stream.Collectors;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * Fixed volume ratios, independent of the scene and locations.
 */
public class FixedVolumeRatiosAlgorithm implements VolumeRatiosAlgorithm {

    private static final long serialVersionUID = Serialization.VERSION;

    private final SpeakerVolumeRatios speakerVolumeRatios;

    public FixedVolumeRatiosAlgorithm(SpeakerVolumeRatios speakerVolumeRatios) {
        this.speakerVolumeRatios = speakerVolumeRatios;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Snapshot scene) {
        return this.speakerVolumeRatios;
    }

    public static Factory<VolumeRatiosAlgorithm> fixed(SpeakerVolumeRatios speakerVolumeRatios) {
        return new Factory<VolumeRatiosAlgorithm>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public VolumeRatiosAlgorithm create() {
                return new FixedVolumeRatiosAlgorithm(speakerVolumeRatios);
            }
        };
    }

    /**
     * Map all speakers to volume 0, just the speaker with the given id to volume 1.
     */
    public static Factory<VolumeRatiosAlgorithm> justSpeaker(Room room, int speakerId) {
        SpeakerVolumeRatios justSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> speaker.getId() == speakerId ? 1.0 : 0.0)));
        return fixed(justSpeakerVolumeRatios);
    }

}
