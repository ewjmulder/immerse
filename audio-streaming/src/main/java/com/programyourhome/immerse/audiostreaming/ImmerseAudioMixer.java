package com.programyourhome.immerse.audiostreaming;

import static com.programyourhome.immerse.audiostreaming.AudioUtil.toSigned;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.RecordingMode;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

import one.util.streamex.EntryStream;

public class ImmerseAudioMixer {

    // TODO: make configurable, for instance for refresh rate setting
    private static final int SLEEP_MILLIS = 5;

    private final Room room;
    private final Set<SoundCard> soundCards;
    private final Set<SoundCardStream> soundCardStreams;
    private final ImmerseAudioFormat outputFormat;
    private final ImmerseAudioFormat inputFormat;
    private final Set<ActiveScenario> activeScenarios;
    private final SoundCardDetector soundCardDetector;
    private final Thread workerThread;
    private MixerState state;

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
        this.workerThread = new Thread(this::updateStreams);
        this.state = MixerState.NEW;
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

    public synchronized void initialize() throws IOException, LineUnavailableException {
        if (this.state != MixerState.NEW) {
            throw new IllegalStateException("Should be in state NEW to initialize");
        }
        this.soundCardDetector.detectSoundCards();
        this.buildStreams(this.soundCards);
        this.updateState(MixerState.INITIALIZED);
    }

    private void buildStreams(Set<SoundCard> soundCards) throws LineUnavailableException {
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

    public synchronized void start() {
        this.workerThread.start();
    }

    // TODO: naming of input / output / buffers / samples / amplitude etc.
    private void updateStreams() {
        while (this.state.isRunning()) {
            this.doUpdate();
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {}
        }
        this.soundCardStreams.forEach(SoundCardStream::stop);
        this.updateState(MixerState.STOPPED);
    }

    private synchronized void doUpdate() {
        long start = System.nanoTime();

        // Gather all data to write.
        ImmerseAudioLoop audioLoop = new ImmerseAudioLoop(this.activeScenarios, this.soundCardStreams, this.outputFormat);
        Map<SoundCardStream, byte[]> dataToWrite = audioLoop.getDataToWrite();

        // Signal start just before adding the audio data to the buffer.
        this.activeScenarios.forEach(ActiveScenario::startIfNotStarted);

        // Actually write to the sound card
        EntryStream.of(dataToWrite).forKeyValue((soundCardStream, data) -> soundCardStream.writeToLine(data));

        // Start the streams after initial buffer fill, to be in sync as much as possible.
        if (this.state == MixerState.INITIALIZED) {
            this.soundCardStreams.forEach(SoundCardStream::start);
            this.updateState(MixerState.STARTED);
        }

        this.handleScenarioLifecycle(audioLoop);

        long end = System.nanoTime();
        // System.out.println("Millis for loop: " + (end - start) / 1_000_000.0);
    }

    private void handleScenarioLifecycle(ImmerseAudioLoop audioLoop) {
        for (ActiveScenario activeScenario : audioLoop.getScenariosToRestart()) {
            try {
                this.activeScenarios.remove(activeScenario);
                this.playScenario(activeScenario.getScenario());
            } catch (IOException e) {
                e.printStackTrace();
                audioLoop.getScenariosToRemove().add(activeScenario);
            }
        }
        this.activeScenarios.removeAll(audioLoop.getScenariosToRemove());
    }

    private synchronized void updateState(MixerState newState) {
        if (!this.state.isAllowedNextState(newState)) {
            throw new IllegalStateException("Invalid state transition from: " + this.state + " to " + newState);
        }
        this.state = newState;
    }

    public synchronized void playScenario(Scenario scenario) throws IOException {
        if (!this.state.isRunning()) {
            throw new IllegalStateException("Mixer is not in a running state (" + this.state + ")");
        }
        if (this.room != scenario.getRoom()) {
            throw new IllegalArgumentException("The room object should be identical. This mixer is configured for room: " + this.room.getName() + ", "
                    + "while the provided scenario is for room: " + scenario.getRoom().getName());
        }
        AudioInputStream originalStream = scenario.getAudioResource().getAudioStream();
        // Workaround for a JDK bug: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8146338
        // TODO: write more information about the bug: asymmetric unsigned byte --> signed float --> signed byte conversion
        AudioInputStream signedStream = AudioUtil.convert(originalStream, toSigned(originalStream.getFormat()));
        AudioInputStream converted = AudioUtil.convert(signedStream, this.inputFormat.toJavaAudioFormat());
        this.activeScenarios.add(new ActiveScenario(scenario, scenario.getSettings().getPlaybackSupplier().get(), converted));
    }

    public void stop() {
        this.updateState(MixerState.STOPPING);
        try {
            this.workerThread.join();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Stopping interrupted", e);
        }
    }

}
