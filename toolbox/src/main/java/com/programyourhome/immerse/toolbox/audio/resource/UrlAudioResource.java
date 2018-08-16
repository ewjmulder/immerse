package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioFileType;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * An audio resource based on a URL. There are 3 possible ways to configure the format:
 * 1. Provide the audio file type explicitly. The appropriate headers are expected to be present in the stream.
 * 2. Provide the audio format explicitly. The stream is expected to contain just PCM frames and no headers.
 */
public class UrlAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private AudioInputStream audioInputStream;
    private final boolean live;

    /**
     * Option 1 (see class Javadoc).
     */
    public UrlAudioResource(URL url, AudioFileType fileType, boolean live) {
        try {
            this.audioInputStream = fileType.getReaderInstance().getAudioInputStream(url.openStream());
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("Exception while creating audio input stream", e);
        }
        this.live = live;
    }

    /**
     * Option 2 (see class Javadoc).
     */
    public UrlAudioResource(URL url, ImmerseAudioFormat audioFormat, boolean live) {
        try {
            this.audioInputStream = new AudioInputStream(url.openStream(), audioFormat.toJavaAudioFormat(), AudioSystem.NOT_SPECIFIED);
        } catch (IOException e) {
            throw new IllegalStateException("Exception while creating audio input stream", e);
        }
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
    public static Factory<AudioResource> urlWithType(String urlString, AudioFileType audioFileType) {
        return urlWithType(toURLNoCheckedException(urlString), audioFileType, false);
    }

    public static Factory<AudioResource> urlWithType(String urlString, AudioFileType audioFileType, boolean live) {
        return urlWithType(toURLNoCheckedException(urlString), audioFileType, live);
    }

    public static Factory<AudioResource> urlWithType(URL url, AudioFileType audioFileType, boolean live) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new UrlAudioResource(url, audioFileType, live);
            }
        };
    }

    /**
     * Default not live.
     */
    public static Factory<AudioResource> urlWithFormat(String urlString, ImmerseAudioFormat audioFormat) {
        return urlWithFormat(toURLNoCheckedException(urlString), audioFormat, false);
    }

    public static Factory<AudioResource> urlWithFormat(String urlString, ImmerseAudioFormat audioFormat, boolean live) {
        return urlWithFormat(toURLNoCheckedException(urlString), audioFormat, live);
    }

    public static Factory<AudioResource> urlWithFormat(URL url, ImmerseAudioFormat audioFormat, boolean live) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new UrlAudioResource(url, audioFormat, live);
            }
        };
    }
    
    private static URL toURLNoCheckedException(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid", e);
        }
    }

}
