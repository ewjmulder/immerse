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
import com.programyourhome.immerse.domain.audio.resource.StreamConfig;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * An audio resource based on a URL. There are 3 possible ways to configure the format:
 * 1. Provide the audio file type explicitly. The appropriate headers are expected to be present in the stream.
 * 2. Provide the audio format explicitly. The stream is expected to contain just PCM frames and no headers.
 */
public class UrlAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final AudioInputStream audioInputStream;
    private StreamConfig config;

    /**
     * Option 1 (see class Javadoc).
     */
    public UrlAudioResource(URL url, AudioFileType fileType, StreamConfig config) {
        try {
            this.audioInputStream = fileType.getReaderInstance().getAudioInputStream(url.openStream());
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("Exception while creating audio input stream", e);
        }
        this.setConfigOrDefault(config);
    }

    /**
     * Option 2 (see class Javadoc).
     */
    public UrlAudioResource(URL url, ImmerseAudioFormat audioFormat, StreamConfig config) {
        try {
            this.audioInputStream = new AudioInputStream(url.openStream(), audioFormat.toJavaAudioFormat(), AudioSystem.NOT_SPECIFIED);
        } catch (IOException e) {
            throw new IllegalStateException("Exception while creating audio input stream", e);
        }
        this.setConfigOrDefault(config);
    }

    private void setConfigOrDefault(StreamConfig config) {
        this.config = config;
        if (this.config == null) {
            this.config = StreamConfig.defaultNonLive(this.getFormat());
        }
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    @Override
    public StreamConfig getConfig() {
        return this.config;
    }

    /**
     * Default config.
     */
    public static Factory<AudioResource> urlWithType(String urlString, AudioFileType audioFileType) {
        return urlWithType(toURLNoCheckedException(urlString), audioFileType, null);
    }

    public static Factory<AudioResource> urlWithType(String urlString, AudioFileType audioFileType, StreamConfig config) {
        return urlWithType(toURLNoCheckedException(urlString), audioFileType, config);
    }

    public static Factory<AudioResource> urlWithType(URL url, AudioFileType audioFileType, StreamConfig config) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new UrlAudioResource(url, audioFileType, config);
            }
        };
    }

    /**
     * Default config.
     */
    public static Factory<AudioResource> urlWithFormat(String urlString, ImmerseAudioFormat audioFormat) {
        return urlWithFormat(toURLNoCheckedException(urlString), audioFormat, null);
    }

    public static Factory<AudioResource> urlWithFormat(String urlString, ImmerseAudioFormat audioFormat, StreamConfig config) {
        return urlWithFormat(toURLNoCheckedException(urlString), audioFormat, config);
    }

    public static Factory<AudioResource> urlWithFormat(URL url, ImmerseAudioFormat audioFormat, StreamConfig config) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new UrlAudioResource(url, audioFormat, config);
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
