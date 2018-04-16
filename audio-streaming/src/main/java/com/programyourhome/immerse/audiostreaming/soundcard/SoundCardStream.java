package com.programyourhome.immerse.audiostreaming.soundcard;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.RecordingMode;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

/**
 * This class represents an open stream from the mixer to the actual sound card hardware (through the Java Sound API).
 * It can start and stop the streaming and can write 'raw' bytes to the sound card, that should match the format of the audio line.
 */
public class SoundCardStream {

    private final SoundCard soundCard;
    private final SourceDataLine outputLine;
    private final ImmerseAudioFormat outputFormat;
    private final double framesPerMilli;
    private long framesWritten;
    private boolean mutedLeft;
    private boolean mutedRight;

    public SoundCardStream(SoundCard soundCard, SourceDataLine outputLine) {
        this.soundCard = soundCard;
        this.outputLine = outputLine;
        this.outputFormat = ImmerseAudioFormat.fromJavaAudioFormat(this.outputLine.getFormat());
        if (this.outputFormat.getRecordingMode() != RecordingMode.STEREO) {
            throw new IllegalArgumentException("Only recording mode stereo is supported");
        }
        if (!this.outputFormat.isSigned()) {
            throw new IllegalArgumentException("Only signed samples are supported");
        }
        this.framesPerMilli = this.outputFormat.getNumberOfFramesPerSecond() / 1000.0;
        this.framesWritten = 0;
        this.mutedLeft = false;
        this.mutedRight = false;
    }

    public SoundCard getSoundCard() {
        return this.soundCard;
    }

    public void open() {
        try {
            this.outputLine.open();
        } catch (LineUnavailableException e) {
            throw new IllegalStateException("Line unavailable", e);
        }
    }

    public void start() {
        this.outputLine.start();
    }

    public void stop() {
        this.outputLine.stop();
        this.outputLine.close();
    }

    /**
     * Calculate the amount of frames needed for this sound card given the amount of millis that
     * should be present in the buffer. The calculation is based upon the difference between the amount of
     * frames that should be in the buffer and the amount of frames that are actually still in the buffer.
     * That last value is given by the hardware through the DataLine.getLongFramePosition call. The return value of that call
     * is known to be far from exact, mostly updating about every 5 milliseconds and sometimes even giving a
     * smaller number upon subsequent invocation. The exact behavior can be different for different sound card hardware.
     * This is taken into account in the implementation and is found to be good enough for our goal:
     * giving a good estimation of the amount of frames needed to fill up the buffer.
     */
    public long getAmountOfFramesNeeded(int bufferMillis) {
        double amountOfFramesToBuffer = bufferMillis * this.framesPerMilli;
        long lineFramePosition = this.outputLine.getLongFramePosition();
        double amountOfFramesAhead = this.framesWritten - lineFramePosition;
        long amountOfFramesNeeded = Math.round(amountOfFramesToBuffer - amountOfFramesAhead);

        // Sanity check: amount of frames needed should never be negative.
        // Unfortunately, this sometimes happens with certain hardware or thread race conditions.
        return Math.max(0, amountOfFramesNeeded);
    }

    /**
     * Mute this sound card, both left and right channels.
     */
    public void mute() {
        this.muteLeft();
        this.muteRight();
    }

    /**
     * Mute the left channel of this sound card.
     */
    public void muteLeft() {
        this.mutedLeft = true;
    }

    /**
     * Mute the right channel of this sound card.
     */
    public void muteRight() {
        this.mutedRight = true;
    }

    /**
     * Unmute this sound card, both left and right channels.
     */
    public void unMute() {
        this.unMuteLeft();
        this.unMuteRight();
    }

    /**
     * Unmute the left channel of this sound card.
     */
    public void unMuteLeft() {
        this.mutedLeft = false;
    }

    /**
     * Unmute the right channel of this sound card.
     */
    public void unMuteRight() {
        this.mutedRight = false;
    }

    /**
     * Write the contents of the given buffer to the sound card, taking mute settings into account.
     */
    public void writeToLine(byte[] buffer) {
        if (this.mutedLeft) {
            this.mute(buffer, 0);
        }
        if (this.mutedRight) {
            this.mute(buffer, this.outputFormat.getNumberOfBytesPerSample());
        }
        // This performs the actual I/O on the sound card hardware.
        this.outputLine.write(buffer, 0, buffer.length);
        // Update the frames written by calculating how many were in the byte array.
        this.framesWritten += buffer.length / this.outputFormat.getNumberOfBytesPerFrame();
    }

    /**
     * Mute one channel of the byte buffer from the given start index, taking into account sample size.
     */
    protected void mute(byte[] buffer, int startIndex) {
        for (int i = startIndex; i < buffer.length; i += this.outputFormat.getNumberOfBytesPerFrame()) {
            // Muting means setting to 0, since we have validated the format is using signed samples.
            buffer[i] = 0;
            if (this.outputFormat.getSampleSize() == SampleSize.TWO_BYTES) {
                buffer[i + 1] = 0;
            }
        }
    }

}
