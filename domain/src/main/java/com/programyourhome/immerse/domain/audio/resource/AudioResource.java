package com.programyourhome.immerse.domain.audio.resource;

import javax.sound.sampled.AudioInputStream;

/**
 * An audio resource is an object that can provide an AudioInputStream.
 */
public interface AudioResource {

    /**
     * Get the AudioInputStream for this audio resource.
     * This method should always return the same object.
     */
    public AudioInputStream getAudioInputStream();

}