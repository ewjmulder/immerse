package com.programyourhome.immerse.toolbox.audio.resource;

import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource that uses a Supplier to get an AudioInputStream.
 * The provided supplier should always return a 'fresh' audio input stream (with the same contents)
 * that can be played individually.
 */
public class SuppliedAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final Supplier<AudioInputStream> audioInputStreamSupplier;

    public SuppliedAudioResource(Supplier<AudioInputStream> audioInputStreamSupplier) {
        this.audioInputStreamSupplier = audioInputStreamSupplier;
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStreamSupplier.get();
    }

    public static Factory<AudioResource> supplied(Supplier<AudioInputStream> audioInputStreamSupplier) {
        return () -> new SuppliedAudioResource(audioInputStreamSupplier);
    }

}
