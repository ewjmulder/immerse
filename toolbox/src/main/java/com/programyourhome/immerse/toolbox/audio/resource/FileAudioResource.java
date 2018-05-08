package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource based on a file on disk.
 * The supported audio file types are the ones accepted by JVM's AudioSystem.
 */
public class FileAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

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

    public static Factory<AudioResource> filePath(String filePath) {
        return () -> new FileAudioResource(filePath);
    }

    public static Factory<AudioResource> file(File file) {
        return () -> new FileAudioResource(file);
    }

}
