package com.programyourhome.immerse.domain.audio.resource;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * An audio resource based on a file on disk.
 * The supported audio file types are the ones accepted by JVM's AudioSystem.
 */
public class FileAudioResource implements AudioResource {

    private final AudioInputStream audioInputStream;

    public FileAudioResource(String path) {
        this(new File(path));
    }

    public FileAudioResource(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File: '" + file + "' does not exist.");
        }
        try {
            this.audioInputStream = AudioSystem.getAudioInputStream(file);
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("Exception while getting audio input stream", e);
        }
    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
    }

    public static Supplier<AudioResource> filePath(String filePath) {
        return () -> new FileAudioResource(filePath);
    }

    public static Supplier<AudioResource> file(File file) {
        return () -> new FileAudioResource(file);
    }

}
