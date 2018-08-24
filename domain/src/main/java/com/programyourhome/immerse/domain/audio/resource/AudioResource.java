package com.programyourhome.immerse.domain.audio.resource;

import java.io.Serializable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * An audio resource is an object that can provide an AudioInputStream.
 */
public interface AudioResource extends Serializable {

    /**
     * Get the AudioInputStream for this audio resource.
     * This method should always return the same object.
     * The returned stream will be consumed just once.
     */
    public AudioInputStream getAudioInputStream();

    public default ImmerseAudioFormat getFormat() {
        return ImmerseAudioFormat.fromJavaAudioFormat(getAudioInputStream().getFormat());
    }

    public default AudioFormat getFormatJava() {
        return getAudioInputStream().getFormat();
    }

    /**
     * Get the config for this resource.
     */
    public default StreamConfig getConfig() {
        return StreamConfig.defaultNonLive(this.getFormat());
    }

}