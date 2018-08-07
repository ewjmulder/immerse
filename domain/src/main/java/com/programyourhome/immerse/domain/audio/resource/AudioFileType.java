package com.programyourhome.immerse.domain.audio.resource;

import javax.sound.sampled.spi.AudioFileReader;

/**
 * All possible audio file types as supported by the JVM Sound API.
 * The reflection detour is taken to prevent internal API warnings.
 * The risk of Sound API changes is quite minimal, so accepted.
 */
public enum AudioFileType {

    WAVE("WaveFileReader"),
    WAVE_FLOAT("WaveFloatFileReader"),
    AIFF("AiffFileReader"),
    AU("AuFileReader"),
    MIDI("SoftMidiAudioFileReader");

    private String readerClassName;

    private AudioFileType(String readerClassName) {
        this.readerClassName = readerClassName;
    }

    public AudioFileReader getReaderInstance() {
        try {
            Class<?> readerClass = Class.forName("com.sun.media.sound." + this.readerClassName);
            return (AudioFileReader) readerClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Exception during instantiation of reader class", e);
        }
    }

}
