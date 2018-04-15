package com.programyourhome.immerse.audiostreaming.generate;

import javax.sound.sampled.AudioInputStream;

/**
 * Interface for anything that can create an audio input stream.
 * Most implementing classes will have constructor parameters to control the output.
 */
public interface AudioInputStreamGenerator {

    /**
     * Generate an audio input stream.
     * Should return a new, fresh object on every call that can be played independently.
     */
    public AudioInputStream generate();

}
