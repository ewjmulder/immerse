package com.programyourhome.immerse.audiostreaming;

import static com.programyourhome.immerse.audiostreaming.simulation.AudioInputStreamGenerator.generate;
import static com.programyourhome.immerse.domain.audio.playback.Playback.times;
import static com.programyourhome.immerse.domain.audio.resource.AudioResource.fromSupplier;
import static com.programyourhome.immerse.domain.location.dynamic.DynamicLocation.fixed;
import static com.programyourhome.immerse.domain.location.dynamic.DynamicLocation.keyFrames;
import static com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm.maxSum;
import static com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm.fieldOfHearing;
import static com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm.onlyClosest;
import static com.programyourhome.immerse.domain.util.TestData.scenario;
import static com.programyourhome.immerse.domain.util.TestData.settings;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.RecordingMode;
import com.programyourhome.immerse.audiostreaming.format.SampleRate;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public class MixerWarmup {

    private static final long LENGTH_IN_MILLIS = 100;
    private static final int FREQUENCY = 4000;
    private static final int REPETITIONS = 5;

    public List<Scenario> getWarmupScenarios(ImmerseAudioMixer mixer) {
        List<Scenario> warmupScenarios = new ArrayList<>();

        Room room = mixer.getRoom();
        Iterator<Speaker> speakerIter = room.getSpeakers().values().iterator();
        Speaker speaker1 = speakerIter.next();
        Speaker speaker2 = speakerIter.next();
        Vector3D betweenTwoSpakers = Vector3D.fromLa4j(speaker1.getVectorLa4j().add(speaker2.getVectorLa4j()).divide(2));

        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, speaker1.getVector3D());
        keyFrames.put(LENGTH_IN_MILLIS, speaker2.getVector3D());

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                mixer.getRoom().getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));

        // TODO: add different AudioResources, file and maybe URL if that can be done with local resource (resolves to local file, no network needed)
        // TODO: vary the recording mode, sample size, singed-ness (currently not supported by the generator)
        warmupScenarios.add(scenario(room, fromSupplier(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_8K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), settings(fieldOfHearing(), fractional(), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, fromSupplier(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_11K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                keyFrames(keyFrames), fixed(0, 0, 0), settings(fixed(fixedSpeakerVolumeRatios), fractional(), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, fromSupplier(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_16K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), settings(onlyClosest(), maxSum(1), Playback.timer(LENGTH_IN_MILLIS * REPETITIONS))));
        warmupScenarios.add(scenario(room, fromSupplier(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_22K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                keyFrames(keyFrames), fixed(0, 0, 0), settings(fieldOfHearing(), maxSum(1), Playback.timer(LENGTH_IN_MILLIS * REPETITIONS))));
        warmupScenarios.add(scenario(room, fromSupplier(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_32K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), settings(fixed(fixedSpeakerVolumeRatios), maxSum(1), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, fromSupplier(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_44K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                keyFrames(keyFrames), fixed(0, 0, 0), settings(onlyClosest(), fractional(), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, fromSupplier(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_48K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), settings(fieldOfHearing(), maxSum(1), Playback.timer(LENGTH_IN_MILLIS * REPETITIONS))));

        return warmupScenarios;
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
