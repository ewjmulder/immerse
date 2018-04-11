package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import java.util.Map;
import java.util.Map.Entry;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.util.MathUtil;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

/**
 * Volume ratios algorithm that gives volume only to the speaker closest to the sound source (measured in angle).
 * The other speakers will be silent.
 * Also in case of multiple speakers being the closest, just one will get volume.
 */
public class OnlyClosestVolumeRatiosAlgorithm implements VolumeRatiosAlgorithm {

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Snapshot snapshot) {
        Map<Integer, Double> speakerAngles = EntryStream.of(snapshot.getScenario().getRoom().getSpeakers())
                .mapValues(speaker -> MathUtil.calculateAngleInDegrees(snapshot, speaker))
                .toMap();
        int speakerIdOfMinAngle = EntryStream.of(speakerAngles).minBy(Entry::getValue).get().getKey();
        return new SpeakerVolumeRatios(
                StreamEx.of(snapshot.getScenario().getRoom().getSpeakers().keySet()).toMap(speakerId -> speakerId == speakerIdOfMinAngle ? 1.0 : 0.0));
    }

}
