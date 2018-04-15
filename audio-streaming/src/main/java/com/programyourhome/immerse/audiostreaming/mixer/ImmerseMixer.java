package com.programyourhome.immerse.audiostreaming.mixer;

import static com.programyourhome.immerse.audiostreaming.util.AudioUtil.toSigned;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.mixer.scenario.ActiveScenario;
import com.programyourhome.immerse.audiostreaming.mixer.scenario.ScenarioPlaybackListener;
import com.programyourhome.immerse.audiostreaming.mixer.step.MixerStep;
import com.programyourhome.immerse.audiostreaming.mixer.warmup.CoverAllSettingsWarmupScenarioGenerator;
import com.programyourhome.immerse.audiostreaming.soundcard.SoundCardDetector;
import com.programyourhome.immerse.audiostreaming.soundcard.SoundCardStream;
import com.programyourhome.immerse.audiostreaming.util.AudioUtil;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

// TODO: mention API to the outside is just lifecycle: initialize, start, stop and playScenario
// No 'leaking' of internal data like ActiveScenario, just communication with playback id's
// and mention the scenariosToActive synchronysation
public class ImmerseMixer {

    // TODO: make issue to remove all TODO's into solutions or issues (unless about logging or unit tests)

    private static final int STEP_PACE_MILLIS = 5;
    private static final int WAIT_FOR_PREDICATE = 5;

    // Listeners for the mixer state changes.
    private final Set<MixerStateListener> stateListeners;
    // Listeners for the scenario playback changes.
    private final Set<ScenarioPlaybackListener> playbackListeners;
    // The room this mixer is active in. One mixer can work in only one room.
    private final Room room;
    // The collection of sound cards this mixer should use.
    private final Set<SoundCard> soundCards;
    // The collection of sound card streams to write audio data to.
    private final Set<SoundCardStream> soundCardStreams;
    // The output format to use.
    private final ImmerseAudioFormat outputFormat;
    // The input format that this mixer can operate on.
    private final ImmerseAudioFormat inputFormat;
    // Scenarios that should be activated in the next step.
    private final Set<ActiveScenario> scenariosToActivate;
    // Scenarios that are being played by this mixer, whether currently active or not (e.g. (re)starting, stopping)
    private final Set<ActiveScenario> scenariosInPlayback;
    // Scenarios that are currently active: their audio should be processed in the next step.
    private final Set<ActiveScenario> activeScenarios;
    // A sound card detector for getting the right mixer info objects.
    private final SoundCardDetector soundCardDetector;
    // Separate thread that runs the mixer logic.
    private final Thread workerThread;
    // The current state of this mixer.
    private MixerState state;
    // Whether this is a warmup mixer or not.
    private boolean warmupMixer;

