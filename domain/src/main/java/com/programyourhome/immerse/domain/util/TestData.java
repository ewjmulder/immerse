package com.programyourhome.immerse.domain.util;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.ImmerseSettings;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * Supplies a uniform and easy way to create some test data.
 * The basic idea is a room of 10x10x10 with all names and descriptions set to some default value.
 */
public class TestData {

    private static int nextSpeakerId = 1;

    private TestData() {
    }

    public static Vector3D source(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    public static Vector3D listener(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    public static Speaker speaker(double x, double y, double z) {
        return speaker(nextSpeakerId++, x, y, z);
    }

    public static Speaker speaker(int id, double x, double y, double z) {
        return Speaker.builder()
                .id(id)
                .name("Speaker " + id)
                .description("Description of speaker " + id)
                .vector(x, y, z)
                .build();
    }

    public static Room room(Speaker... speakers) {
        return Room.builder()
                .name("Room")
                .description("Description of room")
                .dimensions(10, 10, 10)
                .addSpeakers(speakers)
                .build();
    }

    public static Scenario scenario(Room room, AudioResource audioResource,
            DynamicLocation sourceLocation, DynamicLocation listenerLocation, ImmerseSettings settings) {
        return Scenario.builder()
                .name("Scenario")
                .description("Description of scenario")
                .room(room)
                .audioResource(audioResource)
                .sourceLocation(sourceLocation)
                .listenerLocation(listenerLocation)
                .settings(settings)
                .build();
    }

    /**
     * Just create a snapshot, without any interest in the dynamic part of the scenario.
     */
    public static Snapshot snapshot(Room room, Vector3D source, Vector3D listener, ImmerseSettings settings) {
        return Snapshot.builder()
                .scenario(scenario(room, null, null, null, settings))
                .source(source)
                .listener(listener)
                .build();
    }

    public static ImmerseSettings settings() {
        return ImmerseSettings.defaults();
    }

    public static ImmerseSettings settings(VolumeRatiosAlgorithm volumeRatiosAlgorithm,
            NormalizeAlgorithm normalizeAlgorithm, Supplier<Playback> playbackSupplier) {
        return ImmerseSettings.builder()
                .volumeRatiosAlgorithm(volumeRatiosAlgorithm)
                .normalizeAlgorithm(normalizeAlgorithm)
                .playback(playbackSupplier)
                .build();
    }

}
