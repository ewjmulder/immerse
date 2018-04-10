package com.programyourhome.immerse.domain.audio.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;

public interface AudioResource {

    /**
     * Get an audio stream of this audio resource.
     * NB: Every call should create a new stream object (with the same contents), that can be played independently.
     */
    public AudioInputStream getAudioStream() throws IOException;

    public static AudioResource fromFilePath(String filePath) {
        return new FileAudioResource(filePath);
    }

    public static AudioResource fromFile(File file) {
        return new FileAudioResource(file);
    }

    public static AudioResource fromUrlString(String urlString) {
        return new UrlAudioResource(urlString);
    }

    public static AudioResource fromUrl(URL url) {
        return new UrlAudioResource(url);
    }

    public static AudioResource fromSupplier(Supplier<AudioInputStream> audioInputStreamSupplier) {
        return () -> audioInputStreamSupplier.get();
    }

}
