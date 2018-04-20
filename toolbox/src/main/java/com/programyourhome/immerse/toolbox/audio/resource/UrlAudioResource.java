package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource based on a URL.
 * The supported audio file types are the ones accepted by JVM's AudioSystem.
 */
public class UrlAudioResource implements AudioResource {

    private AudioInputStream audioInputStream;

    public UrlAudioResource(String urlString) {
        try {
            this.setAudioInputStream(new URL(urlString));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid", e);
        }
    }

    public UrlAudioResource(URL url) {
        this.setAudioInputStream(url);
    }

    private void setAudioInputStream(URL url) {
        try {
            this.audioInputStream = AudioSystem.getAudioInputStream(url);
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("Exception while getting audio input stream", e);
        }
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    public static Supplier<AudioResource> urlString(String urlString) {
        return () -> new UrlAudioResource(urlString);
    }

    public static Supplier<AudioResource> url(URL url) {
        return () -> new UrlAudioResource(url);
    }

}
