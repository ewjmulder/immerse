package com.programyourhome.immerse.audiostreaming;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;

public class SampleReader {

    private SampleReader() {
    }

    public static boolean readSamples(AudioInputStream stream, short[] sampleBuffer) throws IOException {
        ImmerseAudioFormat format = ImmerseAudioFormat.fromJavaAudioFormat(stream.getFormat());
        boolean endOfStream = false;

        int bytesToRead = sampleBuffer.length * format.getNumberOfBytesPerSample();
        byte[] byteBuffer = new byte[bytesToRead];
        int bytesRead = stream.read(byteBuffer);
        if (bytesRead < byteBuffer.length) {
            // Corner case: exactly at end of stream upon read action.
            if (bytesRead == -1) {
                bytesRead = 0;
            }
            endOfStream = true;
            // Pad the rest of the array with 0's to create silence.
            Arrays.fill(byteBuffer, bytesRead, byteBuffer.length, (byte) 0);
        }
        for (int i = 0; i < sampleBuffer.length; i++) {
            sampleBuffer[i] = readSample(byteBuffer, i, format);
        }

        return endOfStream;
    }

    public static short readSample(byte[] byteBuffer, int sampleIndex, ImmerseAudioFormat format) {
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
