package com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios;

import java.util.Map;
import java.util.Map.Entry;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;
import com.programyourhome.immerse.toolbox.util.MathUtil;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

/**
 * Volume ratios algorithm that gives volume only to the speaker closest to the sound source (measured in angle).
 * The other speakers will be silent.
 * Also in case of multiple speakers being the closest, just one will get volume.
 */
public class OnlyClosestVolumeRatiosAlgorithm extends AbstractLocationBasedVolumeRatiosAlgorithm {

    private static final long serialVersionUID = Serialization.VERSION;

    public OnlyClosestVolumeRatiosAlgorithm(Room room, DynamicLocation sourceLocation, DynamicLocation listenerLocation) {
        super(room, sourceLocation, listenerLocation);
    }

    @Override
    protected SpeakerVolumeRatios calculateVolumeRatios(Room room, Vector3D sourceLocation, Vector3D listenerLocation) {
        Map<Integer, Double> speakerAngles = EntryStream.of(room.getSpeakers())
                .mapValues(speaker -> MathUtil.calculateAngleInDegrees(sourceLocation, listenerLocation, speaker))
                .toMap();
        int speakerIdOfMinAngle = EntryStream.of(speakerAngles).minBy(Entry::getValue).get().getKey();
        return new SpeakerVolumeRatios(
                StreamEx.of(room.getSpeakers().keySet()).toMap(speakerId -> speakerId == speakerIdOfMinAngle ? 1.0 : 0.0));
    }

    public static Factory<VolumeRatiosAlgorithm> onlyClosest(Room room, Factory<DynamicLocation> sourceLocation, Factory<DynamicLocation> listenerLocation) {
        return new Factory<VolumeRatiosAlgorithm>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public VolumeRatiosAlgorithm create() {
                return new OnlyClosestVolumeRatiosAlgorithm(room, sourceLocation.create(), listenerLocation.create());
            }
        };
    }

}
