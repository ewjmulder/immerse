package com.programyourhome.immerse.domain.format;

import java.io.Serializable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import com.programyourhome.immerse.domain.Serialization;

/**
 * Audio format with all relevant information about the details of the format.
 * Is equivalent to the Java Sound AudioFormat class, but more type safe and more tailored to Immerse.
 */
public class ImmerseAudioFormat implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private boolean output;
    private RecordingMode recordingMode;
    private SampleRate sampleRate;
    private SampleSize sampleSize;
    private Boolean signed;
    private ByteOrder byteOrder;

    private ImmerseAudioFormat() {
    }

    /**
     * Whether or not the audio format is meant for Immerse output or not.
     * (an output format has extra constraints)
     */
    public boolean isOutput() {
        return this.output;
    }

    /**
     * Recording mode can be mono or stereo.
     */
    public RecordingMode getRecordingMode() {
        return this.recordingMode;
    }

    /**
     * One of the predefined possible sample rates.
     */
    public SampleRate getSampleRate() {
        return this.sampleRate;
    }

    /**
     * Sample size can be one or two bytes.
     */
    public SampleSize getSampleSize() {
        return this.sampleSize;
    }

    /**
     * Whether the sample values are signed or not.
     */
    public boolean isSigned() {
        return this.signed;
    }

    /**
     * The byte order can be little or big endian.
     */
    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    public boolean isBigEndian() {
        return this.byteOrder == ByteOrder.BIG_ENDIAN;
    }

    public boolean isLittleEndian() {
        return this.byteOrder == ByteOrder.LITTLE_ENDIAN;
    }

    public int getNumberOfBytesPerSample() {
        return this.sampleSize.getNumberOfBytes();
    }

    public int getNumberOfBytesPerFrame() {
        return this.recordingMode.getNumberOfChannels() * this.sampleSize.getNumberOfBytes();
    }

    public int getNumberOfFramesPerSecond() {
        return this.sampleRate.getNumberOfSamplesPerSecond();
    }

    public int getNumberOfBytesPerSecond() {
        return this.sampleRate.getNumberOfSamplesPerSecond() * this.getNumberOfBytesPerFrame();
    }

    public double getNumberOfBytesPerMilli() {
        return this.getNumberOfBytesPerSecond() / 1000.0;
    }

    public AudioFormat toJavaAudioFormat() {
        return new AudioFormat(this.sampleRate.getNumberOfSamplesPerSecond(), this.sampleSize.getNumberOfBits(),
                this.recordingMode.getNumberOfChannels(), this.signed, this.byteOrder.isBigEndian());
    }

    public static ImmerseAudioFormat fromJavaAudioFormat(AudioFormat audioFormat) {
        return builder()
                .recordingMode(audioFormat.getChannels())
                .sampleRate(audioFormat.getSampleRate())
                .sampleSizeBits(audioFormat.getSampleSizeInBits())
                .setSigned(audioFormat.getEncoding())
                .byteOrderBig(audioFormat.isBigEndian())
                .buildForInput();
    }

    private static boolean encodingToSigned(Encoding encoding) {
        if (encoding == Encoding.ALAW || encoding == Encoding.ULAW || encoding == Encoding.PCM_FLOAT) {
            throw new IllegalArgumentException("Unsupported encoding: '" + encoding + "'");
        }
        return encoding == Encoding.PCM_SIGNED;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmerseAudioFormat format;

        public Builder() {
            this.format = new ImmerseAudioFormat();
        }

        public Builder recordingMode(int numberOfChannels) {
            return this.recordingMode(RecordingMode.fromNumberOfChannels(numberOfChannels));
        }

        public Builder recordingMode(RecordingMode recordingMode) {
            this.format.recordingMode = recordingMode;
            return this;
        }

        public Builder sampleRate(float sampleRate) {
            return this.sampleRate(SampleRate.fromNumberOfSamplesPerSecond(sampleRate));
        }

        public Builder sampleRate(SampleRate sampleRate) {
            this.format.sampleRate = sampleRate;
            return this;
        }

        public Builder sampleSizeBits(int numberOfBits) {
            return this.sampleSize(SampleSize.fromNumberOfBits(numberOfBits));
        }

        public Builder sampleSizeBytes(int numberOfBytes) {
            return this.sampleSize(SampleSize.fromNumberOfBytes(numberOfBytes));
        }

        public Builder sampleSize(SampleSize sampleSize) {
            this.format.sampleSize = sampleSize;
            return this;
        }

        public Builder setSigned(Encoding encoding) {
            return this.setSigned(encodingToSigned(encoding));
        }

        public Builder setSigned(boolean signed) {
            this.format.signed = signed;
            return this;
        }

        public Builder signed() {
            return this.setSigned(true);
        }

        public Builder unsigned() {
            return this.setSigned(false);
        }

        public Builder byteOrderLittle(boolean littleEndian) {
            return this.byteOrder(ByteOrder.fromLittleEndian(littleEndian));
        }

        public Builder byteOrderBig(boolean bigEndian) {
            return this.byteOrder(ByteOrder.fromBigEndian(bigEndian));
        }

        public Builder byteOrderLittle() {
            return this.byteOrder(ByteOrder.LITTLE_ENDIAN);
        }

        public Builder byteOrderBig() {
            return this.byteOrder(ByteOrder.BIG_ENDIAN);
        }

        public Builder byteOrder(ByteOrder byteOrder) {
            this.format.byteOrder = byteOrder;
            return this;
        }

        public ImmerseAudioFormat buildForInput() {
            this.format.output = false;
            return this.format;
        }

        public ImmerseAudioFormat buildForOutput() {
            if (this.format.recordingMode != null) {
                throw new IllegalStateException("Cannot set recording mode for output format");
            } else {
                // Always stereo mode, because we want to supply output to both channels / speakers.
                this.format.recordingMode = RecordingMode.STEREO;
            }
            if (this.format.signed != null) {
                throw new IllegalStateException("Cannot set signed for output format");
            } else {
                // Always signed, so calculations on the amplitudes are easier.
                this.format.signed = true;
            }
            if (this.format.byteOrder != null) {
                throw new IllegalStateException("Cannot set byte order for output format");
            } else {
                // Always little endian, cause that seems to be more common in audio (wave) files.
                this.format.byteOrder = ByteOrder.LITTLE_ENDIAN;
            }
            if (this.format.sampleRate == null) {
                throw new IllegalStateException("Sample rate is required");
            }
            if (this.format.sampleSize == null) {
                throw new IllegalStateException("Sample size is required");
            }
            this.format.output = true;
            return this.format;
        }
    }

}
