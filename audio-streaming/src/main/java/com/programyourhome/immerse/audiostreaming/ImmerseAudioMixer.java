package com.programyourhome.immerse.audiostreaming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.RecordingMode;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

// TODO: implement support for playing multiple audio streams at the same time
// TODO: useful to split into NonInteractiveScenarioPlayer and InteractiveScenarioPlayer?
// But: player should always be ready to accept a new audio stream 'into the loop'
// IDEA: always keep the loop running, and newly played audio starts buffer_millis after the play command
// Optimization: do not actually provide all 0's to the hardware (so do not 'keep the engine running' when there is no music to play).
// Really nice idea, only big problem is how to keep all the soundcards in sync?
// Only real life test can tell, either:
// A. as long as all buffers are big enough, syncing is actually quite good and no manual sync needed
// B. streams go out of sync very slowly and with some small trickery this can be kept within acceptable range manually
// C. streams are quickly out of sync and are very hard to keep in sync manually

public class ImmerseAudioMixer {

    // TODO: make dynamic based on hardware tests. Although 30 seems like a reasonable default.
    private static final int BUFFER_MILLIS = 30;
    // TODO: make configurable, for instance for refresh rate setting
    private static final int SLEEP_MILLIS = 5;

    private final Room room;
    private final Set<SoundCard> soundCards;
    private final Set<SoundCardStream> soundCardStreams;
    private final ImmerseAudioFormat outputFormat;
    private final ImmerseAudioFormat inputFormat;
    private final Set<ActiveScenario> activeScenarios;
    private final SoundCardDetector soundCardDetector;
    // TODO: move these state vars into some wrapper + enum of state? (new, initialized, started, stopping, stopped) + checks on right state before action
    // TODO: cannot have active scenario's before started?
    private final Thread updateThread;
    private boolean streamsHaveStarted;
    private boolean shouldStop;

    // TODO: room validation?
    // General remark: domain data does not have much validation itself, you can create whatever you like
    // (maybe except audio format, so maybe that should actually be moved to audio streaming!)
    // When starting a stream / mixer in audio-streaming, the validation takes place on the domain objects.
    public ImmerseAudioMixer(Room room, Set<SoundCard> soundCards, ImmerseAudioFormat outputAudioFormat) {
        if (soundCards.isEmpty()) {
            throw new IllegalArgumentException("You should provide at least one sound card");
        }
        if (!outputAudioFormat.isOutput()) {
            throw new IllegalArgumentException("The provided output audio format should be marked as output");
        }
        this.room = room;
        this.soundCards = soundCards;
        this.soundCardStreams = new HashSet<>();
        this.outputFormat = outputAudioFormat;
        this.inputFormat = this.createInputFormat();
        this.activeScenarios = new HashSet<>();
        this.soundCardDetector = new SoundCardDetector();
        this.updateThread = new Thread(this::updateStreams);
        this.streamsHaveStarted = false;
        this.shouldStop = false;
    }

    private ImmerseAudioFormat createInputFormat() {
        // Input format based on output format sample settings. Just switch from stereo to mono,
        // because an input stream should always consist of 1 channel that will be mixed dynamically.
        return ImmerseAudioFormat.builder()
                .recordingMode(RecordingMode.MONO)
                .sampleRate(this.outputFormat.getSampleRate())
                .sampleSize(this.outputFormat.getSampleSize())
                .setSigned(this.outputFormat.isSigned())
                .byteOrder(this.outputFormat.getByteOrder())
                .buildForInput();
    }

    public void initialize() throws IOException, LineUnavailableException {
        this.soundCardDetector.detectSoundCards();
        this.buildStreams(this.soundCards);
    }

