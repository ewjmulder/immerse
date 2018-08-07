package com.programyourhome.immerse.domain.audio.resource;

import java.io.Serializable;

import javax.sound.sampled.AudioInputStream;

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

    /**
     * Whether the input stream is dynamic or not.
     * In this context dynamic means it is generated 'on the fly' and non-repeatable,
     * like live microphone input, some kind of (broadcasted) stream or non-repeatable algorithmically generated audio.
     */
    public default boolean isDynamic() {
        return false;
    }

}