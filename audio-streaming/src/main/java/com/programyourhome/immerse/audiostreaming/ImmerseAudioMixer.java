package com.programyourhome.immerse.audiostreaming;

import static com.programyourhome.immerse.audiostreaming.AudioUtil.toSigned;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    // Note: setting this to less then 5 should not improve anything, because
    // the 'resolution' of sound cards providing their current frame is about 5 ms.
    // Note2: less then 5 makes sense when the update loop itself takes a considerable amount of time.
    // Maybe sleep 5 - the previous loop time?
    private static final int SLEEP_MILLIS = 5;

    private static final int WARMUP_SLEEP_MILLIS = 5;

    private final Set<MixerStateListener> stateListeners;
    private final Set<ScenarioPlaybackListener> playbackListeners;
    private final Room room;
    private final Set<SoundCard> soundCards;
    private final Set<SoundCardStream> soundCardStreams;
    private final ImmerseAudioFormat outputFormat;
    private final ImmerseAudioFormat inputFormat;
    private final Set<ActiveScenario> scenariosToActivate;
    private final Set<ActiveScenario> activeScenarios;
    private final SoundCardDetector soundCardDetector;
    private final Thread workerThread;
    private MixerState state;
    private boolean warmupMixer;

    public ImmerseAudioMixer(Room room, Set<SoundCard> soundCards, ImmerseAudioFormat outputAudioFormat) {
        if (room.getSpeakers().size() < 2) {
            throw new IllegalArgumentException("The room should have at least 2 speakers");
        }
        if (soundCards.isEmpty()) {
            throw new IllegalArgumentException("You should provide at least one sound card");
        }
        if (!outputAudioFormat.isOutput()) {
            throw new IllegalArgumentException("The provided output audio format should be marked as output");
        }
        this.stateListeners = new HashSet<>();
        this.playbackListeners = new HashSet<>();
        this.room = room;
        this.soundCards = soundCards;
        this.soundCardStreams = new HashSet<>();
        this.outputFormat = outputAudioFormat;
        this.inputFormat = this.createInputFormat();
        // Explicitly synchronize this set, because it is the only 'overlapping' part between the mixer internals and the 'outside' world.
        this.scenariosToActivate = Collections.synchronizedSet(new HashSet<>());
        this.activeScenarios = new HashSet<>();
        this.soundCardDetector = new SoundCardDetector();
        this.workerThread = new Thread(this::updateStreams);
        this.state = MixerState.NEW;
        this.warmupMixer = false;
    }

    public Room getRoom() {
        return this.room;
    }

    public void addStateListener(MixerStateListener listener) {
        this.stateListeners.add(listener);
    }

    public void removeStateListener(MixerStateListener listener) {
        this.stateListeners.remove(listener);
    }

    public void addPlaybackListener(ScenarioPlaybackListener listener) {
        this.playbackListeners.add(listener);
    }

    public void removePlaybackListener(ScenarioPlaybackListener listener) {
        this.playbackListeners.remove(listener);
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

    public void initialize() {
        if (this.state != MixerState.NEW) {
            throw new IllegalStateException("Should be in state NEW to initialize");
        }
        try {
            this.soundCardDetector.detectSoundCards();
            this.buildStreams(this.soundCards);
        } catch (IOException | LineUnavailableException e) {
            throw new IllegalStateException("Exception during initialization", e);
        }
        this.updateState(MixerState.WARMUP);
        if (this.warmupMixer) {
            this.soundCardStreams.forEach(SoundCardStream::mute);
            this.updateState(MixerState.INITIALIZED);
        } else {
            new Thread(() -> this.warmup()).start();
        }
    }

    private void warmup() {
        long start = System.nanoTime();

        ImmerseAudioMixer warmupMixer = new ImmerseAudioMixer(this.room, this.soundCards, this.outputFormat);
        warmupMixer.warmupMixer = true;

        Set<Scenario> scenariosInProgress = new HashSet<>();
        warmupMixer.addPlaybackListener(new ScenarioPlaybackListener() {
            @Override
            public void scenarioStarted(Scenario scenario) {
                System.out.println("WARMUP Scenario started: " + scenario.getName());
            }

            @Override
            public void scenarioRestarted(Scenario scenario) {
                System.out.println("WARMUP Scenario restarted: " + scenario.getName());
            }

            @Override
            public void scenarioStopped(Scenario scenario) {
                scenariosInProgress.remove(scenario);
                System.out.println("WARMUP Scenario stopped: " + scenario.getName());
            }
        });
        warmupMixer.addStateListener((fromState, toState) -> System.out.println("WARMUP State change from " + fromState + " to " + toState));

        warmupMixer.initialize();
        warmupMixer.start();
        // TODO: make warmup parameters configurable, like running time per scenario, amount of loops per scenario etc.
        List<Scenario> warmupScenarios = new MixerWarmup().getWarmupScenarios(this);
        // TODO: possibility to parallelize running multiple scenario's
        warmupScenarios.forEach(scenario -> {
            scenariosInProgress.add(scenario);
            warmupMixer.playScenario(scenario);
            while (scenariosInProgress.contains(scenario)) {
                try {
                    Thread.sleep(WARMUP_SLEEP_MILLIS);
                } catch (InterruptedException e) {}
            }
        });
        warmupMixer.stop();
        this.updateState(MixerState.INITIALIZED);

        long end = System.nanoTime();
        System.out.println("Millis for warmup: " + (end - start) / 1_000_000.0);
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
                // TODO: some way to check this assumption? -> try to set the default audio out in the OS and see if this behavior changes
                // In case an exception is thrown for this mixer, it's probably used as the default,
                // so use that one by setting the mixer info to 'null' (Java Sound API limitation?).
                outputLine = AudioSystem.getSourceDataLine(this.outputFormat.toJavaAudioFormat(), null);
            }
            SoundCardStream soundCardStream = new SoundCardStream(soundCard, outputLine);
            this.soundCardStreams.add(soundCardStream);
        }
    }

    // If in warmup, blocks until initialized
    public void start() {
        if (this.state != MixerState.WARMUP && this.state != MixerState.INITIALIZED) {
            throw new IllegalStateException("Invalid state (" + this.state + "), unable to start.");
        }
        while (this.state == MixerState.WARMUP) {
            // TODO: util sleep method
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {}
        }
        this.soundCardStreams.forEach(SoundCardStream::open);
        this.workerThread.start();
    }

    // TODO: naming of input / output / buffers / samples / amplitude etc.
    private void updateStreams() {
        while (this.state.isRunning()) {
            this.activateScenarios();
            this.doUpdate();
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {}
        }
        this.soundCardStreams.forEach(SoundCardStream::stop);
        this.updateState(MixerState.STOPPED);
    }

    private void activateScenarios() {
        // Loop over a copy, to prevent concurrent modification issues. Make the copy in an atomic way by using the synchronized toArray.
        List<ActiveScenario> scenariosToActivateCopy = new ArrayList<>(Arrays.asList(this.scenariosToActivate.toArray(new ActiveScenario[0])));
        this.activeScenarios.addAll(scenariosToActivateCopy);
        // Now remove all activated scenario's from the collection (which might have grown in the mean time).
        this.scenariosToActivate.removeAll(scenariosToActivateCopy);
        scenariosToActivateCopy.forEach(scenario -> this.playbackListeners
                .forEach(listener -> listener.scenarioEventNoException(listener::scenarioStarted, scenario.getScenario())));
    }

    private void doUpdate() {
        long start = System.nanoTime();

        // Gather all data to write.
        ImmerseAudioLoop audioLoop = new ImmerseAudioLoop(this.activeScenarios, this.soundCardStreams, this.outputFormat);
        Map<SoundCardStream, byte[]> dataToWrite = audioLoop.getDataToWrite();

        // Signal scenario start just before adding the audio data to the buffer.
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
            this.activeScenarios.remove(activeScenario);
            // TODO: use thread pool
            new Thread(() -> ImmerseAudioMixer.this.restartScenario(activeScenario)).start();
            this.playbackListeners.forEach(listener -> listener.scenarioEventNoException(listener::scenarioRestarted, activeScenario.getScenario()));
        }
        this.activeScenarios.removeAll(audioLoop.getScenariosToRemove());
        audioLoop.getScenariosToRemove().forEach(scenario -> this.playbackListeners
                .forEach(listener -> listener.scenarioEventNoException(listener::scenarioStopped, scenario.getScenario())));
    }

    private void updateState(MixerState newState) {
        if (!this.state.isAllowedNextState(newState)) {
            throw new IllegalStateException("Invalid state transition from: " + this.state + " to " + newState);
        }
        MixerState oldState = this.state;
        this.state = newState;
        this.stateListeners.forEach(listener -> listener.stateChanged(oldState, this.state));
    }

    public void playScenario(Scenario scenario) {
        if (!this.state.isRunning()) {
            throw new IllegalStateException("Mixer is not in a running state (" + this.state + ")");
        }
        if (this.room != scenario.getRoom()) {
            throw new IllegalArgumentException("The room object should be identical. This mixer is configured for room: " + this.room.getName() + ", "
                    + "while the provided scenario is for room: " + scenario.getRoom().getName());
        }
        this.scenariosToActivate.add(new ActiveScenario(scenario, this.convertAudioStream(scenario)));
    }

    public void restartScenario(ActiveScenario activeScenario) {
        activeScenario.reset(this.convertAudioStream(activeScenario.getScenario()));
        this.scenariosToActivate.add(activeScenario);
    }

    private AudioInputStream convertAudioStream(Scenario scenario) {
        try {
            AudioInputStream originalStream = scenario.getAudioResource().constructAudioStream();
            // Workaround for a JDK bug: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8146338
            // TODO: write more information about the bug: asymmetric unsigned byte --> signed float --> signed byte conversion
            AudioInputStream signedStream = AudioUtil.convert(originalStream, toSigned(originalStream.getFormat()));
            AudioInputStream converted = AudioUtil.convert(signedStream, this.inputFormat.toJavaAudioFormat());
            return converted;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to convert audio input stream", e);
        }
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
