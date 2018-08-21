package com.programyourhome.immerse.audiostreaming.mixer.scenario;

import static com.programyourhome.immerse.audiostreaming.mixer.ActiveImmerseSettings.getSettings;
import static com.programyourhome.immerse.domain.format.ImmerseAudioFormat.fromJavaAudioFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.pmw.tinylog.Logger;

import com.programyourhome.immerse.audiostreaming.mixer.AudioInputStreamWrapper;
import com.programyourhome.immerse.audiostreaming.util.LogUtil;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.playback.Playback;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.algorithms.normalize.NormalizeAlgorithm;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

/**
 * An active scenario is a scenario that is begin played by the mixer.
 * It contains the 'static' scenario data and all settings interface implementations that can contain state.
 * It also keeps track of the elapsed time since the playback has started.
 * An active scenario can be used for several playback loops.
 * In the first loop it will consume the original audio input stream and simultaneously cache those bytes (unless the resource is dynamic).
 * In subsequent loops it will use the cached data, so the original stream does not have to be reused / reopened.
 */
public class ActiveScenario {

    private final UUID id;
    private final Scenario scenario;
    private DynamicLocation sourceLocation;
    private DynamicLocation listenerLocation;
    private VolumeRatiosAlgorithm volumeRatiosAlgorithm;
    private NormalizeAlgorithm normalizeAlgorithm;
    private final Playback playback;
    private AudioInputStream inputStream;
    private AudioInputBuffer inputBuffer;
    private final File cachedStreamFile;
    private final boolean live;
    private long startMillis;

    public ActiveScenario(Scenario scenario, AudioInputStream audioInputStream, boolean live) {
        this.id = UUID.randomUUID();
        this.scenario = scenario;
        this.playback = this.scenario.getSettings().getPlaybackFactory().create();
        this.live = live;
        if (this.live) {
            this.inputStream = audioInputStream;
            this.cachedStreamFile = null;
        } else {
            try {
                this.cachedStreamFile = File.createTempFile("scenario-cache-", "-pcm-bytes");
                this.inputStream = new AudioInputStreamWrapper(audioInputStream, new FileOutputStream(this.cachedStreamFile));
            } catch (IOException e) {
                throw new IllegalStateException("Exception during stream caching init", e);
            }
        }
        this.inputBuffer = this.createAndFillInputBuffer();
        this.resetFromSettings();
    }

    /**
     * Uniquely identifies this active scenario, since the scenario itself can be re-used multiple times.
     */
    public UUID getId() {
        return this.id;
    }

    public Scenario getScenario() {
        return this.scenario;
    }

    public DynamicLocation getSourceLocation() {
        return this.sourceLocation;
    }

    public DynamicLocation getListenerLocation() {
        return this.listenerLocation;
    }

    public VolumeRatiosAlgorithm getVolumeRatiosAlgorithm() {
        return this.volumeRatiosAlgorithm;
    }

    public NormalizeAlgorithm getNormalizeAlgorithm() {
        return this.normalizeAlgorithm;
    }

    public Playback getPlayback() {
        return this.playback;
    }

    public AudioInputStream getInputStream() {
        return this.inputStream;
    }

    public AudioInputBuffer getInputBuffer() {
        return this.inputBuffer;
    }

    public long getStartMillis() {
        return this.startMillis;
    }

    public boolean isStarted() {
        return this.startMillis > -1;
    }

    /**
     * Start this active scenario, by recording the current time as start time.
     * For convenience, this method can be called several times and checks if action is actually required.
     */
    public void startIfNotStarted() {
        if (!this.isStarted()) {
            this.startMillis = System.currentTimeMillis();
            this.playback.audioStarted();
        }
    }

    /**
     * Reset this active scenario for the next start of playback (after at least 1 completed loop).
     * Get the input stream from the created cache file and reset the settings.
     */
    public void resetForNextStart() {
        if (this.live) {
            throw new IllegalStateException("Live audio resources cannot be restarted.");
        }
        AudioFormat format = this.inputStream.getFormat();
        long length = this.inputStream.getFrameLength();
        try {
            this.inputStream = new AudioInputStream(new FileInputStream(this.cachedStreamFile), format, length);
            this.inputBuffer = this.createAndFillInputBuffer();
        } catch (IOException e) {
            throw new IllegalStateException("Exception during stream reset from cache", e);
        }
        this.resetFromSettings();
    }

    private AudioInputBuffer createAndFillInputBuffer() {
        AudioInputBuffer audioInputBuffer;
        // TODO: Fix this in a proper way (LiveConfig or some other general config?)
        if (this.live) {
            audioInputBuffer = new AudioInputBuffer(this.inputStream,
                    this.calculateScenarioInputBufferSize(fromJavaAudioFormat(this.inputStream.getFormat()), this.live),
                    this.live, /* FIXME!!! Hard coded */ 100);
        } else {
            audioInputBuffer = new AudioInputBuffer(this.inputStream,
                    this.calculateScenarioInputBufferSize(fromJavaAudioFormat(this.inputStream.getFormat()), this.live));
        }
        if (this.live) {
            // For live streams: just continuously call align and fill to stay as close to 'live' as possible.
            // The thread will block at I/O when there is nothing to read from the live stream.
            new Thread(() -> LogUtil.logExceptions(() -> {
                while (true) {
                    audioInputBuffer.fill();
                    // TODO: do we need a very small sleep here?
                }
            })).start();
        } else {
            // Non-live streams: perform the initial fill synchronously, so the input buffer is ready to go.
            audioInputBuffer.fill();
        }
        return audioInputBuffer;
    }

    private int calculateScenarioInputBufferSize(ImmerseAudioFormat format, boolean live) {
        int bufferSizeMillis = live ? getSettings().getAudioInputBufferLiveMillis() : getSettings().getAudioInputBufferNonLiveMillis();
        // Take an (approximate) buffer size for the amount of millis we want to buffer.
        int bufferSizeBytes = bufferSizeMillis * (int) format.getNumberOfBytesPerMilli();
        // Cut the buffer size off at an exact amount of frames to avoid issues when reading from the underlying AudioInputStream.
        bufferSizeBytes -= bufferSizeBytes % format.getNumberOfBytesPerFrame();
        return bufferSizeBytes;
    }

    /**
     * Get fresh copies of all settings, except for the Playback object,
     * since that should persist over several restarts to be able to count loops.
     */
    private void resetFromSettings() {
        this.startMillis = -1;
        this.sourceLocation = this.scenario.getSettings().getSourceLocationFactory().create();
        this.listenerLocation = this.scenario.getSettings().getListenerLocationFactory().create();
        this.volumeRatiosAlgorithm = this.scenario.getSettings().getVolumeRatiosAlgorithmFactory().create();
        this.normalizeAlgorithm = this.scenario.getSettings().getNormalizeAlgorithmFactory().create();
    }

    /**
     * Completely stop this active scenario, releasing all resources it currently holds.
     */
    public void stop() {
        try {
            this.inputStream.close();
            if (this.cachedStreamFile != null) {
                this.cachedStreamFile.delete();
            }
        } catch (IOException e) {
            Logger.warn(e, "Exception during stopping of active scenario");
        }
    }

}
