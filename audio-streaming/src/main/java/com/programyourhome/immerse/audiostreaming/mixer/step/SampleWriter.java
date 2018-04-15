package com.programyourhome.immerse.audiostreaming.mixer.step;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.audiostreaming.util.AudioUtil;

/**
 * Write sample values from a samples array to a byte array.
 */
public class SampleWriter {

    private SampleWriter() {
    }

    /**
     * Writes as many samples as are present in the sample array into a byte array.
     */
    public static byte[] writeSamples(short[] samples, ImmerseAudioFormat format) {
        AudioUtil.assertSigned(format);
        byte[] outputBuffer = new byte[samples.length * format.getNumberOfBytesPerSample()];
        for (int i = 0; i < samples.length; i++) {
            writeSample(samples[i], outputBuffer, i, format);
        }
        return outputBuffer;
    }

    /**
     * Write one sample into the given byte array at the given index.
     */
    public static void writeSample(short sample, byte[] outputBuffer, int sampleIndex, ImmerseAudioFormat format) {
        AudioUtil.assertSigned(format);
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
