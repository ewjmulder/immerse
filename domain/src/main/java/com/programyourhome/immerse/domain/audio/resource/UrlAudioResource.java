package com.programyourhome.immerse.domain.audio.resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class UrlAudioResource implements AudioResource {

    private URL url;

    protected UrlAudioResource(String urlString) {
        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid.", e);
        }
    }

    protected UrlAudioResource(URL url) {
        this.url = url;
    }

    @Override
    public AudioInputStream getAudioStream() throws IOException {
        try {
            return AudioSystem.getAudioInputStream(this.url);
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

}
