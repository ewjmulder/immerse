package com.programyourhome.immerse.audiostreaming.util;

import java.io.ByteArrayInputStream;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.RecordingMode;
import com.programyourhome.immerse.audiostreaming.format.SampleRate;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.toolbox.util.MathUtil;

//TODO: refactor to class with interface: state is not the only reason to use objects, so are testability and extendability!
public class AudioInputStreamGenerator {

    private AudioInputStreamGenerator() {
    }

    public static AudioInputStream generate(ImmerseAudioFormat format, int frequency, long lengthInMillis) {
        if (format.getRecordingMode() == RecordingMode.STEREO) {
            throw new IllegalArgumentException("Recording mode stereo not supported");
        }
        if (!format.isSigned()) {
            throw new IllegalArgumentException("Unsigned samples not supported");
        }
        if (format.getSampleSize() == SampleSize.TWO_BYTES) {
            throw new IllegalArgumentException("Two byte samples not supported");
        }
        int lengthInFrames = (int) (lengthInMillis / 1000.0 * format.getNumberOfFramesPerSecond());
        byte[] bytes = new byte[lengthInFrames * format.getNumberOfBytesPerFrame()];
        fillBytes(format, bytes, frequency);
        return new AudioInputStream(new ByteArrayInputStream(bytes), format.toJavaAudioFormat(), lengthInFrames);
    }

    private static void fillBytes(ImmerseAudioFormat format, byte[] bytes, int frequency) {
        double waveLengthInMillis = 1.0 / frequency * 1000;
        int lengthInFrames = bytes.length / format.getNumberOfBytesPerFrame();
        for (int frame = 0; frame < lengthInFrames; frame++) {
            double elapsedTimeInMillis = frame / (double) format.getNumberOfFramesPerSecond() * 1000;
            double fractionOfWave = elapsedTimeInMillis % waveLengthInMillis / waveLengthInMillis;
            double amplitudeNormalized = Math.sin(fractionOfWave * 2 * Math.PI);
            double amplitudeFraction = MathUtil.calculateFractionInRange(-1, 1, amplitudeNormalized);
            double amplitude = MathUtil.calculateValueInRange(Byte.MIN_VALUE, Byte.MAX_VALUE, amplitudeFraction);
            // Default to half the max volume.
            amplitude *= 0.5;
            // Only one byte mono supported, so byte index = frame index.
            bytes[frame] = (byte) amplitude;
        }
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[8000];
        ImmerseAudioFormat format = ImmerseAudioFormat.builder()
                .sampleRate(SampleRate.RATE_8K)
                .sampleSize(SampleSize.ONE_BYTE)
                .byteOrder(ByteOrder.LITTLE_ENDIAN)
                .recordingMode(RecordingMode.MONO)
                .signed()
                .buildForInput();
        fillBytes(format, bytes, 500);
    }

}
