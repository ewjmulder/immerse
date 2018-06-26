package com.programyourhome.immerse.audiostreaming.mixer;

import static com.programyourhome.immerse.audiostreaming.util.AudioUtil.toSigned;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.pmw.tinylog.Logger;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.mixer.scenario.ActiveScenario;
import com.programyourhome.immerse.audiostreaming.mixer.scenario.ScenarioPlaybackListener;
import com.programyourhome.immerse.audiostreaming.mixer.step.MixerStep;
import com.programyourhome.immerse.audiostreaming.mixer.warmup.CoverAllSettingsWarmupScenarioGenerator;
import com.programyourhome.immerse.audiostreaming.soundcard.SoundCardDetector;
import com.programyourhome.immerse.audiostreaming.soundcard.SoundCardStream;
import com.programyourhome.immerse.audiostreaming.util.AudioUtil;
import com.programyourhome.immerse.audiostreaming.util.IOUtil;
import com.programyourhome.immerse.audiostreaming.util.MemoryUtil;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

/**
 * The Immerse Mixer is the 'player' of the Immerse system.
 * The mixer itself should be created, initialized and started.
 * After it is fully started, it can play scenarios.
 * It's main loop will keep updating the sound card streams with new buffer data, so essentially keep Immerse running.
 *
 * This class also contains a lot of 'book keeping' about scenario playback state.
 * Furthermore, it can 'warm up' the system for smooth audio playback later.
 *
 * For more information about the setup of this class, see the Javadoc and comments in this class and the project documentation.
 */
public class ImmerseMixer {

    private static final int STEP_PACE_MILLIS = 5;
    private static final int WAIT_FOR_PREDICATE = 5;
    private static final int TRIGGER_MINOR_GC_THRESHOLD_KB = 1000;

    // A name to differentiate the main and warmup mixer in the logs.
    private String name;
    // The executor service for all asynchronous tasks.
    private final ExecutorService executorService;
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
    // Scenarios that should be stopped in the next step.
    private final Set<UUID> scenariosToStop;
    // Scenarios that are being played by this mixer, whether currently active or not (e.g. (re)starting, stopping)
    private final Set<ActiveScenario> scenariosInPlayback;
    // Scenarios that are currently active: their audio should be processed in the next step.
    private final Map<UUID, ActiveScenario> activeScenarios;
    // A sound card detector for getting the right mixer info objects.
    private final SoundCardDetector soundCardDetector;
    // Separate thread that runs the mixer logic.
    private final Thread workerThread;
    // The current state of this mixer.
    private MixerState state;
    // Whether this is the warmup mixer or not.
    private boolean warmupMixer;

    public ImmerseMixer(Room room, Set<SoundCard> soundCards, ImmerseAudioFormat outputAudioFormat) {
        if (soundCards.isEmpty()) {
            throw new IllegalArgumentException("There should at least be one sound card");
        }
        if (!outputAudioFormat.isOutput()) {
            throw new IllegalArgumentException("The provided output audio format should be marked as output");
        }
        this.name = "Main mixer";
        this.executorService = Executors.newCachedThreadPool();
        this.room = room;
        this.soundCards = soundCards;
        this.soundCardStreams = new HashSet<>();
        this.outputFormat = outputAudioFormat;
        // For input: just switch from stereo to mono, because an input stream should always consist of 1 channel that will be mixed dynamically.
        this.inputFormat = AudioUtil.toMonoInput(this.outputFormat);
        this.activeScenarios = new HashMap<>();
        this.soundCardDetector = new SoundCardDetector();
        // Prepare the worker thread (but do not start it yet).
        this.workerThread = new Thread(this::run, "Main Mixer Worker");
        this.state = MixerState.NEW;
        // Default to 'standard' mixer. Property is only settable from inside this class, since warmup is no 'external feature'.
        this.warmupMixer = false;

        // Explicitly synchronize these sets, because they are the 'overlapping' part between the mixer internals and the 'outside' world
        // and are susceptible for ConcurrentModificationException.
        this.stateListeners = Collections.synchronizedSet(new HashSet<>());
        this.playbackListeners = Collections.synchronizedSet(new HashSet<>());
        this.scenariosToActivate = Collections.synchronizedSet(new HashSet<>());
        this.scenariosToStop = Collections.synchronizedSet(new HashSet<>());
        this.scenariosInPlayback = Collections.synchronizedSet(new HashSet<>());
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
        return !this.scenariosInPlayback.isEmpty();
    }

