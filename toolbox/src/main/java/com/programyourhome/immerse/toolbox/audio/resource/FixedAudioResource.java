package com.programyourhome.immerse.toolbox.audio.resource;

import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource that uses a fixed AudioInputStream.
 */
public class FixedAudioResource implements AudioResource {

    private final AudioInputStream audioInputStream;

    public FixedAudioResource(AudioInputStream audioInputStream) {
        this.audioInputStream = audioInputStream;
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    public static Supplier<AudioResource> fixed(AudioInputStream audioInputStream) {
        return () -> new FixedAudioResource(audioInputStream);
    }

}
