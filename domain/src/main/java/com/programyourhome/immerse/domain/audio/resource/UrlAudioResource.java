package com.programyourhome.immerse.domain.audio.resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * An audio resource based on a URL.
 * The supported audio file types are the ones accepted by JVM's AudioSystem.
 */
public class UrlAudioResource implements AudioResource {

    private URL url;

    public UrlAudioResource(String urlString) {
        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid", e);
        }
    }

    public UrlAudioResource(URL url) {
        this.url = url;
    }

    @Override
    public AudioInputStream constructAudioStream() throws IOException {
        try {
            return AudioSystem.getAudioInputStream(this.url);
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

}
