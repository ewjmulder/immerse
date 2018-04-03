package com.programyourhome.immerse.audiostreaming;

import static com.programyourhome.immerse.domain.audio.resource.AudioResource.fromFilePath;
import static com.programyourhome.immerse.domain.location.dynamic.DynamicLocation.fixed;
import static com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.domain.util.TestData.room;
import static com.programyourhome.immerse.domain.util.TestData.scenario;
import static com.programyourhome.immerse.domain.util.TestData.settings;
import static com.programyourhome.immerse.domain.util.TestData.soundCard;
import static com.programyourhome.immerse.domain.util.TestData.speaker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.SampleRate;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

public class Tester {

    private static final String CHILL = "/home/emulder/Downloads/ChillingMusic.wav";
    private static final String BASS = "/home/emulder/Downloads/doublebass.wav";
    // TODO: find out why clapping has a tick but clapping-saved (load+save by Audacity) does not.
    private static final String CLAPPING = "/home/emulder/Downloads/clapping.wav";
    private static final String VOICE = "/home/emulder/Downloads/voice.wav";

    public static void main(String[] args) throws Exception {
        Speaker speaker1 = speaker(1, 0, 10, 10);
        Speaker speaker2 = speaker(2, 10, 10, 10);

        Room room = room(speaker1, speaker2);

        // Speaker speaker3 = speaker(3, 10, 0, 10);
        // Speaker speaker4 = speaker(4, 0, 0, 10);
        // Room room = room(speaker1, speaker2, speaker3, speaker4);

        // TODO: convenience class around key frames?
        // TODO: key frames options loop or once
        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, new Vector3D(0, 10, 10));
        keyFrames.put(6_000L, new Vector3D(10, 10, 10));
        keyFrames.put(10_000L, new Vector3D(10, 0, 10));
        keyFrames.put(12_000L, new Vector3D(0, 0, 10));

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));
        Scenario scenario1 = scenario(room, fromFilePath(CHILL), DynamicLocation.keyFrames(keyFrames), fixed(5, 5, 5),
                settings(VolumeRatiosAlgorithm.fieldOfHearing(60), NormalizeAlgorithm.maxSum(1), Playback.forever()));

        Scenario scenario2 = scenario(room, fromFilePath(CHILL), fixed(0, 0, 0), fixed(5, 5, 5),
                settings(fixed(fixedSpeakerVolumeRatios), fractional(), Playback.timer(10000)));

        SoundCard soundCard1 = soundCard(1, "pci-0000:00:14.0-usb-0:6:1.0", speaker1, speaker2);
        // SoundCard soundCard2 = soundCard(2, "pci-0000:00:14.0-usb-0:1:1.0", speaker3, speaker4);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        ImmerseAudioMixer mixer = new ImmerseAudioMixer(room, new HashSet<>(Arrays.asList(soundCard1)), outputFormat);
        // mixer.initialize();
        // mixer.start();

        mixer.playScenario(scenario2);

        // for (int i = 0; i < 4; i++) {
        // try {
        // Thread.sleep(500);
        // } catch (InterruptedException e) {}
        //
        // mixer.playScenario(scenario1);
        // }

        // for (int i = 0; i < 1; i++) {
        // try {
        // Thread.sleep(500);
        // } catch (InterruptedException e) {}
        //
        // mixer.playScenario(scenario2);
        // }

    }

}