    private synchronized void buildStreams(Set<SoundCard> soundCards) throws LineUnavailableException {
        for (SoundCard soundCard : soundCards) {
            SourceDataLine outputLine;
            Mixer.Info mixerInfo = this.soundCardDetector.getMixerInfo(soundCard.getPhysicalPort());
            if (mixerInfo == null) {
                throw new IllegalArgumentException("No mixer found for soundcard: " + soundCard);
            }
            try {
                outputLine = AudioSystem.getSourceDataLine(this.outputFormat.toJavaAudioFormat(), mixerInfo);
                System.out.println("NOT an exception for systemMixerInfo: " + mixerInfo);
            } catch (IllegalArgumentException e) {
                System.out.println("Exception for systemMixerInfo: " + mixerInfo);
                // TODO: some way to check this assumption?
                // In case an exception is thrown for this mixer, it's probably used as the default,
                // so use that one by setting the mixer info to 'null' (Java Sound API limitation?).
                outputLine = AudioSystem.getSourceDataLine(this.outputFormat.toJavaAudioFormat(), null);
            }
            outputLine.open();
            SoundCardStream soundCardStream = new SoundCardStream(soundCard, outputLine);
            this.soundCardStreams.add(soundCardStream);
        }
    }

    public void start() {
        this.updateThread.start();
    }

    // TODO: every time check playback shouldstop
    // TODO: make into nice Java 8 streams pipeline with helper methods
    // TODO: naming of input / output / buffers / samples / amplitude etc.
    private void updateStreams() {
        while (!this.shouldStop) {
            long start = System.nanoTime();
            // TODO: split out in synchronized method(s)
            synchronized (this) {
                int amountOfFramesNeeded = this.calculateAmountOfFramesNeeded();
                ScenarioLoopContext context = new ScenarioLoopContext(amountOfFramesNeeded);

                Map<SoundCardStream, byte[]> dataToWrite = this.activeScenarios.isEmpty() ? this.fillEmptyStream(context) : this.fillAudioBuffer(context);

                // Signal start just before adding the audio data to the buffer.
                this.activeScenarios.forEach(ActiveScenario::startIfNotStarted);

                // Actually write to the sound card
                EntryStream.of(dataToWrite).forKeyValue((soundCardStream, data) -> soundCardStream.writeToLine(data));

                // Start the streams after initial buffer fill, to be in sync as much as possible.
                if (!this.streamsHaveStarted) {
                    this.soundCardStreams.forEach(SoundCardStream::start);
                    this.streamsHaveStarted = true;
                }

                for (ActiveScenario activeScenario : context.getScenariosToRestart()) {
                    try {
                        this.activeScenarios.remove(activeScenario);
                        this.playScenario(activeScenario.getScenario());
                    } catch (IOException e) {
                        // Log error
                        context.getScenariosToRemove().add(activeScenario);
                    }
                }
                this.activeScenarios.removeAll(context.getScenariosToRemove());
            }

            long end = System.nanoTime();
            // System.out.println("Millis for loop: " + (end - start) / 1_000_000.0);
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {}
        }
        this.soundCardStreams.forEach(SoundCardStream::stop);
    }

    private Map<SoundCardStream, byte[]> fillAudioBuffer(ScenarioLoopContext context) {
        List<SpeakerData> speakerDatasInput = StreamEx.of(this.activeScenarios)
                .mapToEntry(activeScenario -> this.readFromInputStreams(activeScenario, context))
                .flatMapValues(this::optionalToStream)
                .mapKeys(this::getSpeakerVolumes)
                .mapKeyValue(SpeakerData::new)
                .toList();

        return StreamEx.of(this.soundCardStreams)
                // TODO: can this be done nicer?
                .mapToEntry(s -> speakerDatasInput)
                .mapToValue((soundCardStream, speakerDatas) -> StreamEx.of(speakerDatas)
                        .map(speakerData -> this.calculateSoundCardSamples(soundCardStream, speakerData.speakerVolumes, speakerData.samples))
                        .toList())
                .mapValues(this::mergeAmplitudes)
                .mapToValue((soundCardStream, samples) -> this.writeAmplitudes(context, soundCardStream, samples))
                .toMap();
    }

    // private void fillAudioBuffer(ScenarioLoopContext context) {
    // // TODO: use streamex mapValues and make helper methods for just one sound card stream
    // // Then we don't need any of the intermediate maps anymore!
    // // TODO: if parallel streams, use thread safe collections in context! (but prob not worth parallel)
    // Map<ActiveScenario, short[]> inputSamples = this.readFromInputStreams(context);
    // Map<SoundCardStream, List<short[]>> streamAmplitudesLists = this.processVolumeFractions(inputSamples);
    // Map<SoundCardStream, short[]> streamAmplitudes = this.mergeAmplitudes(context, streamAmplitudesLists);
    // this.writeAmplitudes(context, streamAmplitudes);
    // }

