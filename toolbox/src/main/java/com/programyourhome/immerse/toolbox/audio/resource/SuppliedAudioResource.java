package com.programyourhome.immerse.toolbox.audio.resource;

import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource that uses a Supplier to get an InputStream.
 * The provided supplier is used to request an audio input stream exactly once.
 */
public class SuppliedAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final AudioInputStream audioInputStream;
    private final boolean live;

    public SuppliedAudioResource(Supplier<AudioInputStream> audioInputStreamSupplier, boolean live) {
        this.audioInputStream = audioInputStreamSupplier.get();
        this.live = live;
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    @Override
    public boolean isLive() {
        return this.live;
    }

    /**
     * Default not live.
     */
    public static Factory<AudioResource> supplied(Supplier<AudioInputStream> audioInputStreamSupplier) {
    	return supplied(audioInputStreamSupplier, false);
    }

    public static Factory<AudioResource> supplied(Supplier<AudioInputStream> audioInputStreamSupplier, boolean live) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new SuppliedAudioResource(audioInputStreamSupplier, live);
            }
        };
    }

}
