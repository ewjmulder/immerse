package com.programyourhome.immerse.domain.speakers.algorithms;

import java.util.Map;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

import one.util.streamex.DoubleStreamEx;
import one.util.streamex.EntryStream;

public class FractionalSpeakerVolumesAlgorithm implements SpeakerVolumesAlgorithm {

    @Override
    public SpeakerVolumes calculateSpeakerVolumes(Scene scene) {
        Map<Integer, Double> speakerAngles = EntryStream.of(scene.getRoom().getSpeakers())
                .mapValues(speaker -> this.calculateAngleInDegrees(scene, speaker))
                .toMap();
        // TODO: decide on algorithm to use for dividing the volumes among the speakers. + make configurable
        // TODO: current algorithm has the quirk that adding extra speakers 'far away' will influence the volume of the otherwise identical scene.
        // But that is also sortof a 'feature': not having enough speakers is a sub-optimal experience anyway with this lib...
        // IDEA: square the volume fraction to focus on closest speakers
        // IDEA: pick the x closest ones and devide evenly according to distance
        // TODO: integrate distance -> setting for (certain) algorithms.
        // TODO: integrate multiplier.
        // TODO: split out generic stuff into interface or MathHelper or so.
        double minAngle = DoubleStreamEx.of(speakerAngles.values()).min().getAsDouble();
        double maxAngle = DoubleStreamEx.of(speakerAngles.values()).max().getAsDouble();
        return new SpeakerVolumes(EntryStream.of(speakerAngles)
                .mapValues(angle -> this.calculateVolumeFraction(minAngle, maxAngle, angle))
                .toMap());
    }

}
