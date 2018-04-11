package com.programyourhome.immerse.domain.audio.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;

/**
 * An audio resource is an object that can provide an AudioInputStream through a Supplier.
 */
public interface AudioResource {

    /**
     * Get a supplier that can construct an AudioInputStream for this audio resource.
     * NB: Every 'get' call of the supplier should create a new stream object (with the same contents), that can be played independently.
     */
    public Supplier<AudioInputStream> getAudioInputStreamSupplier() throws IOException;

    /**
     * From a file path.
     */
    public static AudioResource fromFilePath(String filePath) {
        return new FileAudioResource(filePath);
    }

    /**
     * From a file.
     */
    public static AudioResource fromFile(File file) {
        return new FileAudioResource(file);
    }

    /**
     * From a URL string.
     */
    public static AudioResource fromUrlString(String urlString) {
        return new UrlAudioResource(urlString);
    }

    /**
     * From a URL object.
     */
    public static AudioResource fromUrl(URL url) {
        return new UrlAudioResource(url);
    }

}
