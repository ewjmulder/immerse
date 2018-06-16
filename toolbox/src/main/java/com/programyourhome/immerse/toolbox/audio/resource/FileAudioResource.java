package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.audio.resource.AudioResource;

/**
 * An audio resource based on a file on disk.
 * The supported audio file types are the ones accepted by JVM's AudioSystem.
 */
public class FileAudioResource implements AudioResource {

    private static final long serialVersionUID = Serialization.VERSION;

    private final InputStream inputStream;

    public FileAudioResource(String path) {
        this(new File(path));
    }

    public FileAudioResource(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File: '" + file + "' does not exist.");
        }
        try {
            this.inputStream = new FileInputStream(file);
        } catch (IOException e) {
            throw new IllegalStateException("Exception while getting input stream", e);
        }
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    public static Factory<AudioResource> file(String filePath) {
        return file(new File(filePath));
    }

    public static Factory<AudioResource> file(File file) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new FileAudioResource(file);
            }
        };
    }

}
