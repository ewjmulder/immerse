package com.programyourhome.immerse.audiostreaming.generate;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.RecordingMode;
import com.programyourhome.immerse.domain.format.SampleSize;
import com.programyourhome.immerse.toolbox.util.MathUtil;

/**
 * Audio input stream generator that can produce sine waves of a certain frequency.
 */
public class SineWaveAudioInputStreamGenerator implements AudioInputStreamGenerator {

    private final ImmerseAudioFormat format;
    private final int frequency;
    private final long lengthInMillis;

    /**
     * Create a sine wave generator for the given audio format.
     * Currently only supports mono with signed, one byte samples. Sample rate can be varied.
     */
    public SineWaveAudioInputStreamGenerator(ImmerseAudioFormat format, int frequency, long lengthInMillis) {
        if (format.getRecordingMode() != RecordingMode.MONO) {
            throw new IllegalArgumentException("Only recording mode mono is supported");
        }
        if (!format.isSigned()) {
            throw new IllegalArgumentException("Unsigned samples not supported");
        }
        if (format.getSampleSize() != SampleSize.ONE_BYTE) {
            throw new IllegalArgumentException("Only one byte samples are supported");
        }
        this.format = format;
        this.frequency = frequency;
        this.lengthInMillis = lengthInMillis;
    }

    @Override
    public AudioInputStream generate() {
        // Calculate the amount of frames needed.
        int lengthInFrames = (int) (this.lengthInMillis / 1000.0 * this.format.getNumberOfFramesPerSecond());
        // Prepare a byte array of the right size.
        byte[] bytes = new byte[lengthInFrames * this.format.getNumberOfBytesPerFrame()];
        // Fill the bytes with audio data.
        fillBytes(this.format, bytes, this.frequency);
        // Create an audio stream of the bytes with the right format and length.
        return new AudioInputStream(new ByteArrayInputStream(bytes), this.format.toJavaAudioFormat(), lengthInFrames);
    }

    /**
     * Fill the byte array with the amplitudes of the configured wave frequency.
     */
    private static void fillBytes(ImmerseAudioFormat format, byte[] bytes, int frequency) {
        double waveLengthInMillis = 1.0 / frequency * 1000;
        int lengthInFrames = bytes.length / format.getNumberOfBytesPerFrame();
        for (int frame = 0; frame < lengthInFrames; frame++) {
            double elapsedTimeInMillis = frame / (double) format.getNumberOfFramesPerSecond() * 1000;
            // Calculate where we 'are' in the sine wave - a value between 0 (start of wave) and 1 - (and of wave).
            double fractionOfWave = elapsedTimeInMillis % waveLengthInMillis / waveLengthInMillis;
            // Calculate the amplitude in the 'unity sine wave' - a value between -1 and 1.
            double amplitudeNormalized = Math.sin(fractionOfWave * 2 * Math.PI);
            // Calculate the byte value of the normalized amplitude, in the range of the possible values for byte.
            double amplitude = MathUtil.calculateFromRangeToRange(-1, 1, amplitudeNormalized, Byte.MIN_VALUE, Byte.MAX_VALUE);
            // Default to half the max volume.
            amplitude *= 0.5;
            // Only one byte mono supported, so byte index = frame index.
            bytes[frame] = (byte) amplitude;
        }
    }

}
