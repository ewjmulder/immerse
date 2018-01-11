package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import java.util.Map;
import java.util.Map.Entry;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public class OnlyClosestSpeakerVolumeRatiosAlgorithm implements SpeakerVolumeRatiosAlgorithm {

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Scene scene) {
        Map<Integer, Double> speakerAngles = EntryStream.of(scene.getRoom().getSpeakers())
                .mapValues(speaker -> this.calculateAngleInDegrees(scene, speaker))
                .toMap();
        int speakerIdOfMinAngle = EntryStream.of(speakerAngles).minBy(Entry::getValue).get().getKey();
        return new SpeakerVolumeRatios(StreamEx.of(scene.getRoom().getSpeakers().keySet()).toMap(speakerId -> speakerId == speakerIdOfMinAngle ? 1.0 : 0.0));
    }

}