    public ImmerseMixer(Room room, Set<SoundCard> soundCards, ImmerseAudioFormat outputAudioFormat) {
        if (room.getSpeakers().size() < 2) {
            throw new IllegalArgumentException("The room should have at least 2 speakers");
        }
        if (soundCards.isEmpty()) {
            throw new IllegalArgumentException("There should at least be one sound card");
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
        // For input: just switch from stereo to mono, because an input stream should always consist of 1 channel that will be mixed dynamically.
        this.inputFormat = AudioUtil.toMonoInput(this.outputFormat);
        // Explicitly synchronize this set, because it is the only 'overlapping' part between the mixer internals and the 'outside' world.
        this.scenariosToActivate = Collections.synchronizedSet(new HashSet<>());
        this.scenariosInPlayback = new HashSet<>();
        this.activeScenarios = new HashSet<>();
        this.soundCardDetector = new SoundCardDetector();
        // Prepare the worker thread (but do not start it yet).
        this.workerThread = new Thread(this::run);
        this.state = MixerState.NEW;
        // Default to 'standard' mixer. Property is only settable from inside this class, since warmup is no 'external feature'.
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

    /**
     * Whether or not this mixer has scenarios in playback.
     */
    public boolean hasScenariosInPlayback() {
        return !this.getScenariosInPlayback().isEmpty();
    }

    /**
     * Get all playback id's of all scenarios in playback.
     */
    public Set<UUID> getScenariosInPlayback() {
        return StreamEx.of(this.scenariosInPlayback)
                .map(ActiveScenario::getId)
                .toSet();
    }

    /**
     * Whether or not the provided playback id is still in playback.
     */
    public boolean isScenarioInPlayback(UUID playbackId) {
        return this.getScenariosInPlayback().contains(playbackId);
    }

    /**
     * Perform the initialization logic of the mixer.
     * That means initializing the sound card streams and performing warmup as configured.
     * The warmup will start asynchronously and no scenarios can start until it is done.
     */
    public void initialize() {
        if (this.state != MixerState.NEW) {
            throw new IllegalStateException("Should be in state NEW to initialize");
        }
        try {
            this.soundCardDetector.detectSoundCards();
            this.initializeSoundCardStreams(this.soundCards);
        } catch (IOException | LineUnavailableException e) {
            throw new IllegalStateException("Exception during initialization", e);
        }
        this.updateState(MixerState.WARMUP);
        if (this.warmupMixer) {
            // If we are a warmup mixer, mute all sound cards so warmup is silent ...
            this.soundCardStreams.forEach(SoundCardStream::mute);
            // ... and set the state to initialized so warmup scenarios can start playing.
            this.updateState(MixerState.INITIALIZED);
        } else {
            // If we are not a warmup mixer (so a regular mixer), we should initiate warmup (asynchronously).
            new Thread(() -> this.warmup()).start();
        }
    }

    /**
     * Initialize the sound card streams by getting the mixer info for each sound card and
     * then trying to acquire a data line in the right audio format.
     * This might fail if a mixer info cannot be found or a sound card is already in use by another process.
     */
    private void initializeSoundCardStreams(Set<SoundCard> soundCards) throws LineUnavailableException {
        for (SoundCard soundCard : soundCards) {
            SourceDataLine outputLine;
            Mixer.Info mixerInfo = this.soundCardDetector.getMixerInfo(soundCard.getPhysicalPort());
            if (mixerInfo == null) {
                throw new IllegalArgumentException("No mixer found for soundcard: " + soundCard);
            }
            try {
                outputLine = AudioSystem.getSourceDataLine(this.outputFormat.toJavaAudioFormat(), mixerInfo);
            } catch (IllegalArgumentException e) {
                // TODO: logging / warning
                System.out.println("Exception for systemMixerInfo: " + mixerInfo);
                // TODO: some way to check this assumption? -> try to set the default audio out in the OS and see if this behavior changes
                // In case an exception is thrown for this mixer, it's probably used as the default,
                // so use that one by setting the mixer info to 'null' (Java Sound API limitation?).
                outputLine = AudioSystem.getSourceDataLine(this.outputFormat.toJavaAudioFormat(), null);
            }
            this.soundCardStreams.add(new SoundCardStream(soundCard, outputLine));
        }
    }

    /**
     * Perform JVM warmup to optimize performance of the first scenario. Without warmup, there will be hickups in
     * playback for all code that is ran for the first time: both step logic and different types of scenarios.
     *
     * Warmup is done by creating a new mixer specifically for warmup and letting it play some different scenarios.
     */
    private void warmup() {
        long start = System.nanoTime();

        // Create a new mixer with the same configuration as this one.
        ImmerseMixer warmupMixer = new ImmerseMixer(this.room, this.soundCards, this.outputFormat);
        // Set that mixer to be a warmup mixer.
        warmupMixer.warmupMixer = true;

        warmupMixer.initialize();
        warmupMixer.start();
        Map<Scenario, Long> warmupScenarios = new CoverAllSettingsWarmupScenarioGenerator().generateWarmupScenarios(this);
        warmupScenarios.forEach((scenario, runningTime) -> {
            warmupMixer.playScenario(scenario);
            // Sleep for a fraction of the running time, so multiple scenarios will overlap during warmup.
            this.sleep((long) (runningTime * 0.3));
        });
        // Now wait for all warmup scenarios to complete.
        this.waitFor(() -> !warmupMixer.hasScenariosInPlayback());
        // Warmup is done, stop the warmup mixer.
        warmupMixer.stop();
        // This mixer, the 'regular' mixer is now initialized and ready to play 'real' scenarios.
        this.updateState(MixerState.INITIALIZED);

        long end = System.nanoTime();
        // TOOD: proper logging
        System.out.println("Millis for warmup: " + (end - start) / 1_000_000.0);
    }

    /**
     * Start the mixer. That means start a separate thread that will run the mixer and keep the audio buffers full.
     * After this method call is completed, scenarios can start playing.
     * If the mixer is still in warmup, this method will block until that is completed.
     */
    public void start() {
        if (this.state != MixerState.WARMUP && this.state != MixerState.INITIALIZED) {
            throw new IllegalStateException("Invalid state (" + this.state + "), unable to start.");
        }
        this.waitFor(() -> this.state == MixerState.INITIALIZED);
        // Open (lock) the sound cards streams so we can write data to them.
        this.soundCardStreams.forEach(SoundCardStream::open);
        // Start the thread that will run the mixer.
        this.workerThread.start();
    }

    /**
     * Run the mixer: loop while the state is running and keep performing the buffer update logic.
     * When the state changes to not running, gracefully quit the run loop and stop all sound card streams.
     */
    private void run() {
        while (this.state.isRunning()) {
            // Activate any 'waiting' scenarios.
            this.activateScenarios();
            // Update the audio buffers and record the processing time.
            long stepNanos = this.updateBuffers();
            double stepMillis = stepNanos / 1_000_000.0;
            // TODO: sleep millis - actual step execution time. + warn when actual time is bigger than sleep time!
            System.out.println("Millis for step: " + stepMillis);
            if (stepMillis > STEP_PACE_MILLIS) {
                // TODO: log warn about not keeping up the pace!
            } else {
                // Sleep for the step pace millis - the time it took to run the step logic.
                this.sleep(STEP_PACE_MILLIS - (int) Math.round(stepMillis));
            }
        }
        // When the while loop above has broken, we should stop this mixer, so stop all sound card streams.
        this.soundCardStreams.forEach(SoundCardStream::stop);
        // Clear the scenario collections for proper state cleanup.
        this.scenariosToActivate.clear();
        this.activeScenarios.clear();
        this.scenariosInPlayback.clear();
        // Signal that we have fully stopped.
        this.updateState(MixerState.STOPPED);
    }

    /**
     * Activate all scenarios to activate. This 'detour' is implemented to make sure the activeScenarios collection does not run
     * into concurrent modification exceptions. This method is called before the step logic so we can safely modify the activeScenarios collection.
     */
    private void activateScenarios() {
        // Loop over a copy, to prevent concurrent modification issues. Make the copy in an atomic way by using the synchronized toArray.
        List<ActiveScenario> scenariosToActivateCopy = new ArrayList<>(Arrays.asList(this.scenariosToActivate.toArray(new ActiveScenario[0])));
        this.activeScenarios.addAll(scenariosToActivateCopy);
        // Now remove all activated scenario's from the collection (which might have grown in the mean time).
        this.scenariosToActivate.removeAll(scenariosToActivateCopy);
        scenariosToActivateCopy.forEach(scenario -> this.playbackListeners
                .forEach(listener -> listener.scenarioEventNoException(listener::scenarioStarted, scenario.getId())));
    }

    /**
     * Update the sound card buffers with the next step of audio data.
     */
    private long updateBuffers() {
        long start = System.nanoTime();

        // Gather all data to write by running the mixer step algorithm.
        MixerStep mixerStep = new MixerStep(this.activeScenarios, this.soundCardStreams, this.outputFormat);
        Map<SoundCardStream, byte[]> dataToWrite = mixerStep.calculateBufferData();

        // Signal scenario start just before adding the audio data to the buffer.
        this.activeScenarios.forEach(ActiveScenario::startIfNotStarted);

        // Actually write the buffer data to the sound card stream (will run in a separate thread).
        EntryStream.of(dataToWrite).forKeyValue(SoundCardStream::writeToLine);

        // If not started, do start the streams after the initial buffer fill, to be in sync as much as possible.
        if (this.state == MixerState.INITIALIZED) {
            this.soundCardStreams.forEach(SoundCardStream::start);
            this.updateState(MixerState.STARTED);
        }

        // Now handle the scenario life cycle actions that were gathered during the mixer step.
        this.handleScenarioLifecycle(mixerStep);

        long end = System.nanoTime();
        return end - start;
    }

    /**
     * During the mixer step algorithm, some scenario lifecycle actions may have been gathered.
     * Those are handled in this method. This activity is split, to prevent issues with changes in
     * collections that are also being looped at the same time.
     */
    private void handleScenarioLifecycle(MixerStep mixerStep) {
        for (ActiveScenario activeScenario : mixerStep.getScenariosToRestart()) {
            this.activeScenarios.remove(activeScenario);
            // TODO: use thread pool
            new Thread(() -> ImmerseMixer.this.restartScenario(activeScenario)).start();
            // TODO: handle events in a separate thread to prevent expensive listeners from giving hickups in playback
            this.playbackListeners.forEach(listener -> listener.scenarioEventNoException(listener::scenarioRestarted, activeScenario.getId()));
        }
        this.activeScenarios.removeAll(mixerStep.getScenariosToRemove());
        this.scenariosInPlayback.removeAll(mixerStep.getScenariosToRemove());
        // TODO: handle events in a separate thread to prevent expensive listeners from giving hickups in playback
        mixerStep.getScenariosToRemove().forEach(scenario -> this.playbackListeners
                .forEach(listener -> listener.scenarioEventNoException(listener::scenarioStopped, scenario.getId())));
    }

    /**
     * Restart a scenario for another loop.
     * This means all state is reset, except the Playback. This still counts as the same
     * playback, just the next loop.
     */
    private void restartScenario(ActiveScenario activeScenario) {
        activeScenario.resetForNextStart(this.convertAudioStream(activeScenario.getScenario()));
        this.scenariosToActivate.add(activeScenario);
    }

    /**
     * Play a scenario on the mixer.
     * Will throw an exception if the mixer is not in a running state.
     * After this method returns, the scenario will become active in the next step.
     * The return value is a unique identifier for this playback of the scenario and can be used
     * to query if the playback is still in progress. Also the same id will be used in events sent
     * to scenario listeners.
     */
    public UUID playScenario(Scenario scenario) {
        if (!this.state.isRunning()) {
            throw new IllegalStateException("Mixer is not in a running state (" + this.state + ")");
        }
        if (this.room != scenario.getRoom()) {
            throw new IllegalArgumentException("The room object should be identical. This mixer is configured for room: " + this.room.getName() + ", "
                    + "while the provided scenario is for room: " + scenario.getRoom().getName());
        }
        ActiveScenario activeScenario = new ActiveScenario(scenario, this.convertAudioStream(scenario));
        this.scenariosInPlayback.add(activeScenario);
        this.scenariosToActivate.add(activeScenario);
        return activeScenario.getId();
    }

    /**
     * Convert the audio input stream of a scenario to the desired audio format.
     * This method uses the build-in AudioSystem converters to perform the actual conversion.
     */
    private AudioInputStream convertAudioStream(Scenario scenario) {
        AudioInputStream originalStream = scenario.getSettings().getAudioResourceSupplier().get().getAudioInputStream();
        // Workaround for a JDK bug: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8146338
        // TODO: write more information about the bug: asymmetric unsigned byte --> signed float --> signed byte conversion
        AudioInputStream signedStream = AudioUtil.convert(originalStream, toSigned(originalStream.getFormat()));
        AudioInputStream converted = AudioUtil.convert(signedStream, this.inputFormat.toJavaAudioFormat());
        return converted;
    }

    /**
     * Stop the mixer.
     * This method will signal the worker thread to stop after the next step and blocks
     * until the worker thread has performed the stop logic and died.
     */
    public void stop() {
        this.updateState(MixerState.STOPPING);
        try {
            this.workerThread.join();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Stopping interrupted", e);
        }
    }

    /**
     * Update the mixer state to the new state.
     * This method will also validate that the state change is allowed.
     */
    private void updateState(MixerState newState) {
        if (!this.state.isAllowedNextState(newState)) {
            throw new IllegalStateException("Invalid state transition from: " + this.state + " to " + newState);
        }
        MixerState oldState = this.state;
        this.state = newState;
        // TODO: handle events in a separate thread to prevent expensive listeners from giving hickups in playback
        this.stateListeners.forEach(listener -> listener.stateChangedNoException(oldState, this.state));
    }

    /**
     * Utility method that can wait for a certain predicate to become true.
     */
    private void waitFor(Supplier<Boolean> predicate) {
        while (!predicate.get()) {
            this.sleep(WAIT_FOR_PREDICATE);
        }
    }

    /**
     * Sleep for a certain amount of millis and ignore an InterruptedException.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {}
    }

}
