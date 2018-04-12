package com.programyourhome.immerse.audiostreaming.mixer;

import static com.programyourhome.immerse.audiostreaming.util.AudioInputStreamGenerator.generate;
import static com.programyourhome.immerse.toolbox.audio.playback.LoopPlayback.times;
import static com.programyourhome.immerse.toolbox.audio.playback.TimerPlayback.timer;
import static com.programyourhome.immerse.toolbox.audio.resource.FixedAudioResource.fixed;
import static com.programyourhome.immerse.toolbox.location.dynamic.FixedDynamicLocation.fixed;
import static com.programyourhome.immerse.toolbox.location.dynamic.KeyFramesDynamicLocation.keyFrames;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.FractionalNormalizeAlgorithm.fractional;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.normalize.MaxSumNormalizeAlgorithm.maxSum;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FieldOfHearingVolumeRatiosAlgorithm.fieldOfHearing;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.FixedVolumeRatiosAlgorithm.fixed;
import static com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios.OnlyClosestVolumeRatiosAlgorithm.onlyClosest;
import static com.programyourhome.immerse.toolbox.util.TestData.scenario;
import static com.programyourhome.immerse.toolbox.util.TestData.settings;

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
        Vector3D betweenTwoSpakers = Vector3D.fromLa4j(speaker1.getPosition().toLa4j().add(speaker2.getPosition().toLa4j()).divide(2));

        SortedMap<Long, Vector3D> keyFrames = new TreeMap<>();
        keyFrames.put(0L, speaker1.getPosition());
        keyFrames.put(LENGTH_IN_MILLIS, speaker2.getPosition());

        SpeakerVolumeRatios fixedSpeakerVolumeRatios = new SpeakerVolumeRatios(
                mixer.getRoom().getSpeakers().values().stream().collect(Collectors.toMap(Speaker::getId, speaker -> 1.0)));

        warmupScenarios.add(scenario(room, settings(fixed(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_8K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), fieldOfHearing(), fractional(), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, settings(fixed(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_11K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                keyFrames(keyFrames), fixed(0, 0, 0), fixed(fixedSpeakerVolumeRatios), fractional(), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, settings(fixed(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_16K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), onlyClosest(), maxSum(1), timer(LENGTH_IN_MILLIS * REPETITIONS))));
        warmupScenarios.add(scenario(room, settings(fixed(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_22K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                keyFrames(keyFrames), fixed(0, 0, 0), fieldOfHearing(), maxSum(1), timer(LENGTH_IN_MILLIS * REPETITIONS))));
        warmupScenarios.add(scenario(room, settings(fixed(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_32K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), fixed(fixedSpeakerVolumeRatios), maxSum(1), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, settings(fixed(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_44K, SampleSize.ONE_BYTE, true, ByteOrder.BIG_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                keyFrames(keyFrames), fixed(0, 0, 0), onlyClosest(), fractional(), times(REPETITIONS))));
        warmupScenarios.add(scenario(room, settings(fixed(generate(
                this.format(RecordingMode.MONO, SampleRate.RATE_48K, SampleSize.ONE_BYTE, true, ByteOrder.LITTLE_ENDIAN), FREQUENCY, LENGTH_IN_MILLIS)),
                fixed(betweenTwoSpakers), fixed(0, 0, 0), fieldOfHearing(), maxSum(1), timer(LENGTH_IN_MILLIS * REPETITIONS))));

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
