package com.programyourhome.immerse.audiostreaming.format;

import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class ImmerseAudioFormat {

    private boolean output;
    private RecordingMode recordingMode;
    private SampleRate sampleRate;
    private SampleSize sampleSize;
    private Boolean signed;
    private ByteOrder byteOrder;

    private ImmerseAudioFormat() {
    }

    public boolean isOutput() {
        return this.output;
    }

    public RecordingMode getRecordingMode() {
        return this.recordingMode;
    }

    public SampleRate getSampleRate() {
        return this.sampleRate;
    }

    public SampleSize getSampleSize() {
        return this.sampleSize;
    }

    public boolean isSigned() {
        return this.signed;
    }

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

    public int getNumberOfBytesPerSecond() {
        return this.sampleRate.getNumberOfSamplesPerSecond() * this.getNumberOfBytesPerFrame();
    }

    /**
     * The number of frames per second. Since we are using a non-compressed format, the frame rate is the same as the sample rate.
     *
     * @return number of frames per second
     */
    public int getNumberOfFramesPerSecond() {
        return this.sampleRate.getNumberOfSamplesPerSecond();
    }

    public AudioFormat toJavaAudioFormat() {
        return new AudioFormat(this.sampleRate.getNumberOfSamplesPerSecond(), this.sampleSize.getNumberOfBits(),
                this.recordingMode.getNumberOfChannels(), this.signed, this.byteOrder == ByteOrder.BIG_ENDIAN);
    }

    // TODO: nice for a unit test
    public static void main(String[] args) {
        ImmerseAudioFormat original = ImmerseAudioFormat.builder()
                .recordingMode(RecordingMode.STEREO)
                .sampleRate(SampleRate.RATE_44K)
                .sampleSize(SampleSize.TWO_BYTES)
                .signed()
                .byteOrder(ByteOrder.LITTLE_ENDIAN)
                .buildForInput();
        ImmerseAudioFormat doubleConverted = ImmerseAudioFormat.fromJavaAudioFormat(original.toJavaAudioFormat());
        System.out.println(original);
        System.out.println(doubleConverted);
    }

    public static ImmerseAudioFormat fromJavaAudioFormat(AudioFormat audioFormat) {
        return builder()
                // TODO: move fromXxx calls to builder overload methods
                .recordingMode(RecordingMode.fromNumberOfChannels(audioFormat.getChannels()))
                .sampleRate(SampleRate.fromNumberOfSamplesPerSecond(audioFormat.getSampleRate()))
                .sampleSize(SampleSize.fromNumberOfBits(audioFormat.getSampleSizeInBits()))
                .setSigned(encodingToSigned(audioFormat.getEncoding()))
                .byteOrder(audioFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN)
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

        public Builder recordingMode(RecordingMode recordingMode) {
            this.format.recordingMode = recordingMode;
            return this;
        }

        public Builder sampleRate(SampleRate sampleRate) {
            this.format.sampleRate = sampleRate;
            return this;
        }

        public Builder sampleSize(SampleSize sampleSize) {
            this.format.sampleSize = sampleSize;
            return this;
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
                // Always stereo mode, because we want to supply output to both channels.
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
