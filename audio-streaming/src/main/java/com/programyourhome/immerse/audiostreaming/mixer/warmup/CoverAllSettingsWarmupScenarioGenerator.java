package com.programyourhome.immerse.audiostreaming.mixer.warmup;

import static com.programyourhome.immerse.toolbox.audio.playback.LoopPlayback.times;
import static com.programyourhome.immerse.toolbox.audio.playback.TimerPlayback.timer;
import static com.programyourhome.immerse.toolbox.audio.resource.SuppliedAudioResource.supplied;
import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.location.dynamic.KeyFramesDynamicLocation.keyFrames;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.MaxSumNormalizeAlgorithm.maxSum;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FieldOfHearingVolumeRatiosAlgorithm.fieldOfHearing;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FixedVolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.OnlyClosestVolumeRatiosAlgorithm.onlyClosest;
import static com.programyourhome.immerse.toolbox.util.TestData.scenario;
import static com.programyourhome.immerse.toolbox.util.TestData.settings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.audiostreaming.generate.SineWaveAudioInputStreamGenerator;
import com.programyourhome.immerse.audiostreaming.mixer.ImmerseMixer;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.format.ByteOrder;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.RecordingMode;
import com.programyourhome.immerse.domain.format.SampleRate;
import com.programyourhome.immerse.domain.format.SampleSize;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

/**
 * Scenario warmup generator that combines all possible different scenario settings.
 */
public class CoverAllSettingsWarmupScenarioGenerator implements WarmupScenarioGenerator {

    private static final long LENGTH_IN_MILLIS = 100;
    private static final int FREQUENCY = 4000;
    private static final int REPETITIONS = 5;

    @Override
    public Map<Scenario, Long> generateWarmupScenarios(ImmerseMixer mixer) {
        Map<Scenario, Long> warmupScenarios = new HashMap<>();

        // Get the first 2 speakers from the room (should always have at least 2) and use those
        // to define a position in between and a very basic key frames dynamic location.
        Room room = mixer.getSettings().getRoom();
        Iterator<Speaker> speakerIter = room.getSpeakers().values().iterator();
        Speaker speaker1 = speakerIter.next();
        Speaker speaker2 = speakerIter.next();
        Vector3D betweenTwoSpakers = Vector3D.fromLa4j(speaker1.getPosition().toLa4j().add(speaker2.getPosition().toLa4j()).divide(2));

        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, speaker1.getPosition());
        keyFrames.put(LENGTH_IN_MILLIS, speaker2.getPosition());

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                mixer.getSettings().getRoom().getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));

        // Use various random combinations of settings, where each option of each setting is used at least once and with (almost) equal ratio.
        warmupScenarios.put(scenario(settings(supplied(() -> this.generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_8K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fieldOfHearing(room, fixed(betweenTwoSpakers), fixed(0, 0, 0)), fractional(), times(REPETITIONS))), LENGTH_IN_MILLIS * REPETITIONS);
        warmupScenarios.put(scenario(settings(supplied(() -> this.generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_11K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(fixedSpeakerVolumeRatios), fractional(), times(REPETITIONS))), LENGTH_IN_MILLIS * REPETITIONS);
        warmupScenarios.put(scenario(settings(supplied(() -> this.generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_16K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                onlyClosest(room, fixed(betweenTwoSpakers), fixed(0, 0, 0)), maxSum(1), timer(LENGTH_IN_MILLIS * REPETITIONS))),
                LENGTH_IN_MILLIS * REPETITIONS);
        warmupScenarios.put(scenario(settings(supplied(() -> this.generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_22K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fieldOfHearing(room, keyFrames(keyFrames), fixed(0, 0, 0)), maxSum(1), timer(LENGTH_IN_MILLIS * REPETITIONS))), LENGTH_IN_MILLIS * REPETITIONS);
        warmupScenarios.put(scenario(settings(supplied(() -> this.generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_32K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(fixedSpeakerVolumeRatios), maxSum(1), times(REPETITIONS))), LENGTH_IN_MILLIS * REPETITIONS);
        warmupScenarios.put(scenario(settings(supplied(() -> this.generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_44K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                onlyClosest(room, keyFrames(keyFrames), fixed(0, 0, 0)), fractional(), times(REPETITIONS))), LENGTH_IN_MILLIS * REPETITIONS);
        warmupScenarios.put(scenario(settings(supplied(() -> this.generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_48K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fieldOfHearing(room, fixed(betweenTwoSpakers), fixed(0, 0, 0)), maxSum(1), timer(LENGTH_IN_MILLIS * REPETITIONS))),
                LENGTH_IN_MILLIS * REPETITIONS);

        return warmupScenarios;
    }

    private AudioInputStream generate(ImmerseAudioFormat format, int frequency, long lengthInMillis) {
        return new SineWaveAudioInputStreamGenerator(format, frequency, lengthInMillis).generate();
    }

    private ImmerseAudioFormat format(RecordingMode recordingMode, SampleRate sampleRate, SampleSize sampleSize, boolean signed, ByteOrder byteOrder) {
        return ImmerseAudioFormat.builder()
                .recordingMode(recordingMode)
                .sampleRate(sampleRate)
                .sampleSize(sampleSize)
                .setSigned(signed)
                .byteOrder(byteOrder)
                .buildForInput();
    }

}
