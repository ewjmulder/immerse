package com.programyourhome.immerse.audiostreaming.mixer.scenario;

import static com.programyourhome.immerse.audiostreaming.mixer.ActiveImmerseSettings.getTechnicalSettings;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.audio.resource.StreamConfig;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * An AudioInputBuffer works like a (small) buffer for an AudioInputStream that guarantees
 * non-blocking reads of a certain size and can be refilled asynchronously.
 *
 * It works as follows:
 * - Create it for an audio input stream and buffer length
 * - Fill it
 * - Use canRead and read to get bytes in a non-blocking way
 * - After each read: call align and fill asynchronously.
 * - If canRead returns false, always check if the stream is closed. If so, use readRemaining to get the last bytes out of the buffer.
 * - If not, the fill is not done yet and the AudioInputStream is 'too slow'. It's up to the using class do decide what to do in that case.
 * This way you can always read from the start of the buffer and fill at the end of the buffer at the same time.
 * Read and fill cannot run at the same time as align, but that is a very fast method anyway.
 *
 * Since this buffer implementation is specifically for AudioInputStream, it will enforce a buffer size and read actions of whole frames.
 */
public class AudioInputBuffer {

    // We need 2 locks: one for read and align not running together ...
    private final Object READ_ALIGN_LOCK = new Object();
    // ... and one for fill and align not running together.
    private final Object FILL_ALIGN_LOCK = new Object();

    private final AudioInputStream inputStream;
    private final int frameSize;
    private final StreamConfig streamConfig;
    private int bufferSize;
    private final byte[] buffer;
    private int startPosition;
    private int endPosition;
    private boolean streamClosed;

    public AudioInputBuffer(AudioInputStream inputStream, StreamConfig streamConfig) {
        this.inputStream = inputStream;
        this.frameSize = inputStream.getFormat().getFrameSize();
        this.streamConfig = streamConfig;
        this.bufferSize = this.calculateBufferSize();
        if (this.bufferSize % this.frameSize != 0) {
            // If the buffer size is no multitude of frame size, 'fill it up' so it is.
            this.bufferSize += this.frameSize - this.bufferSize % this.frameSize;
        }
        this.buffer = new byte[this.bufferSize];
        this.streamClosed = false;
    }

