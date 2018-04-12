package com.programyourhome.immerse.audiostreaming.soundcard;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.audiostreaming.format.SampleSize;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

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

    public long getAmountOfFramesNeeded(int bufferMillis) {
        double amountOfFramesToBuffer = bufferMillis * this.framesPerMilli;
        long lineFramePosition = this.outputLine.getLongFramePosition();
        double amountOfFramesAhead = this.framesWritten - lineFramePosition;
        long amountOfFramesNeeded = Math.round(amountOfFramesToBuffer - amountOfFramesAhead);

        // Sanity check: amount of frames needed should never be negative.
        // Unfortunately, this sometimes happens with certain hardware or thread race conditions.
        return Math.max(0, amountOfFramesNeeded);
    }

    public void mute() {
        this.muteLeft();
        this.muteRight();
    }

    private void muteLeft() {
        this.mutedLeft = true;
    }

    private void muteRight() {
        this.mutedRight = true;
    }

    public void unMute() {
        this.mutedLeft = false;
        this.mutedRight = false;
    }

    // TODO: use thread pool!
    public void writeToLine(byte[] buffer) {
        new Thread(() -> {
            if (this.mutedLeft) {
                this.mute(buffer, 0);
            }
            if (this.mutedRight) {
                this.mute(buffer, this.outputFormat.getNumberOfBytesPerSample());
            }
            this.outputLine.write(buffer, 0, buffer.length);
            this.framesWritten += buffer.length / this.outputFormat.getNumberOfBytesPerFrame();
        }).start();
    }

    // TODO: unit test
    protected void mute(byte[] buffer, int startIndex) {
        for (int i = startIndex; i < buffer.length; i += this.outputFormat.getNumberOfBytesPerFrame()) {
            buffer[i] = 0;
            if (this.outputFormat.getSampleSize() == SampleSize.TWO_BYTES) {
                buffer[i + 1] = 0;
            }
        }
    }

}
