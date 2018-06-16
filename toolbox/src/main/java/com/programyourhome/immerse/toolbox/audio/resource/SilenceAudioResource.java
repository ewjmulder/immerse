package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource that only provides silence.
 */
public class SilenceAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final InputStream inputStream;

    public SilenceAudioResource() {
        this.inputStream = new AudioInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        }, new AudioFormat(8000, 8, 1, true, false), Long.MAX_VALUE);
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    public static Factory<AudioResource> silence() {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new SilenceAudioResource();
            }
        };
    }

}
