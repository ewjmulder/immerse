package com.programyourhome.immerse.domain;

import java.io.Serializable;
import java.util.Set;

import javax.sound.sampled.AudioFormat;

import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.RecordingMode;

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

    public class TechnicalSettings implements Serializable {

        private static final long serialVersionUID = Serialization.VERSION;

        public static final int DEFAULT_SOUND_CARD_BUFFER_MILLIS = 20;
        public static final int DEFAULT_STEP_PACE_MILLIS = 5;
        public static final int DEFAULT_WAIT_FOR_CONDITION_MILLIS = 5;
        public static final int DEFAULT_TRIGGER_MINOR_GC_THRESHOLD_KB = 1000;

        private int soundCardBufferMillis;
        private int stepPaceMillis;
        private int waitForConditionMillis;
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
         * Amount of millis to sleep between each check for conditional waiting.
         */
        public int getWaitForConditionMillis() {
            return this.waitForConditionMillis;
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
            this.settings.inputFormat = outputFormat.copyToBuilder().recordingMode(RecordingMode.MONO).buildForInput();
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
                this.technicalSettings.waitForConditionMillis = TechnicalSettings.DEFAULT_WAIT_FOR_CONDITION_MILLIS;
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

            public Builder functional() {
                return Builder.this;
            }

            public ImmerseSettings build() {
                return Builder.this.settings;
            }

        }
    }

}
