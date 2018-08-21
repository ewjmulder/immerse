package com.programyourhome.immerse.audiostreaming.mixer.step;

import static com.programyourhome.immerse.audiostreaming.mixer.ActiveImmerseSettings.getSettings;

import java.io.IOException;

import org.pmw.tinylog.Logger;

import com.programyourhome.immerse.audiostreaming.mixer.scenario.AudioInputBuffer;
import com.programyourhome.immerse.audiostreaming.util.AudioUtil;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.format.SampleSize;

/**
 * Reads sample values from an audio stream.
 */
public class SampleReader {

    private SampleReader() {
    }

    /**
     * Read as many samples from the audio stream as fit in the provided buffer (or until end of stream).
     * The return boolean indicates if the end of the stream has been reached or not.
     * If so, the remaining samples are set to 0.
     * In case the stream is still open but cannot provide the desired amount of samples, all silence is returned
     * to give the stream some time to 'catch up' without completely blocking Immerse.
     */
    public static boolean readSamples(AudioInputBuffer inputBuffer, short[] sampleBuffer) throws IOException {
        ImmerseAudioFormat format = inputBuffer.getAudioFormat();
        AudioUtil.assertSigned(format);
        boolean endOfStream = false;
        int bytesToRead = sampleBuffer.length * format.getNumberOfBytesPerSample();
        byte[] byteBuffer;
        if (inputBuffer.canRead(bytesToRead)) {
            byteBuffer = inputBuffer.read(bytesToRead);
            // TODO: refactor! Only call update for non-live
            if (!inputBuffer.isLive()) {
                // Update the buffer after a read, so it will be refilled (asynchronously).
                getSettings().submitAsyncTask(() -> inputBuffer.fill());
            }
        } else if (inputBuffer.isStreamClosed()) {
            endOfStream = true;
            byteBuffer = new byte[bytesToRead];
            inputBuffer.readRemaining(byteBuffer);
            // The rest should be silence, so a value of 0. But since that is already the default value for an int in an array, no action is needed.
        } else {
            Logger.warn("AudioInputBuffer doesn't have " + bytesToRead + " available, skipping scenario for this step.");
            // Just initialize a new array with 0's, which means silence.
            byteBuffer = new byte[bytesToRead];
        }

        for (int i = 0; i < sampleBuffer.length; i++) {
            sampleBuffer[i] = readSample(byteBuffer, i, format);
        }

        return endOfStream;
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
