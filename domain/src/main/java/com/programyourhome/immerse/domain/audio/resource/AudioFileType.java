package com.programyourhome.immerse.domain.audio.resource;

import javax.sound.sampled.spi.AudioFileReader;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.SoftMidiAudioFileReader;
import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFloatFileReader;

public enum AudioFileType {

    WAVE(WaveFileReader.class),
    WAVE_FLOAT(WaveFloatFileReader.class),
    AIFF(AiffFileReader.class),
    AU(AuFileReader.class),
    MIDI(SoftMidiAudioFileReader.class);

    private Class<? extends AudioFileReader> readerClass;

    private AudioFileType(Class<? extends AudioFileReader> readerClass) {
        this.readerClass = readerClass;
    }

    public AudioFileReader getReaderInstance() {
        try {
            return this.readerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Exception during creating instance of reader class", e);
        }
    }

}
