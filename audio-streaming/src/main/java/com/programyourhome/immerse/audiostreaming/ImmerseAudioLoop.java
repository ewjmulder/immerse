package com.programyourhome.immerse.audiostreaming;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

import one.util.streamex.StreamEx;

// instance for one loop
public class ImmerseAudioLoop {

    // TODO: make dynamic based on hardware tests. Although 30 seems like a reasonable default. (20 is not enough and cause distortion!)
    private static final int BUFFER_MILLIS = 30;

    private final Set<ActiveScenario> activeScenarios;
    private final Set<SoundCardStream> soundCardStreams;
    private final ImmerseAudioFormat outputFormat;
    private final Set<ActiveScenario> scenariosToRemove;
    private final Set<ActiveScenario> scenariosToRestart;
    private final int amountOfFramesNeeded;

    public ImmerseAudioLoop(Set<ActiveScenario> activeScenarios, Set<SoundCardStream> soundCardStreams, ImmerseAudioFormat outputFormat) {
        this.activeScenarios = activeScenarios;
        this.soundCardStreams = soundCardStreams;
        this.outputFormat = outputFormat;
        this.scenariosToRemove = new HashSet<>();
        this.scenariosToRestart = new HashSet<>();
        this.amountOfFramesNeeded = this.calculateAmountOfFramesNeeded();
    }

    public Set<ActiveScenario> getScenariosToRemove() {
        return this.scenariosToRemove;
    }

    public Set<ActiveScenario> getScenariosToRestart() {
        return this.scenariosToRestart;
    }

    public Map<SoundCardStream, byte[]> getDataToWrite() {
        return this.activeScenarios.isEmpty() ? this.fillEmptyStream() : this.fillAudioBuffer();
    }

    private Map<SoundCardStream, byte[]> fillAudioBuffer() {
        StreamEx.of(this.activeScenarios)
                .filter(activeScenarino -> activeScenarino.getPlayback().shouldStop())
                .forEach(this.scenariosToRemove::add);

        List<SpeakerData> speakerDatasInput = StreamEx.of(this.activeScenarios)
                .mapToEntry(this::readFromInputStreams)
                .flatMapValues(this::optionalToStream)
                .mapKeys(this::getSpeakerVolumes)
                .mapKeyValue(SpeakerData::new)
                .toList();

        return StreamEx.of(this.soundCardStreams)
                // Every soundcard stream is coupled to the 'raw' input data.
                .mapToEntry(soundCardStream -> speakerDatasInput)
                .mapToValue(this::calculateAllSamples)
                .mapValues(this::mergeAmplitudes)
                .mapToValue(this::writeAmplitudes)
                .toMap();
    }

    private List<short[]> calculateAllSamples(SoundCardStream soundCardStream, List<SpeakerData> speakerDatas) {
        return StreamEx.of(speakerDatas)
                .map(speakerData -> this.calculateSoundCardSamples(soundCardStream, speakerData.speakerVolumes, speakerData.samples))
                .toList();
    }

    private byte[] writeAmplitudes(SoundCardStream soundCardStream, short[] streamAmplitudes) {
        byte[] mergedOutputBuffer = new byte[this.amountOfFramesNeeded * this.outputFormat.getNumberOfBytesPerFrame()];
        SampleWriter.writeSamples(streamAmplitudes, mergedOutputBuffer, this.outputFormat);
        return mergedOutputBuffer;
    }

    private short[] mergeAmplitudes(List<short[]> amplitudesLists) {
        short[] amplitudes = new short[amplitudesLists.get(0).length];
        for (int sampleIndex = 0; sampleIndex < amplitudes.length; sampleIndex++) {
            // Merging the buffers is just a matter of summing the amplitudes of the different sounds.
            int totalAmplitude = 0;
            for (short[] sampleBuffer : amplitudesLists) {
                totalAmplitude += sampleBuffer[sampleIndex];
            }
            short sanitizedAmplitude;
            // Keep amplitude within boundaries.
            if (this.outputFormat.getSampleSize() == SampleSize.ONE_BYTE) {
                sanitizedAmplitude = this.sanitizeAsByte(totalAmplitude);
            } else {
                sanitizedAmplitude = this.sanitizeAsShort(totalAmplitude);
            }
            amplitudes[sampleIndex] = sanitizedAmplitude;
        }
        return amplitudes;
    }

    // TODO: move to some kind of util
    private <T> StreamEx<T> optionalToStream(Optional<T> optional) {
        return optional.isPresent() ? StreamEx.of(optional.get()) : StreamEx.empty();
    }

