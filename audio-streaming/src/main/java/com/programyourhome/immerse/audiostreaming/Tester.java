package com.programyourhome.immerse.audiostreaming;

import static com.programyourhome.immerse.domain.audio.playback.ForeverPlayback.forever;
import static com.programyourhome.immerse.domain.audio.resource.FileAudioResource.filePath;
import static com.programyourhome.immerse.domain.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.domain.location.dynamic.KeyFramesDynamicLocation.keyFrames;
import static com.programyourhome.immerse.domain.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.domain.speakers.algorithms.normalize.MaxSumNormalizeAlgorithm.maxSum;
import static com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.FieldOfHearingVolumeRatiosAlgorithm.fieldOfHearing;
import static com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.FixedVolumeRatiosAlgorithm.fixed;
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
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public class Tester {

    private static final String CHILL = "/home/emulder/Downloads/ChillingMusic.wav";
    private static final String BASS = "/home/emulder/Downloads/doublebass.wav";
    // TODO: find out why clapping has a tick but clapping-saved (load+save by Audacity) does not.
    private static final String CLAPPING = "/home/emulder/Downloads/clapping.wav";
    private static final String VOICE = "/home/emulder/Downloads/voice.wav";

    public static void main(String[] args) throws Exception {
        Speaker speaker1 = speaker(1, 0, 10, 10);
        Speaker speaker2 = speaker(2, 10, 10, 10);

        // Room room = room(speaker1, speaker2);

        Speaker speaker3 = speaker(3, 10, 0, 10);
        Speaker speaker4 = speaker(4, 0, 0, 10);
        Speaker speaker5 = speaker(3, 10, 0, 10);
        Speaker speaker6 = speaker(4, 0, 0, 10);
        Room room = room(speaker1, speaker2, speaker3, speaker4, speaker5, speaker6);

        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, new Vector3D(0, 10, 10));
        keyFrames.put(6_000L, new Vector3D(10, 10, 10));
        keyFrames.put(10_000L, new Vector3D(10, 0, 10));
        keyFrames.put(12_000L, new Vector3D(0, 0, 10));

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));
        Scenario scenario1 = scenario(room, settings(filePath(CHILL), keyFrames(keyFrames), fixed(5, 5, 5),
                fieldOfHearing(60), maxSum(1), forever()));

        Scenario scenario2 = scenario(room, settings(filePath(CHILL), fixed(0, 0, 0), fixed(5, 5, 5),
                fixed(fixedSpeakerVolumeRatios), fractional(), forever()));

        SoundCard soundCard1 = soundCard(1, "pci-0000:00:14.0-usb-0:1.2:1.0", speaker1, speaker2);
        SoundCard soundCard2 = soundCard(2, "pci-0000:00:14.0-usb-0:1.3:1.0", speaker3, speaker4);
        SoundCard soundCard3 = soundCard(3, "pci-0000:00:14.0-usb-0:1.4:1.0", speaker5, speaker6);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        ImmerseAudioMixer mixer = new ImmerseAudioMixer(room, new HashSet<>(Arrays.asList(soundCard1, soundCard2, soundCard3)), outputFormat);

        mixer.addPlaybackListener(new ScenarioPlaybackListener() {
            @Override
            public void scenarioStarted(Scenario scenario) {
                System.out.println("Scenario started: " + scenario.getName());
            }

            @Override
            public void scenarioRestarted(Scenario scenario) {
                System.out.println("Scenario restarted: " + scenario.getName());
            }

            @Override
            public void scenarioStopped(Scenario scenario) {
                System.out.println("Scenario stopped: " + scenario.getName());
            }
        });
        mixer.addStateListener((fromState, toState) -> System.out.println("State change from " + fromState + " to " + toState));

        mixer.initialize();
        mixer.start();

        for (int i = 0; i < 4; i++) {
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