    /**
     * Get all playback id's of all scenarios in playback.
     */
    public Set<UUID> getScenariosInPlayback() {
        return StreamEx.of(this.scenariosInPlayback.toArray(new ActiveScenario[0]))
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
     * Get an atomically created copy of the state listeners, to loop over without possible concurrent modification issues.
     */
    private List<MixerStateListener> getStateListenersCopy() {
        return Arrays.asList(this.stateListeners.toArray(new MixerStateListener[0]));
    }

    /**
     * Get an atomically created copy of the playback listeners, to loop over without possible concurrent modification issues.
     */
    private List<ScenarioPlaybackListener> getPlaybackListenersCopy() {
        return Arrays.asList(this.playbackListeners.toArray(new ScenarioPlaybackListener[0]));
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
            // If we are the warmup mixer, mute all sound cards so warmup is silent ...
            this.soundCardStreams.forEach(SoundCardStream::mute);
            // ... and set the state to initialized so warmup scenarios can start playing.
            this.updateState(MixerState.INITIALIZED);
        } else {
            // If we are not the warmup mixer (so the main mixer), we should initiate warmup (asynchronously).
            new Thread(() -> this.warmup(), "Warmup Mixer Executor").start();
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
                // This exception is a 'known issue' of Java Sound when targeting the system default audio device.
                // As a workaround use the default audio device by setting the mixer info to 'null'.
                // Will show up in the logs for the main mixer, so do not log again in case of the warmup mixer.
                if (!this.warmupMixer) {
                    Logger.info("Exception for mixer info: '" + mixerInfo.getName() + "'. "
                            + "Known Java Sound API issue, falling back to default audio device.");
                }
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
        // Set that mixer to be the warmup mixer.
        warmupMixer.warmupMixer = true;
        warmupMixer.name = "Warmup mixer";
        warmupMixer.workerThread.setName("Warmup Mixer Worker");

        warmupMixer.initialize();
        warmupMixer.start();
        Map<Scenario, Long> warmupScenarios = new CoverAllSettingsWarmupScenarioGenerator().generateWarmupScenarios(this);
        warmupScenarios.forEach((scenario, runningTime) -> {
            warmupMixer.playScenario(scenario);
            // Sleep for a fraction of the running time, so multiple scenarios will overlap during warmup.
            this.sleep((long) (runningTime * 0.3));
            // Also perform some GC to warm up the GC logic.
            System.gc();
        });
        // Now wait for all warmup scenarios to complete.
        this.waitFor(() -> !warmupMixer.hasScenariosInPlayback());
        // Warmup is done, stop the warmup mixer.
        warmupMixer.stop();
        // This mixer, the 'main' mixer is now initialized and ready to play 'real' scenarios.
        this.updateState(MixerState.INITIALIZED);

        long end = System.nanoTime();
        Logger.info("Warmup completed in {0.000} seconds", (end - start) / 1_000_000_000.0);
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
            long start = System.nanoTime();

            // Handle any 'waiting' scenarios.
            this.handleScenarios();
            // Update the audio buffers.
            this.updateBuffers();

            long end = System.nanoTime();

            double stepMillis = (end - start) / 1_000_000.0;
            if (stepMillis > STEP_PACE_MILLIS) {
                // Some large step times are to be expected in warmup mode, so only log a warning if we are not the warmup mixer.
                if (!this.warmupMixer) {
                    Logger.warn("Risk for hickups in playback: actual step millis {} was bigger than the step pace millis {}.", stepMillis, STEP_PACE_MILLIS);
                }
            } else {
                // If we are almost running out of Eden space, trigger a minor GC in a controlled manner.
                // Otherwise, sleep for however long is left of the step pace.
                // NB: only do this if the current step was not slower than the pace, to not trigger an extra delay on an already slow step.
                if (MemoryUtil.getFreeEdenSpaceInKB() < TRIGGER_MINOR_GC_THRESHOLD_KB) {
                    // Allocate the right amount of bytes to just go over the limit, so a minor GC is triggered.
                    byte[] triggerBuffer = new byte[TRIGGER_MINOR_GC_THRESHOLD_KB * 1024];
                    // Do 'something' with the array or the JIT might just 'optimize it away'.
                    // In fact, that 'something' is just printing an empty String, but hopefully enough to forever fool JIT ;).
                    System.out.print(triggerBuffer.length > 0 ? "" : "0");
                } else {
                    // Sleep for the step pace millis - the time it took to run the step logic.
                    this.sleep(STEP_PACE_MILLIS - (int) Math.round(stepMillis));
                }
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
     * Handle all scenarios: activate and stop. This 'detour' is implemented to make sure the activeScenarios collection does not run
     * into concurrent modification exceptions. This method is called before the step logic so we can safely modify the activeScenarios collection.
     */
    private void handleScenarios() {
        // Loop over a copy, to prevent concurrent modification issues. Make the copy in an atomic way by using the synchronized toArray.
        List<UUID> scenariosToStopCopy = Arrays.asList(this.scenariosToStop.toArray(new UUID[0]));
        scenariosToStopCopy.forEach(playbackId -> this.activeScenarios.remove(playbackId));
        // Now remove all activated scenario's from the collection (which might have grown in the mean time).
        this.scenariosToStop.removeAll(scenariosToStopCopy);
        // Loop over a copy, to prevent concurrent modification issues. Make the copy in an atomic way by using the synchronized toArray.
        List<ActiveScenario> scenariosToActivateCopy = Arrays.asList(this.scenariosToActivate.toArray(new ActiveScenario[0]));
        scenariosToActivateCopy.forEach(scenario -> this.activeScenarios.put(scenario.getId(), scenario));
        // Now remove all activated scenario's from the collection (which might have grown in the mean time).
        this.scenariosToActivate.removeAll(scenariosToActivateCopy);
        scenariosToActivateCopy.forEach(scenario -> this.logScenarioEvent(scenario, "started"));
        // Notify of start event asynchronously.
        scenariosToActivateCopy.forEach(scenario -> this.getPlaybackListenersCopy()
                .forEach(listener -> this.executorService.submit(() -> listener.scenarioEventNoException(listener::scenarioStarted, scenario.getId()))));
    }

    /**
     * Update the sound card buffers with the next step of audio data.
     */
    private void updateBuffers() {
        // Gather all data to write by running the mixer step algorithm.
        MixerStep mixerStep = new MixerStep(this.activeScenarios.values(), this.soundCardStreams, this.outputFormat);
        Map<SoundCardStream, byte[]> soundCardBufferData = mixerStep.calculateBufferData();

        // Signal scenario start just before adding the audio data to the buffer.
        this.activeScenarios.values().forEach(ActiveScenario::startIfNotStarted);

        // Actually write the buffer data to the sound card stream (asynchronously).
        EntryStream.of(soundCardBufferData).forKeyValue(
                (soundCardStream, bufferData) -> this.executorService.submit(() -> soundCardStream.writeToLine(bufferData)));

        // If not started, do start the streams after the initial buffer fill, to be in sync as much as possible.
        if (this.state == MixerState.INITIALIZED) {
            this.soundCardStreams.forEach(SoundCardStream::start);
            this.updateState(MixerState.STARTED);
        }

        // Now handle the scenario life cycle actions that were gathered during the mixer step.
        this.handleScenarioLifecycle(mixerStep);
    }

    /**
     * During the mixer step algorithm, some scenario lifecycle actions may have been gathered.
     * Those are handled in this method. This activity is split, to prevent issues with changes in
     * collections that are also being looped at the same time.
     */
    private void handleScenarioLifecycle(MixerStep mixerStep) {
        for (ActiveScenario activeScenario : mixerStep.getScenariosToRestart()) {
            this.activeScenarios.remove(activeScenario.getId());
            // Restart asynchronously.
            this.executorService.submit(() -> ImmerseMixer.this.restartScenario(activeScenario));
            this.logScenarioEvent(activeScenario, "restarted");
            // Notify of restart event asynchronously.
            this.getPlaybackListenersCopy().forEach(
                    listener -> this.executorService.submit(() -> listener.scenarioEventNoException(listener::scenarioRestarted, activeScenario.getId())));
        }
        mixerStep.getScenariosToRemove().forEach(scenarioToRemove -> this.activeScenarios.remove(scenarioToRemove.getId()));
        this.scenariosInPlayback.removeAll(mixerStep.getScenariosToRemove());
        mixerStep.getScenariosToRemove().forEach(scenario -> this.logScenarioEvent(scenario, "stopped"));
        // Notify of stop event asynchronously.
        mixerStep.getScenariosToRemove().forEach(scenario -> this.getPlaybackListenersCopy()
                .forEach(listener -> this.executorService.submit(() -> listener.scenarioEventNoException(listener::scenarioStopped, scenario.getId()))));
    }

    /**
     * Restart a scenario for another loop.
     * This means all state is reset, except the Playback. This still counts as the same
     * playback, just the next loop.
     */
    private void restartScenario(ActiveScenario activeScenario) {
        AudioInputStream cachedAudioInputStream = this.cacheLocally(
                activeScenario.getScenario().getSettings().getAudioResourceFactory().create().getInputStream());
        activeScenario.resetForNextStart(this.convertAudioStream(cachedAudioInputStream));
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
        if (!this.room.getId().equals(scenario.getRoom().getId())) {
            throw new IllegalArgumentException("The room id should be identical. This mixer is configured for room: " + this.room.getId() + ", "
                    + "while the provided scenario is for room: " + scenario.getRoom().getId());
        }
        AudioInputStream cachedAudioInputStream = this.cacheLocally(scenario.getSettings().getAudioResourceFactory().create().getInputStream());
        ActiveScenario activeScenario = new ActiveScenario(scenario, this.convertAudioStream(cachedAudioInputStream));
        this.scenariosInPlayback.add(activeScenario);
        this.scenariosToActivate.add(activeScenario);
        return activeScenario.getId();
    }

    public void waitForPlayback(UUID playbackId) {
        this.waitFor(() -> !this.isScenarioInPlayback(playbackId));
    }

    public void stopScenarioPlayback(UUID playbackId) {
        this.scenariosToStop.add(playbackId);
    }

    /**
     * Cache the audio input stream locally, so we can always read fast from disk and not keep
     * any network connections open while playing.
     */
    private AudioInputStream cacheLocally(InputStream inputStream) {
        // Special case: if the input stream is already an AudioInputStream, return it directly.
        // Otherwise we'll miss the audio headers.
        if (inputStream instanceof AudioInputStream) {
            return (AudioInputStream) inputStream;
        }
        try {
            File cacheFile = File.createTempFile("adventure-room-", ".wav");
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            IOUtil.copyAndClose(inputStream, outputStream);
            return AudioSystem.getAudioInputStream(cacheFile);
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("IOException during local caching", e);
        }
    }

    /**
     * Convert the audio input stream of a scenario to the desired audio format.
     * This method uses the build-in AudioSystem converters to perform the actual conversion.
     */
    private AudioInputStream convertAudioStream(AudioInputStream originalStream) {
        // Workaround for a JDK bug: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8146338
        // See the documentation of this project for more information.
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
        this.logStateEvent(oldState, this.state);
        // Notify of state change event asynchronously.
        this.getStateListenersCopy().forEach(listener -> this.executorService.submit(() -> listener.stateChangedNoException(oldState, this.state)));
    }

    /**
     * Log a scenario event in a generic way.
     */
    private void logScenarioEvent(ActiveScenario activeScenario, String event) {
        this.getEventLogMethod().accept("{} {} scenario {}", new Object[] { this.name, event, activeScenario.getScenario().getName() });
    }

    /**
     * Log a state change event in a generic way.
     */
    private void logStateEvent(MixerState oldState, MixerState newState) {
        this.getEventLogMethod().accept("{} changed from state {} to {}", new Object[] { this.name, oldState, newState });
    }

    /**
     * In case of the main mixer, we log events at level info.
     * But in case of the warmup mixer, use trace to avoid polluting the logs.
     */
    private BiConsumer<String, Object[]> getEventLogMethod() {
        return this.warmupMixer ? Logger::trace : Logger::info;
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