    private byte[] writeAmplitudes(ScenarioLoopContext context, SoundCardStream soundCardStream, short[] streamAmplitudes) {
        byte[] mergedOutputBuffer = new byte[context.getAmountOfFramesNeeded() * this.outputFormat.getNumberOfBytesPerFrame()];
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
            // if (sampleIndex % 2 == 0)
            // System.out.println(sanitizedAmplitude);
        }
        return amplitudes;
    }

    private Map<SoundCardStream, List<short[]>> processVolumeFractions(Map<ActiveScenario, short[]> inputSamples) {
        Map<SoundCardStream, List<short[]>> soundCardOutputBuffers = new HashMap<>();
        for (ActiveScenario activeScenario : inputSamples.keySet()) {
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
            SpeakerVolumes speakerVolumes = new SpeakerVolumes(snapshot);

            short[] sampleBuffer = inputSamples.get(activeScenario);
            for (SoundCardStream soundCardStream : this.soundCardStreams) {
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
                soundCardOutputBuffers.computeIfAbsent(soundCardStream, s -> new ArrayList<>()).add(stereoSamples);
            }
        }
        return soundCardOutputBuffers;
    }

    public static void main(String[] args) {
        List<?> l = StreamEx.of(1, 2, 3)
                .mapToEntry(i -> i % 2 == 0 ? Optional.empty() : Optional.of(i))
                .flatMapValues(op -> op.isPresent() ? StreamEx.of(op.get()) : StreamEx.empty())
                .toList();
        System.out.println(l);

        List<?> l2 = StreamEx.of(1, 2, 3)
                .map(i -> i % 2 == 0 ? Optional.empty() : Optional.of(i))
                .flatMap(op -> op.isPresent() ? StreamEx.of(op.get()) : StreamEx.empty())
                .toList();
        System.out.println(l2);
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
        return new SpeakerVolumes(snapshot);
    }

    private Optional<short[]> readFromInputStreams(ActiveScenario activeScenario, ScenarioLoopContext context) {
        try {
            short[] samples = new short[context.getAmountOfFramesNeeded()];
            boolean endOfStream = SampleReader.readSamples(activeScenario.getInputStream(), samples);
            if (endOfStream) {
                // End of stream reached, check playback for next action.
                if (activeScenario.getPlayback().endOfStream()) {
                    context.addScenarioToRestart(activeScenario);
                } else {
                    // No more playback, remove scenario.
                    context.addScenarioToRemove(activeScenario);
                }
            }
            return Optional.of(samples);
        } catch (IOException e) {
            // TODO: log error
            context.addScenarioToRemove(activeScenario);
            return Optional.empty();
        }
    }

    private Map<SoundCardStream, byte[]> fillEmptyStream(ScenarioLoopContext context) {
        int arraySize = context.getAmountOfFramesNeeded() * this.outputFormat.getNumberOfBytesPerFrame();
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

    public void playScenario(Scenario scenario) throws IOException {
        if (this.room != scenario.getRoom()) {
            throw new IllegalArgumentException("The room object should be identical. This mixer is configured for room: " + this.room.getName() + ", "
                    + "while the provided scenario is for room: " + scenario.getRoom().getName());
        }
        AudioInputStream originalStream = scenario.getAudioResource().getAudioStream();
        AudioFormat originalFormat = originalStream.getFormat();
        if (!AudioSystem.isConversionSupported(this.inputFormat.toJavaAudioFormat(), originalFormat)) {
            throw new IllegalArgumentException("Conversion of audio resource to desired input format is not supported");
        }
        AudioInputStream converted = AudioSystem.getAudioInputStream(this.inputFormat.toJavaAudioFormat(), originalStream);
        synchronized (this) {
            this.activeScenarios.add(new ActiveScenario(scenario, scenario.getSettings().getPlaybackSupplier().get(), converted));
        }
    }

    public void stop() {
        this.shouldStop = true;
        try {
            this.updateThread.join();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Stopping interrupted", e);
        }
    }

}
