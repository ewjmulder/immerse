package com.programyourhome.immerse.domain.audio.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;

/**
 * An audio resource is an object that can construct an AudioInputStream.
 */
public interface AudioResource {

    /**
     * Construct an AudioInputStream for this audio resource.
     * NB: Every call should create a new stream object (with the same contents), that can be played independently.
     */
    public AudioInputStream constructAudioStream() throws IOException;

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

    /**
     * From some kind of supplier of AudioInputStream objects.
     * NB: The same rule applies: the supplier should return a 'fresh' stream object for every call.
     */
    public static AudioResource fromSupplier(Supplier<AudioInputStream> audioInputStreamSupplier) {
        return () -> audioInputStreamSupplier.get();
    }

}
