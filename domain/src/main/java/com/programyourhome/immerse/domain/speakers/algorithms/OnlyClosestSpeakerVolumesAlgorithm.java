package com.programyourhome.immerse.domain.speakers.algorithms;

import java.util.Map;
import java.util.Map.Entry;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public class OnlyClosestSpeakerVolumesAlgorithm implements SpeakerVolumesAlgorithm {

    @Override
    public SpeakerVolumes calculateSpeakerVolumes(Scene scene) {
        Map<Integer, Double> speakerAngles = EntryStream.of(scene.getRoom().getSpeakers())
                .mapValues(speaker -> this.calculateAngleInDegrees(scene, speaker))
                .toMap();

        // System.out.println("Listener: " + scene.getListener());
        // System.out.println("Source: " + scene.getSource());
        // System.out.println("Angles: " + speakerAngles);

        int speakerIdOfMinAngle = EntryStream.of(speakerAngles).minBy(Entry::getValue).get().getKey();

        // System.out.println("speakerIdOfMinAngle: " + speakerIdOfMinAngle);

        return new SpeakerVolumes(StreamEx.of(scene.getRoom().getSpeakers().keySet()).toMap(speakerId -> speakerId == speakerIdOfMinAngle ? 1.0 : 0.0));
    }

    // TODO: unit tests!

}
