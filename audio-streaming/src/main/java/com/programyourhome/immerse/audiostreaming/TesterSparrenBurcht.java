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

    private static final String CHILL_PINE = "/home/ubuntu/sandbox/ChillingMusic_Loud.wav";
    private static final String CLAPPING_PINE = "/home/ubuntu/sandbox/clapping-louder-long.wav";
    private static final String VOICE_PINE = "/home/ubuntu/sandbox/voice-12-sec.wav";
    private static final String CHILL = "/home/emulder/Downloads/ChillingMusic.wav";
    private static final String BASS = "/home/emulder/Downloads/doublebass.wav";
    // TODO: find out why clapping has a tick but clapping-saved (load+save by Audacity) does not.
    private static final String CLAPPING = "/home/emulder/Downloads/clapping.wav";
    private static final String VOICE = "/home/emulder/Downloads/voice.wav";

    public static void main(String[] args) throws Exception {
        Speaker speaker1  = speaker(1,  120, 0,   250);
        Speaker speaker2  = speaker(2,  240, 0,   250);
        Speaker speaker3  = speaker(3,  0,   0,   250);
        Speaker speaker4  = speaker(4,  0,   240, 250);
        Speaker speaker5  = speaker(5,  0,   365, 250);
        Speaker speaker6  = speaker(6,  0,   75,  250);
        Speaker speaker7  = speaker(7,  365, 0,   250);
        Speaker speaker8  = speaker(8,  365, 120, 250);
        Speaker speaker9  = speaker(9,  365, 240, 250);
        Speaker speaker10 = speaker(10, 365, 365, 250);
        Speaker speaker11 = speaker(11, 120, 365, 250);
        Speaker speaker12 = speaker(12, 240, 365, 250);
        Room room = room(speaker1, speaker2, speaker3, speaker4, speaker5, speaker6, speaker7, speaker8, speaker9, speaker10, speaker11, speaker12);

        // TODO: convenience class around key frames?
        // TODO: key frames options loop or once
        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, new Vector3D(0, 0, 250));
        keyFrames.put(3_000L, new Vector3D(0, 365, 250));
        keyFrames.put(6_000L, new Vector3D(365, 365, 250));
        keyFrames.put(9_000L, new Vector3D(365, 0, 250));
        keyFrames.put(12_000L, new Vector3D(0, 0, 250));

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0))); //speaker.getId() == 11 ? 1.0 : 0.0)));
        Scenario scenario1 = scenario(room, fromFilePath(CHILL_PINE), DynamicLocation.keyFrames(keyFrames), fixed(5, 5, 5),
                settings(VolumeRatiosAlgorithm.fieldOfHearing(60), NormalizeAlgorithm.maxSum(1), Playback.forever()));

        Scenario scenario2 = scenario(room, fromFilePath(CHILL_PINE), DynamicLocation.keyFrames(keyFrames), fixed(180, 180, 150),
//                settings(VolumeRatiosAlgorithm.fieldOfHearing(45), NormalizeAlgorithm.maxSum(1), Playback.forever()));
                settings(fixed(fixedSpeakerVolumeRatios), fractional(), Playback.forever()));

        SoundCard soundCard1 = soundCard(1, "platform-1c1a000.ehci0-controller-usb-0:1.2:1.0", speaker1, speaker2);
        SoundCard soundCard2 = soundCard(2, "platform-1c1a000.ehci0-controller-usb-0:1.3:1.0", speaker3, speaker4);
        SoundCard soundCard3 = soundCard(3, "platform-1c1a000.ehci0-controller-usb-0:1.4:1.0", speaker5, speaker6);
        SoundCard soundCard4 = soundCard(4, "platform-1c1a000.ehci0-controller-usb-0:1.1.2:1.0", speaker7, speaker8);
        SoundCard soundCard5 = soundCard(5, "platform-1c1a000.ehci0-controller-usb-0:1.1.3:1.0", speaker9, speaker10);
        SoundCard soundCard6 = soundCard(6, "platform-1c1a000.ehci0-controller-usb-0:1.1.4:1.0", speaker11, speaker12);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        ImmerseAudioMixer mixer = new ImmerseAudioMixer(room, new HashSet<>(Arrays.asList(soundCard1, soundCard2, soundCard3, soundCard4, soundCard5, soundCard6)), outputFormat);

        mixer.initialize();
        mixer.start();

        for (int i = 0; i < 1; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}

            mixer.playScenario(scenario2);
        }

        // for (int i = 0; i < 1; i++) {
        // try {
        // Thread.sleep(500);
        // } catch (InterruptedException e) {}
        //
        // mixer.playScenario(scenario2);
        // }

    }

}
