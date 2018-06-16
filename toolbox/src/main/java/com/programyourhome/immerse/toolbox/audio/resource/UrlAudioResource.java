package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource based on a URL.
 * The supported audio file types are the ones accepted by JVM's AudioSystem.
 */
public class UrlAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private InputStream inputStream;

    public UrlAudioResource(String urlString) {
        this.setAudioInputStream(toURLNoCheckedException(urlString));
    }

    public UrlAudioResource(URL url) {
        this.setAudioInputStream(url);
    }

    private void setAudioInputStream(URL url) {
        try {
            this.inputStream = url.openStream();
        } catch (IOException e) {
            throw new IllegalStateException("Exception while getting audio input stream", e);
        }
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    public static URL toURLNoCheckedException(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid", e);
        }
    }

    public static Factory<AudioResource> url(String urlString) {
        return url(toURLNoCheckedException(urlString));
    }

    public static Factory<AudioResource> url(URL url) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new UrlAudioResource(url);
            }
        };
    }

}