    /**
     * Best practice formula for a small but reliable buffer size.
     */
    private int calculateBufferSize() {
        return this.streamConfig.getChunkSize() + 2 * this.streamConfig.getPacketSize() +
                2 * (int) (getTechnicalSettings().getStepPaceMillis() * this.getAudioFormat().getNumberOfBytesPerMilli());
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public ImmerseAudioFormat getAudioFormat() {
        return ImmerseAudioFormat.fromJavaAudioFormat(this.inputStream.getFormat());
    }

    private int getActiveSize() {
        return this.endPosition - this.startPosition;
    }

    public boolean isStreamClosed() {
        return this.streamClosed;
    }

    /**
     * The requested amount of bytes can be read if there are enough available in the buffer.
     * Furthermore, the alignment must be done or the stream must be closed, to not be in the way of a refill.
     */
    public boolean canRead(int numberOfBytes) {
        // Re-align if needed.
        if (this.startPosition > 0) {
            this.align();
        }
        return (this.startPosition == 0 || this.streamClosed) && this.getActiveSize() >= numberOfBytes;
    }

    /**
     * Will read the amount of bytes requested (= buffer length) into the supplied buffer.
     * Guaranteed to return all bytes non-blocking or throw an exception.
     */
    public void read(byte[] toBuffer) {
        int numberOfBytes = toBuffer.length;
        if (!this.canRead(numberOfBytes)) {
            throw new IllegalStateException("Cannot read " + numberOfBytes + " bytes");
        }
        if (numberOfBytes % this.frameSize != 0) {
            throw new IllegalStateException("Cannot read a fraction of a frame");
        }
        synchronized (this.READ_ALIGN_LOCK) {
            System.arraycopy(this.buffer, this.startPosition, toBuffer, 0, numberOfBytes);
            this.startPosition += numberOfBytes;
        }
    }

    /**
     * Read the remaining bytes out of the buffer after the wrapped stream is closed.
     * This method can be called multiple times as long as there are still bytes left in the input buffer.
     * It returns the amount of bytes read and if that is smaller than the toBuffer size, the input buffer will be empty.
     */
    public int readRemaining(byte[] toBuffer) {
        if (!this.streamClosed) {
            throw new IllegalStateException("Read remaining only supported when wrapped stream is closed");
        }
        // Copy either the full toBuffer length or the active size if there are less bytes available.
        int amountToRead = Math.min(toBuffer.length, this.getActiveSize());
        synchronized (this.READ_ALIGN_LOCK) {
            System.arraycopy(this.buffer, this.startPosition, toBuffer, 0, amountToRead);
            this.startPosition += amountToRead;
        }
        return amountToRead;
    }

    /**
     * Refill the buffer by reading bytes from the wrapped stream.
     * Tries to read from the wrapped stream until either the buffer is full
     * or the end of the stream has been reached.
     */
    public int fill() {
        // Re-align if needed.
        if (this.startPosition > 0) {
            this.align();
        }
        try {
            int amountNeeded;
            if (this.streamConfig.isLive()) {
                // For live streams: just read one packet at a time, independent of the current position.
                amountNeeded = this.streamConfig.getPacketSize();
            } else {
                // Non-live streams: we need the amount of bytes to fill the whole buffer.
                amountNeeded = this.bufferSize - this.endPosition;
            }
            int totalAmountRead = 0;
            // Use a loop here, because the contract of InputStream.read() does not force the implementation to return a full buffer, even when the stream
            // is still open. So we have to loop until either the stream is closed or the buffer is full.
            while (!this.streamClosed && totalAmountRead < amountNeeded) {
                int amountRead;
                if (this.streamConfig.isLive()) {
                    // For live streams: read into a separate buffer.
                    byte[] packetBuffer = new byte[amountNeeded];
                    amountRead = this.inputStream.read(packetBuffer);
                    if (amountRead > 0) {
                        if (amountRead <= this.bufferSize - this.endPosition) {
                            // If there is enough room in the main buffer, append the bytes there.
                            synchronized (this.FILL_ALIGN_LOCK) {
                                System.arraycopy(packetBuffer, 0, this.buffer, this.endPosition, amountRead);
                            }
                        } else {
                            // If the packet bytes do not fit, just discard them by pretending nothing was read.
                            // This will make the live stream stay as close to 'live' as possible.
                            amountRead = 0;
                        }
                    }
                } else {
                    // Non-live streams: read directly into the main buffer.
                    synchronized (this.FILL_ALIGN_LOCK) {
                        amountRead = this.inputStream.read(this.buffer, this.endPosition, amountNeeded - totalAmountRead);
                    }
                }
                synchronized (this.FILL_ALIGN_LOCK) {
                    if (amountRead == -1) {
                        this.streamClosed = true;
                        this.inputStream.close();
                    } else {
                        totalAmountRead += amountRead;
                        this.endPosition += amountRead;
                    }
                }
            }
            return totalAmountRead;
        } catch (IOException e) {
            throw new IllegalStateException("IOException during fill", e);
        }
    }

    /**
     * Align the buffer, that is: move all 'active' bytes to the front of the buffer.
     * This allows the next cycle of read & fill to take place simultaneously without having to resort to complicated wrapping of indices.
     */
    private int align() {
        synchronized (this.READ_ALIGN_LOCK) {
            synchronized (this.FILL_ALIGN_LOCK) {
                int amount = this.getActiveSize();
                System.arraycopy(this.buffer, this.startPosition, this.buffer, 0, amount);
                this.startPosition = 0;
                this.endPosition = amount;
                return amount;
            }
        }
    }

}
