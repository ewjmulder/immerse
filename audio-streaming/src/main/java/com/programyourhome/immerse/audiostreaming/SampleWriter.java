package com.programyourhome.immerse.audiostreaming;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;

public class SampleWriter {

    private SampleWriter() {
    }

    public static void writeSamples(short[] samples, byte[] outputBuffer, ImmerseAudioFormat format) {
        int bytesToWrite = samples.length * format.getNumberOfBytesPerSample();
        if (outputBuffer.length != bytesToWrite) {
            throw new IllegalArgumentException(
                    "Expected number of bytes to write: '" + bytesToWrite + "' does not match buffer size: '" + outputBuffer.length + "'");
        }
        for (int i = 0; i < samples.length; i++) {
            writeSample(samples[i], outputBuffer, i, format);
        }
    }

    public static void writeSample(short sample, byte[] outputBuffer, int sampleIndex, ImmerseAudioFormat format) {
        int byteIndex = sampleIndex * format.getNumberOfBytesPerSample();
        if (format.getSampleSize() == SampleSize.ONE_BYTE) {
            // Simple case, just write the sample as 1 byte.
            outputBuffer[byteIndex] = (byte) sample;
        } else {
            if (format.isBigEndian()) {
                // Write the sample as 2 bytes in big endian order in the buffer.
                outputBuffer[byteIndex] = (byte) (sample >> 8 & 0xFF);
                outputBuffer[byteIndex + 1] = (byte) (sample & 0xFF);
            } else {
                // Write the sample as 2 bytes in little endian order in the buffer.
                outputBuffer[byteIndex] = (byte) (sample & 0xFF);
                outputBuffer[byteIndex + 1] = (byte) (sample >> 8 & 0xff);
            }
        }
    }

}
