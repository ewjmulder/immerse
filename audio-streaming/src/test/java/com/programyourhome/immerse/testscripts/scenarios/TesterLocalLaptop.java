package com.programyourhome.immerse.testscripts.scenarios;

import static com.programyourhome.immerse.toolbox.audio.playback.ForeverPlayback.forever;
import static com.programyourhome.immerse.toolbox.audio.resource.FixedAudioResource.fixed;
import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FixedVolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.toolbox.util.TestData.room;
import static com.programyourhome.immerse.toolbox.util.TestData.scenario;
import static com.programyourhome.immerse.toolbox.util.TestData.settings;
import static com.programyourhome.immerse.toolbox.util.TestData.soundCard;
import static com.programyourhome.immerse.toolbox.util.TestData.speaker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.RecordingMode;
import com.programyourhome.immerse.audiostreaming.format.SampleRate;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.audiostreaming.generate.SineWaveAudioInputStreamGenerator;
import com.programyourhome.immerse.audiostreaming.mixer.ImmerseMixer;
import com.programyourhome.immerse.audiostreaming.mixer.scenario.ScenarioPlaybackListener;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public class TesterLocalLaptop {

    private static final String CHILL = "/home/emulder/Downloads/ChillingMusic.wav";
    private static final String BASS = "/home/emulder/Downloads/doublebass.wav";
    private static final String CLAPPING = "/home/emulder/Downloads/clapping.wav";
    private static final String VOICE = "/home/emulder/Downloads/voice.wav";

    public static void main(String[] args) throws Exception {
        Speaker speaker1 = speaker(1, 0, 10, 10);
        Speaker speaker2 = speaker(2, 10, 10, 10);
        Room room = room(speaker1, speaker2);

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                room.getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));
        ImmerseAudioFormat format = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.ONE_BYTE)
                .recordingMode(RecordingMode.MONO)
                .signed()
                .buildForInput();
        Scenario scenario = scenario(room, settings(fixed(generate(format, 500, 10_000)), fixed(5, 10, 10), fixed(5, 5, 5),
                // Scenario scenario = scenario(room, settings(file(new File(CHILL)), fixed(5, 10, 10), fixed(5, 5, 5),
                fixed(fixedSpeakerVolumeRatios), fractional(), forever()));

        SoundCard soundCard1 = soundCard(1, "pci-0000:00:1f.3", speaker1, speaker2);

        ImmerseAudioFormat outputFormat = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .buildForOutput();

        ImmerseMixer mixer = new ImmerseMixer(room, new HashSet<>(Arrays.asList(soundCard1)), outputFormat);

        mixer.addPlaybackListener(new ScenarioPlaybackListener() {
            @Override
            public void scenarioStarted(UUID playbackId) {
                System.out.println("Scenario started: " + playbackId);
            }

            @Override
            public void scenarioRestarted(UUID playbackId) {
                System.out.println("Scenario restarted: " + playbackId);
            }

            @Override
            public void scenarioStopped(UUID playbackId) {
                System.out.println("Scenario stopped: " + playbackId);
            }
        });
        mixer.addStateListener((fromState, toState) -> System.out.println("State change from " + fromState + " to " + toState));

        mixer.initialize();
        mixer.start();

        for (int i = 0; i < 1; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}

            mixer.playScenario(scenario);
        }
    }

    private static AudioInputStream generate(ImmerseAudioFormat format, int frequency, long lengthInMillis) {
        return new SineWaveAudioInputStreamGenerator(format, frequency, lengthInMillis).generate();
    }

}
