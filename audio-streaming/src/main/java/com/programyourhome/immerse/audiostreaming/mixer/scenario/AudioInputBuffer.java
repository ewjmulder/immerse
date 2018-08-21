package com.programyourhome.immerse.audiostreaming.mixer.scenario;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;

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
    private final int bufferSize;
    private final boolean live;
    private final int stepSize;
    private final byte[] buffer;
    private int startPosition;
    private int endPosition;
    private boolean streamClosed;

    public AudioInputBuffer(AudioInputStream inputStream, int bufferSize) {
        this(inputStream, bufferSize, false, -1);
    }

    public AudioInputBuffer(AudioInputStream inputStream, int bufferSize, boolean live, int stepSize) {
        this.inputStream = inputStream;
        this.frameSize = inputStream.getFormat().getFrameSize();
        if (bufferSize % this.frameSize != 0) {
            throw new IllegalArgumentException("Buffer size must be a multitude of frame size");
        }
        if (live && stepSize < 1) {
            throw new IllegalArgumentException("A live audio stream must have a positive step size");
        }
        this.live = live;
        this.stepSize = stepSize;
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];
        this.streamClosed = false;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public ImmerseAudioFormat getAudioFormat() {
        return ImmerseAudioFormat.fromJavaAudioFormat(this.inputStream.getFormat());
    }

    public boolean isLive() {
        return this.live;
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
     * Will read the amount of bytes requested from the buffer and return them.
     * Guaranteed to return all bytes non-blocking or throw an exception.
     */
    public byte[] read(int numberOfBytes) {
        if (!this.canRead(numberOfBytes)) {
            throw new IllegalStateException("Cannot read " + numberOfBytes + " bytes");
        }
        if (numberOfBytes % this.frameSize != 0) {
            throw new IllegalStateException("Cannot read a fraction of a frame");
        }
        byte[] toBuffer = new byte[numberOfBytes];
        synchronized (this.READ_ALIGN_LOCK) {
            System.arraycopy(this.buffer, this.startPosition, toBuffer, 0, numberOfBytes);
            this.startPosition += numberOfBytes;
        }
        return toBuffer;
    }

    /**
     * Read the remaining bytes out of the buffer after the wrapped stream is closed.
     */
    public int readRemaining(byte[] toBuffer) {
        synchronized (this.READ_ALIGN_LOCK) {
            if (!this.streamClosed) {
                throw new IllegalStateException("Read remaining only supported when wrapped stream is closed");
            }
            int amount = this.getActiveSize();
            if (this.buffer.length < amount) {
                throw new IllegalStateException("Buffer should be big enough for remaining bytes");
            }
            System.arraycopy(this.buffer, this.startPosition, toBuffer, 0, amount);
            this.startPosition += amount;
            return amount;
        }
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
            if (this.live) {
                // For live streams: just read one step at a time, independent of the current position.
                amountNeeded = this.stepSize;
            } else {
                // Non-live streams: we need the amount of bytes to fill the whole buffer.
                amountNeeded = this.bufferSize - this.endPosition;
            }
            int totalAmountRead = 0;
            // Use a loop here, because the contract of InputStream.read() does not force the implementation to return a full buffer, even when the stream
            // is still open. So we have to loop until either the stream is closed or the buffer is full.
            while (!this.streamClosed && totalAmountRead < amountNeeded) {
                int amountRead;
                if (this.live) {
                    // For live streams: read into a separate buffer.
                    byte[] stepBuffer = new byte[this.stepSize];
                    amountRead = this.inputStream.read(stepBuffer);
                    if (amountRead > 0) {
                        if (amountRead <= this.bufferSize - this.endPosition) {
                            // If there is enough room in the main buffer, append the bytes there.
                            synchronized (this.FILL_ALIGN_LOCK) {
                                System.arraycopy(stepBuffer, 0, this.buffer, this.endPosition, amountRead);
                            }
                        } else {
                            // If the step bytes do not fit, just discard them by pretending nothing was read.
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
