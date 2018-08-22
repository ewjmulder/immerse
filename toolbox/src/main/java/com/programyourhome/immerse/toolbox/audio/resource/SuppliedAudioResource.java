package com.programyourhome.immerse.toolbox.audio.resource;

import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.audio.resource.ResourceConfig;

/**
 * An audio resource that uses a Supplier to get an InputStream.
 * The provided supplier is used to request an audio input stream exactly once.
 */
public class SuppliedAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final AudioInputStream audioInputStream;
    private final ResourceConfig config;

    public SuppliedAudioResource(Supplier<AudioInputStream> audioInputStreamSupplier) {
        this(audioInputStreamSupplier, null);
    }

    public SuppliedAudioResource(Supplier<AudioInputStream> audioInputStreamSupplier, ResourceConfig config) {
        this.audioInputStream = audioInputStreamSupplier.get();
        this.config = config;
        if (config == null) {
            config = ResourceConfig.defaultNonLive(this.getFormat());
        }
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    @Override
    public ResourceConfig getConfig() {
        return this.config;
    }

    /**
     * Default config.
     */
    public static Factory<AudioResource> supplied(Supplier<AudioInputStream> audioInputStreamSupplier) {
        return supplied(audioInputStreamSupplier, null);
    }

    public static Factory<AudioResource> supplied(Supplier<AudioInputStream> audioInputStreamSupplier, ResourceConfig config) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new SuppliedAudioResource(audioInputStreamSupplier, config);
            }
        };
    }

}
