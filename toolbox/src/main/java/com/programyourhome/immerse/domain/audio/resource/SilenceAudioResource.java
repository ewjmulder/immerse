package com.programyourhome.immerse.domain.audio.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * An audio resource that only provides silence.
 */
public class SilenceAudioResource implements AudioResource {

    private final AudioInputStream audioInputStream;

    public SilenceAudioResource() {
        this.audioInputStream = new AudioInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        }, new AudioFormat(8000, 8, 1, true, false), Long.MAX_VALUE);
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    public static Supplier<AudioResource> silence() {
        return () -> new SilenceAudioResource();
    }

}
