package com.programyourhome.immerse.domain.audio.soundcard;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

public class SoundCardStream {

    private final SourceDataLine outputLine;
    private final AudioFormat inputFormat;
    private final AudioFormat outputFormat;
    private long nextFrameToWrite;
    private final SoundCardSpeakers soundCardSpeakers;

    public SoundCardStream(AudioFormat inputFormat, SourceDataLine outputLine, SoundCardSpeakers soundCardSpeakers) {
        this.inputFormat = inputFormat;
        this.outputLine = outputLine;
        this.soundCardSpeakers = soundCardSpeakers;

        this.outputFormat = this.outputLine.getFormat();
        this.nextFrameToWrite = 0;
    }

    public SourceDataLine getOutputLine() {
        return this.outputLine;
    }

    public AudioFormat getInputFormat() {
        return this.inputFormat;
    }

    public AudioFormat getOutputFormat() {
        return this.outputFormat;
    }

    public void start() {
        this.outputLine.start();
    }

    public boolean isDone(byte[] inputBuffer) {
        return this.nextFrameToWrite * this.inputFormat.getFrameSize() > inputBuffer.length;
    }

    public void stop() {
        this.outputLine.stop();
        this.outputLine.close();
    }

    public void update(byte[] inputBuffer, int bufferMillis, SpeakerVolumes speakerVomules) {
        int framesPerMilli = (int) (this.inputFormat.getSampleRate() / 1000);

        long amountOfFramesToBuffer = bufferMillis * framesPerMilli;
        long amountOfFramesAhead = this.nextFrameToWrite - this.outputLine.getFramePosition();
        long amountOfFramesNeeded = amountOfFramesToBuffer - amountOfFramesAhead;
        long toFrame = this.nextFrameToWrite + amountOfFramesNeeded;
        long millisLeftInBuffer = (amountOfFramesToBuffer - amountOfFramesNeeded) / framesPerMilli;
        // TODO: should never be 0, warning if it is.
        if (amountOfFramesNeeded > 0) {
            int outputBufferSize = (int) (amountOfFramesNeeded * this.outputFormat.getFrameSize());
            // System.out.println("Chunk size: " + nextChunkSize);
            byte[] outputBuffer = new byte[outputBufferSize];
            // TODO: test if this prevents arrayindexoutofbounds
            for (long frame = this.nextFrameToWrite; frame < toFrame && frame * this.inputFormat.getFrameSize() < inputBuffer.length; frame++) {
                // TODO: support for partial input buffers
                int startByteInInputBuffer = (int) (this.inputFormat.getFrameSize() * frame);
                int startByteInOutputBuffer = (int) (this.outputFormat.getFrameSize() * (frame - this.nextFrameToWrite));

                double volumeFractionSpeakerLeft = speakerVomules.getVolume(this.soundCardSpeakers.getSpeakerIdLeft());
                double volumeFractionSpeakerRight = speakerVomules.getVolume(this.soundCardSpeakers.getSpeakerIdRight());
                // TODO: prevent arrayindexoutofbounds at end of stream
                byte[] frameBytes = this.calculateFrameBytes(inputBuffer, startByteInInputBuffer, volumeFractionSpeakerLeft, volumeFractionSpeakerRight);
                System.arraycopy(frameBytes, 0, outputBuffer, startByteInOutputBuffer, frameBytes.length);
            }
            this.outputLine.write(outputBuffer, 0, outputBuffer.length);
            this.nextFrameToWrite = toFrame;
        }
    }

    private byte[] calculateFrameBytes(byte[] inputBuffer, int startByteInInputBuffer,
            double volumeFractionSpeakerLeft, double volumeFractionSpeakerRight) {
        byte[] inputSample = new byte[2];
        System.arraycopy(inputBuffer, startByteInInputBuffer, inputSample, 0, 2);
        // Convert the 2 sample bytes to the amplitude value they represent.
        short inpuAmplitude = ByteBuffer.wrap(inputSample).order(this.inputFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort();

        short leftAmplitude = (short) (inpuAmplitude * volumeFractionSpeakerLeft);
        short rightAmplitude = (short) (inpuAmplitude * volumeFractionSpeakerRight);

        // Convert the left and right calculated amplitude values back to bytes.
        final byte[] outputFrameBytes = new byte[4];
        // TODO: assume/force output format is always PCM signed / big endian?
        if (this.outputFormat.isBigEndian()) {
            outputFrameBytes[0] = (byte) (leftAmplitude >> 8 & 0xFF);
            outputFrameBytes[1] = (byte) (leftAmplitude & 0xFF);
            outputFrameBytes[2] = (byte) (rightAmplitude >> 8 & 0xFF);
            outputFrameBytes[3] = (byte) (rightAmplitude & 0xFF);
        } else {
            outputFrameBytes[0] = (byte) (leftAmplitude & 0xFF);
            outputFrameBytes[1] = (byte) (leftAmplitude >> 8 & 0xff);
            outputFrameBytes[2] = (byte) (rightAmplitude & 0xFF);
            outputFrameBytes[3] = (byte) (rightAmplitude >> 8 & 0xff);
        }
        return outputFrameBytes;
    }

}
