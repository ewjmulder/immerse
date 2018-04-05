package com.programyourhome.immerse.audiostreaming;

import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

public class SoundCardStream {

    private final SoundCard soundCard;
    private final SourceDataLine outputLine;
    private final ImmerseAudioFormat outputFormat;
    private final double framesPerMilli;
    private long framesWritten;

    public SoundCardStream(SoundCard soundCard, SourceDataLine outputLine) {
        this.soundCard = soundCard;
        this.outputLine = outputLine;
        this.outputFormat = ImmerseAudioFormat.fromJavaAudioFormat(this.outputLine.getFormat());
        this.framesPerMilli = this.outputFormat.getNumberOfFramesPerSecond() / 1000.0;
        this.framesWritten = 0;
    }

    public SoundCard getSoundCard() {
        return this.soundCard;
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

    // TODO: use thread pool!
    public void writeToLine(byte[] buffer) {
        new Thread(() -> {
            this.outputLine.write(buffer, 0, buffer.length);
            this.framesWritten += buffer.length / this.outputFormat.getNumberOfBytesPerFrame();
        }).start();
    }

}
