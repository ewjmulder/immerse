package com.programyourhome.immerse.audiostreaming.model.audioresource;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class InputStreamAudioResource implements AudioResource {

    private InputStream inputStream;

    protected InputStreamAudioResource(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public AudioInputStream getAudioStream() throws IOException {
        AudioInputStream audioInputStream;
        if (this.inputStream instanceof AudioInputStream) {
            // Special case: the input stream already is an audio input stream.
            // No need to wrap it any further then.
            audioInputStream = (AudioInputStream) this.inputStream;
        } else {
            try {
                audioInputStream = AudioSystem.getAudioInputStream(this.inputStream);
            } catch (UnsupportedAudioFileException e) {
                throw new IOException(e);
            }
        }
        return audioInputStream;
    }

}
