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
     * Whether the input stream is live or not.
     * In this context live means it is generated 'on the fly' and non-repeatable,
     * like microphone input, some kind of (broadcasted) stream or dynamically generated audio.
     * This also indicates that the input is only available at the 'live time rate', meaning that
     * the stream supplies as many bytes per second as a player will play. Normally,
     * that means some fair amount of buffer should be in place, but for Immerse, we want to
     * keep this buffer as small as possible for the best 'live' experience.
     * Therefore, Immerse will handle live audio resources somewhat different than non-live ones.
     * 
     * Also, the recommendation for live streams is to keep the buffer size at the sending side
     * as small as possible, both with writing to the stream as in consuming any underlying source input stream.
     * It might even be beneficial to skip some bytes to get closer to 'live' if some hickup
     * on the Immerse side caused an extra buffer buildup.
     */
    public default boolean isLive() {
        return false;
    }

}