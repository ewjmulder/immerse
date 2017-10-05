package com.programyourhome.immerse.audiostreaming.model.audioresource;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class FileAudioResource implements AudioResource {

    private File file;

    protected FileAudioResource(String path) {
        this(new File(path));
    }

    protected FileAudioResource(File file) {
        this.file = file;
        if (!this.file.exists()) {
            throw new IllegalArgumentException("File: '" + file + "' does not exists.");
        }
    }

    @Override
    public AudioInputStream getAudioStream() throws IOException {
        try {
            return AudioSystem.getAudioInputStream(this.file);
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

}
