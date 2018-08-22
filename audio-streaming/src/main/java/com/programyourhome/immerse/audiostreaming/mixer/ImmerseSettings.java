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
 * It's values are tied to a mixer and all related classes.
 * There is a distinction between the functional settings and technical settings, also in the builder.
 */
public class ImmerseSettings implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private TechnicalSettings technicalSettings;
    private Room room;
    private Set<SoundCard> soundCards;
    private ImmerseAudioFormat outputFormat;
    private ImmerseAudioFormat inputFormat;
    private ExecutorService executorService;

    private ImmerseSettings() {
    }

    /**
     * The technical settings.
     */
    public TechnicalSettings getTechnicalSettings() {
        return this.technicalSettings;
    }

    /**
     * The room Immerse is active in.
     * Immerse can work in only one room at a time.
     */
    public Room getRoom() {
        return this.room;
    }

    /**
     * The collection of sound cards in use.
     */
    public Set<SoundCard> getSoundCards() {
        return this.soundCards;
    }

    /**
     * The output format in use (Immerse).
     */
    public ImmerseAudioFormat getOutputFormat() {
        return this.outputFormat;
    }

    /**
     * The output format in use (Java).
     */
    public AudioFormat getOutputFormatJava() {
        return this.outputFormat.toJavaAudioFormat();
    }

    /**
     * The input format in use (Immerse).
     */
    public ImmerseAudioFormat getInputFormat() {
        return this.inputFormat;
    }

    /**
     * The input format in use (Java).
     */
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

    public class TechnicalSettings implements Serializable {

        private static final long serialVersionUID = Serialization.VERSION;

        public static final int DEFAULT_SOUND_CARD_BUFFER_MILLIS = 30;
        public static final int DEFAULT_STEP_PACE_MILLIS = 5;
        public static final int DEFAULT_AUDIO_INPUT_BUFFER_LIVE_MILLIS = 25;
        public static final int DEFAULT_AUDIO_INPUT_BUFFER_NON_LIVE_MILLIS = 50;
        public static final int DEFAULT_WAIT_FOR_PREDICATE_MILLIS = 5;
        public static final int DEFAULT_TRIGGER_MINOR_GC_THRESHOLD_KB = 1000;

        private int soundCardBufferMillis;
        private int stepPaceMillis;
        private int audioInputBufferLiveMillis;
        private int audioInputBufferNonLiveMillis;
        private int waitForPredicateMillis;
        private int triggerMinorGcThresholdKb;

        /**
         * Amount of millis of audio data to keep in each sound card stream.
         */
        public int getSoundCardBufferMillis() {
            return this.soundCardBufferMillis;
        }

        /**
         * Amount of millis per mixer step.
         * Will sleep to match up if the step took shorter.
         * Step can also take longer, than the next one will not try to 'catch up'.
         */
        public int getStepPaceMillis() {
            return this.stepPaceMillis;
        }

        /**
         * Amount of millis to keep in audio input buffer in case of live audio stream.
         */
        public int getAudioInputBufferLiveMillis() {
            return this.audioInputBufferLiveMillis;
        }

        /**
         * Amount of millis to keep in audio input buffer in case of non live audio stream.
         */
        public int getAudioInputBufferNonLiveMillis() {
            return this.audioInputBufferNonLiveMillis;
        }

        /**
         * Amount of millis to sleep between each check for predicate waiting.
         */
        public int getWaitForPredicateMillis() {
            return this.waitForPredicateMillis;
        }

        /**
         * Amount of KB or less that should be left to trigger next minor GC.
         */
        public int getTriggerMinorGcThresholdKb() {
            return this.triggerMinorGcThresholdKb;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmerseSettings settings;
        private final TechnicalBuilder technicalBuilder;

        public Builder() {
            this.settings = new ImmerseSettings();
            this.technicalBuilder = new TechnicalBuilder();
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

        public TechnicalBuilder technical() {
            return this.technicalBuilder;
        }

        public ImmerseSettings build() {
            return this.settings;
        }

        public class TechnicalBuilder {

            private final TechnicalSettings technicalSettings;

            public TechnicalBuilder() {
                this.technicalSettings = Builder.this.settings.new TechnicalSettings();
                this.technicalSettings.soundCardBufferMillis = TechnicalSettings.DEFAULT_SOUND_CARD_BUFFER_MILLIS;
                this.technicalSettings.stepPaceMillis = TechnicalSettings.DEFAULT_STEP_PACE_MILLIS;
                this.technicalSettings.audioInputBufferLiveMillis = TechnicalSettings.DEFAULT_AUDIO_INPUT_BUFFER_LIVE_MILLIS;
                this.technicalSettings.audioInputBufferNonLiveMillis = TechnicalSettings.DEFAULT_AUDIO_INPUT_BUFFER_NON_LIVE_MILLIS;
                this.technicalSettings.waitForPredicateMillis = TechnicalSettings.DEFAULT_WAIT_FOR_PREDICATE_MILLIS;
                this.technicalSettings.triggerMinorGcThresholdKb = TechnicalSettings.DEFAULT_TRIGGER_MINOR_GC_THRESHOLD_KB;
                Builder.this.settings.technicalSettings = this.technicalSettings;
            }

            public TechnicalBuilder soundCardBufferMillis(int soundCardBufferMillis) {
                this.technicalSettings.soundCardBufferMillis = soundCardBufferMillis;
                return this;
            }

            public TechnicalBuilder stepPaceMillis(int stepPaceMillis) {
                this.technicalSettings.stepPaceMillis = stepPaceMillis;
                return this;
            }

            public TechnicalBuilder audioInputBufferLiveMillis(int audioInputBufferLiveMillis) {
                this.technicalSettings.audioInputBufferLiveMillis = audioInputBufferLiveMillis;
                return this;
            }

            public TechnicalBuilder audioInputBufferNonLiveMillis(int audioInputBufferNonLiveMillis) {
                this.technicalSettings.audioInputBufferNonLiveMillis = audioInputBufferNonLiveMillis;
                return this;
            }

            public Builder functional() {
                return Builder.this;
            }

            public ImmerseSettings build() {
                return Builder.this.settings;
            }

        }
    }

}