    // TODO: nicer!!
    private class SpeakerData {
        SpeakerVolumes speakerVolumes;
        short[] samples;

        public SpeakerData(SpeakerVolumes speakerVolumes, short[] samples) {
            this.speakerVolumes = speakerVolumes;
            this.samples = samples;
        }
    }

    private short[] calculateSoundCardSamples(SoundCardStream soundCardStream, SpeakerVolumes speakerVolumes, short[] sampleBuffer) {
        double volumeFractionSpeakerLeft = speakerVolumes.getVolumeFraction(soundCardStream.getSoundCard().getLeftSpeaker().getId());
        double volumeFractionSpeakerRight = speakerVolumes.getVolumeFraction(soundCardStream.getSoundCard().getRightSpeaker().getId());
        int stereoSamplesSize = sampleBuffer.length * 2;
        short[] stereoSamples = new short[stereoSamplesSize];
        for (int sampleIndex = 0; sampleIndex < sampleBuffer.length; sampleIndex++) {
            short leftAmplitude = (short) (sampleBuffer[sampleIndex] * volumeFractionSpeakerLeft);
            short rightAmplitude = (short) (sampleBuffer[sampleIndex] * volumeFractionSpeakerRight);
            stereoSamples[sampleIndex * 2] = leftAmplitude;
            stereoSamples[sampleIndex * 2 + 1] = rightAmplitude;
        }
        return stereoSamples;
    }

    private SpeakerVolumes getSpeakerVolumes(ActiveScenario activeScenario) {
        long millisSinceStart = 0;
        if (activeScenario.isStarted()) {
            millisSinceStart = System.currentTimeMillis() - activeScenario.getStartMillis();
        }
        Vector3D listener = activeScenario.getScenario().getListenerLocation().getLocation(millisSinceStart);
        Vector3D source = activeScenario.getScenario().getSourceLocation().getLocation(millisSinceStart);
        Snapshot snapshot = Snapshot.builder()
                .scenario(activeScenario.getScenario())
                .source(source)
                .listener(listener)
                .build();
        SpeakerVolumeRatios speakerVolumeRatios = activeScenario.getScenario().getSettings().getVolumeRatiosAlgorithm().calculateVolumeRatios(snapshot);
        return activeScenario.getScenario().getSettings().getNormalizeAlgorithm().calculateVolumes(speakerVolumeRatios);
    }

    private Optional<short[]> readFromInputStreams(ActiveScenario activeScenario) {
        try {
            short[] samples = new short[this.amountOfFramesNeeded];
            boolean endOfStream = SampleReader.readSamples(activeScenario.getInputStream(), samples);
            if (endOfStream) {
                // End of stream reached, check playback for next action.
                if (activeScenario.getPlayback().endOfStream()) {
                    this.scenariosToRestart.add(activeScenario);
                } else {
                    // No more playback, remove scenario.
                    this.scenariosToRemove.add(activeScenario);
                }
            }
            return Optional.of(samples);
        } catch (IOException e) {
            // TODO: log error
            this.scenariosToRemove.add(activeScenario);
            return Optional.empty();
        }
    }

    private Map<SoundCardStream, byte[]> fillEmptyStream() {
        int arraySize = this.amountOfFramesNeeded * this.outputFormat.getNumberOfBytesPerFrame();
        byte[] silenceArray = new byte[arraySize];
        Arrays.fill(silenceArray, (byte) 0);
        return StreamEx.of(this.soundCardStreams)
                .mapToEntry(s -> silenceArray)
                .toMap();
    }

    private int calculateAmountOfFramesNeeded() {
        List<Long> allFramesNeededAmounts = this.soundCardStreams.stream()
                .mapToLong(stream -> stream.getAmountOfFramesNeeded(BUFFER_MILLIS))
                .boxed()
                .collect(Collectors.toList());
        long minFramesNeeded = Collections.min(allFramesNeededAmounts);
        long maxFramesNeeded = Collections.max(allFramesNeededAmounts);
        // Since we have to keep in sync with all streams, we take the max frames needed as the amount needed for all streams.
        long amountOfFramesNeeded = maxFramesNeeded;
        // We want an int for easier use as array size and it will never be bigger than the limits of Integer.
        return (int) amountOfFramesNeeded;
    }

    private byte sanitizeAsByte(int sample) {
        return (byte) Math.max(Math.min(sample, Byte.MAX_VALUE), Byte.MIN_VALUE);
    }

    private short sanitizeAsShort(int sample) {
        return (short) Math.max(Math.min(sample, Short.MAX_VALUE), Short.MIN_VALUE);
    }

}
