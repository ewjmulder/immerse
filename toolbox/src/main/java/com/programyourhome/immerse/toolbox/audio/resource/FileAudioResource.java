package com.programyourhome.immerse.toolbox.audio.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
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

    private interface ExceptionalFunction<Input, Output> {
        public Output apply(Input input) throws IOException, UnsupportedAudioFileException;
    }

    public FileAudioResource(String path) {
        this(new File(path));
    }

    public FileAudioResource(File file) {
        this.audioInputStream = this.loadFile(file, inputFile -> AudioSystem.getAudioInputStream(inputFile));
    }

    public FileAudioResource(File file, AudioFormat format, long length) {
        this.audioInputStream = this.loadFile(file, inputFile -> new AudioInputStream(new FileInputStream(inputFile), format, length));
    }

    private AudioInputStream loadFile(File file, ExceptionalFunction<File, AudioInputStream> consumer) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File: '" + file + "' does not exist.");
        }
        try {
            return consumer.apply(file);
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("Exception while getting audio input stream", e);
        }

    }

    @Override
    public AudioInputStream getAudioInputStream() {
        return this.audioInputStream;
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

    public static Factory<AudioResource> fileWithoutHeaders(File file, AudioFormat format, long length) {
        return new Factory<AudioResource>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public AudioResource create() {
                return new FileAudioResource(file, format, length);
            }
        };
    }

}
