package com.programyourhome.immerse.domain.audio.resource;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * An audio resource based on a file on disk.
 * The supported audio file types are the ones accepted by JVM's AudioSystem.
 */
public class FileAudioResource implements AudioResource {

    private final File file;

    public FileAudioResource(String path) {
        this(new File(path));
    }

    public FileAudioResource(File file) {
        this.file = file;
        if (!this.file.exists()) {
            throw new IllegalArgumentException("File: '" + file + "' does not exist.");
        }
    }

    @Override
    public AudioInputStream constructAudioStream() throws IOException {
        try {
            return AudioSystem.getAudioInputStream(this.file);
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

}
