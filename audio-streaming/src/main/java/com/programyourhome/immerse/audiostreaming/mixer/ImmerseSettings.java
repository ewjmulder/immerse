package com.programyourhome.immerse.audiostreaming.mixer;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;

import com.programyourhome.immerse.audiostreaming.util.AudioUtil;
import com.programyourhome.immerse.audiostreaming.util.LogUtil;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * This class holds all Immerse system wide settings.
 * It's design is not really according to any nice pattern, but is very pragmatic
 * statically accessible, so it will be easy to get settings from any piece of code
 * without having to pass a reference to it all over the place.
 *
 * TODO: make configurable, document usage (should be set before mixer starts and stay the same for 1 mixer 'lifetime').
 * TODO: refine this further, see #74
 * TODO: update and move things to ActiveImmerseSettings
 */
public class ImmerseSettings implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    public static int DEFAULT_SOUND_CARD_BUFFER_MILLIS = 30;
    public static int DEFAULT_STEP_PACE_MILLIS = 5;
    public static int DEFAULT_AUDIO_INPUT_BUFFER_LIVE_MILLIS = 25;
    public static int DEFAULT_AUDIO_INPUT_BUFFER_NON_LIVE_MILLIS = 50;
    public static int DEFAULT_WAIT_FOR_PREDICATE_MILLIS = 5;
    public static int DEFAULT_TRIGGER_MINOR_GC_THRESHOLD_KB = 1000;

    private int soundCardBufferMillis;
    private int stepPaceMillis;
    private int audioInputBufferLiveMillis;
    private int audioInputBufferNonLiveMillis;
    private int waitForPredicateMillis;
    private int triggerMinorGcThresholdKb;

    // The room this mixer is active in. One mixer can work in only one room.
    private Room room;
    // The collection of sound cards this mixer should use.
    private Set<SoundCard> soundCards;
    // The output format to use.
    private ImmerseAudioFormat outputFormat;
    // The input format that this mixer can operate on.
    private ImmerseAudioFormat inputFormat;
    // The executor service for all asynchronous tasks.
    private ExecutorService executorService;

    private ImmerseSettings() {
    }

    public int getSoundCardBufferMillis() {
        return this.soundCardBufferMillis;
    }

    public int getStepPaceMillis() {
        return this.stepPaceMillis;
    }

    public int getAudioInputBufferLiveMillis() {
        return this.audioInputBufferLiveMillis;
    }

    public int getAudioInputBufferNonLiveMillis() {
        return this.audioInputBufferNonLiveMillis;
    }

    public int getWaitForPredicateMillis() {
        return this.waitForPredicateMillis;
    }

    public int getTriggerMinorGcThresholdKb() {
        return this.triggerMinorGcThresholdKb;
    }

    public Room getRoom() {
        return this.room;
    }

    public Set<SoundCard> getSoundCards() {
        return this.soundCards;
    }

    public ImmerseAudioFormat getOutputFormat() {
        return this.outputFormat;
    }

    public AudioFormat getOutputFormatJava() {
        return this.outputFormat.toJavaAudioFormat();
    }

    public ImmerseAudioFormat getInputFormat() {
        return this.inputFormat;
    }

    public AudioFormat getInputFormatJava() {
        return this.inputFormat.toJavaAudioFormat();
    }

    /**
     * Submit a task to be executed asynchronously on the executor service.
     * Also log an exception if thrown during task execution.
     */
    public void submitAsyncTask(Runnable task) {
        if (this.executorService == null) {
            this.executorService = Executors.newCachedThreadPool();
        }
        this.executorService.submit(() -> LogUtil.logExceptions(task));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmerseSettings settings;

        public Builder() {
            this.settings = new ImmerseSettings();
            this.settings.soundCardBufferMillis = DEFAULT_SOUND_CARD_BUFFER_MILLIS;
            this.settings.stepPaceMillis = DEFAULT_STEP_PACE_MILLIS;
            this.settings.audioInputBufferLiveMillis = DEFAULT_AUDIO_INPUT_BUFFER_LIVE_MILLIS;
            this.settings.audioInputBufferNonLiveMillis = DEFAULT_AUDIO_INPUT_BUFFER_NON_LIVE_MILLIS;
            this.settings.waitForPredicateMillis = DEFAULT_WAIT_FOR_PREDICATE_MILLIS;
            this.settings.triggerMinorGcThresholdKb = DEFAULT_TRIGGER_MINOR_GC_THRESHOLD_KB;
        }

        public Builder soundCardBufferMillis(int soundCardBufferMillis) {
            this.settings.soundCardBufferMillis = soundCardBufferMillis;
            return this;
        }

        public Builder stepPaceMillis(int stepPaceMillis) {
            this.settings.stepPaceMillis = stepPaceMillis;
            return this;
        }

        public Builder audioInputBufferLiveMillis(int audioInputBufferLiveMillis) {
            this.settings.audioInputBufferLiveMillis = audioInputBufferLiveMillis;
            return this;
        }

        public Builder audioInputBufferNonLiveMillis(int audioInputBufferNonLiveMillis) {
            this.settings.audioInputBufferNonLiveMillis = audioInputBufferNonLiveMillis;
            return this;
        }

        public Builder room(Room room) {
            this.settings.room = room;
            return this;
        }

        public Builder soundCards(Set<SoundCard> soundCards) {
            this.settings.soundCards = soundCards;
            return this;
        }

        public Builder outputFormat(ImmerseAudioFormat outputFormat) {
            this.settings.outputFormat = outputFormat;
            // For input: just switch from stereo to mono, because an input stream should always consist of 1 channel that will be mixed dynamically.
            this.settings.inputFormat = AudioUtil.toMonoInput(outputFormat);
            return this;
        }

        public ImmerseSettings build() {
            return this.settings;
        }
    }

}
