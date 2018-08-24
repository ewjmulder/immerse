package com.programyourhome.immerse.audiostreaming.mixer.step;

import java.io.IOException;

import com.programyourhome.immerse.audiostreaming.util.AudioUtil;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.SampleSize;

/**
 * Reads sample values from a byte array into a sample array.
 */
public class SampleReader {

    private SampleReader() {
    }

    /**
     * Reads as many samples as are present in the byte array into a sample array.
     */
    public static short[] readSamples(byte[] byteBuffer, ImmerseAudioFormat format) throws IOException {
        short[] sampleBuffer = new short[byteBuffer.length / format.getNumberOfBytesPerSample()];
        for (int i = 0; i < sampleBuffer.length; i++) {
            sampleBuffer[i] = readSample(byteBuffer, i, format);
        }
        return sampleBuffer;
    }

    /**
     * Read one sample from the given byte array at the given index.
     */
    public static short readSample(byte[] byteBuffer, int sampleIndex, ImmerseAudioFormat format) {
        AudioUtil.assertSigned(format);
        short sampleValue;
        int byteIndex = sampleIndex * format.getNumberOfBytesPerSample();
        if (format.getSampleSize() == SampleSize.ONE_BYTE) {
            // Simple case, just return the 1 byte as a sample.
            sampleValue = byteBuffer[byteIndex];
        } else {
            if (format.isBigEndian()) {
                // Read 2 bytes in big endian order as a sample.
                sampleValue = (short) ((byteBuffer[byteIndex] & 0xFF) << 8 | byteBuffer[byteIndex + 1] & 0xFF);
            } else {
                // Read 2 bytes in little endian order as a sample.
                sampleValue = (short) (byteBuffer[byteIndex] & 0xFF | (byteBuffer[byteIndex + 1] & 0xFF) << 8);
            }
        }
        return sampleValue;
    }

}
