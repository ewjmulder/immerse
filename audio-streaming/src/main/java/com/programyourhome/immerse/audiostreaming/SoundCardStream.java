package com.programyourhome.immerse.audiostreaming;

import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.audiostreaming.format.ImmerseAudioFormat;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

public class SoundCardStream {

    private final SoundCard soundCard;
    private final SourceDataLine outputLine;
    private final ImmerseAudioFormat outputFormat;
    private long framesWritten;

    public SoundCardStream(SoundCard soundCard, SourceDataLine outputLine) {
        this.soundCard = soundCard;
        this.outputLine = outputLine;
        this.outputFormat = ImmerseAudioFormat.fromJavaAudioFormat(this.outputLine.getFormat());
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
        double framesPerMilli = this.outputFormat.getNumberOfFramesPerSecond() / 1000.0;

        double amountOfFramesToBuffer = bufferMillis * framesPerMilli;
        double amountOfFramesAhead = this.framesWritten - this.outputLine.getFramePosition();
        long amountOfFramesNeeded = Math.round(amountOfFramesToBuffer - amountOfFramesAhead);
        return amountOfFramesNeeded;
    }

    public void writeToLine(byte[] buffer) {
        this.outputLine.write(buffer, 0, buffer.length);
        this.framesWritten += buffer.length / this.outputFormat.getNumberOfBytesPerFrame();
    }

}
