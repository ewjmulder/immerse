package com.programyourhome.immerse.toolbox.util;

import static com.programyourhome.immerse.toolbox.audio.playback.ForeverPlayback.forever;
import static com.programyourhome.immerse.toolbox.audio.resource.SilenceAudioResource.silence;
import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FieldOfHearingVolumeRatiosAlgorithm.fieldOfHearing;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.ScenarioSettings;
import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;
import com.programyourhome.immerse.domain.volume.DynamicVolume;
import com.programyourhome.immerse.toolbox.volume.dynamic.FixedDynamicVolume;

/**
 * Supplies a uniform and easy way to create some test data.
 * The basic idea is a room of 10x10x10 with all names and descriptions set to some default value.
 */
public class TestData {

    private TestData() {
    }

    public static Vector3D source(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    public static Vector3D listener(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    public static Speaker speaker(int id, double x, double y, double z) {
        return Speaker.builder()
                .id(id)
                .name("Speaker " + id)
                .description("Description of speaker " + id)
                .position(x, y, z)
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

    public static Scenario scenario(ScenarioSettings settings) {
        return Scenario.builder()
                .name("Scenario")
                .description("Description of scenario")
                .settings(settings)
                .build();
    }

    public static ScenarioSettings settings(Room room) {
        return settings(silence(), fieldOfHearing(room, fixed(0, 0, 0), fixed(0, 0, 0)), fractional(), forever());
    }

    public static ScenarioSettings settings(Factory<AudioResource> audioResource, Factory<VolumeRatiosAlgorithm> volumeRatiosAlgorithm,
            Factory<NormalizeAlgorithm> normalizeAlgorithm, Factory<Playback> playback) {
        return settings(audioResource, FixedDynamicVolume.full(), volumeRatiosAlgorithm, normalizeAlgorithm, playback);
    }

    public static ScenarioSettings settings(Factory<AudioResource> audioResource, Factory<DynamicVolume> volume,
            Factory<VolumeRatiosAlgorithm> volumeRatiosAlgorithm, Factory<NormalizeAlgorithm> normalizeAlgorithm, Factory<Playback> playback) {
        return ScenarioSettings.builder()
                .audioResource(audioResource)
                .volume(volume)
                .volumeRatiosAlgorithm(volumeRatiosAlgorithm)
                .normalizeAlgorithm(normalizeAlgorithm)
                .playback(playback)
                .build();
    }

    public static SoundCard soundCard(int id, String physicalPort, Speaker leftSpeaker, Speaker rightSpeaker) {
        return soundCard(id, physicalPort, leftSpeaker.getId(), rightSpeaker.getId());
    }

    public static SoundCard soundCard(int id, String physicalPort, int leftSpeakerId, int rightSpeakerId) {
        return SoundCard.builder()
                .id(id)
                .name("SoundCard " + id)
                .description("Description of sound card " + id)
                .physicalPort(physicalPort)
                .leftSpeakerId(leftSpeakerId)
                .rightSpeakerId(rightSpeakerId)
                .build();
    }

}
